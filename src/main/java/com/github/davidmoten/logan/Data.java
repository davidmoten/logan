package com.github.davidmoten.logan;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.Scanner;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;

public class Data {

	private static final int DEFAULT_MAX_SIZE = 1000000;

	private static Logger log = Logger.getLogger(Data.class.getName());

	private ListMultimap<Long, LogEntry> facade;
	private final TreeSet<String> keys = Sets.newTreeSet();
	private final TreeSet<String> sources = Sets.newTreeSet();

	private final int maxSize;
	private final AtomicLong counter = new AtomicLong();
	private int numEntries;

	public Data() {
		this(DEFAULT_MAX_SIZE, false);
	}

	public Data(int maxSize, boolean loadDummyData) {
		this.maxSize = maxSize;
		ConcurrentSkipListMap<Long, Collection<LogEntry>> map = new ConcurrentSkipListMap<Long, Collection<LogEntry>>();
		facade = Multimaps.newListMultimap(map, new Supplier<List<LogEntry>>() {
			@Override
			public List<LogEntry> get() {
				return Lists.newArrayList();
			}
		});
		if (loadDummyData)
			for (int i = 0; i < 10000; i++)
				add(createRandomLogEntry(i));
	}

	private static LogEntry createRandomLogEntry(int i) {
		Map<String, String> map = Maps.newHashMap();
		String sp1 = Math.random() * 100 + "";
		map.put("specialNumber", sp1);
		String sp2 = Math.random() * 50 + "";
		map.put("specialNumber2", sp2);
		boolean processing = Math.random() > 0.5;
		map.put("processing", processing + "");
		map.put(Field.MSG, "processing=" + processing + ",specialNumber=" + sp1
				+ ",specialNumber2=" + sp2);

		return new LogEntry(System.currentTimeMillis()
				- TimeUnit.MINUTES.toMillis(i), map);
	}

	/**
	 * Adds a {@link LogEntry} to the data.
	 * 
	 * @param entry
	 * @return this
	 */
	public synchronized Data add(LogEntry entry) {
		facade.put(entry.getTime(), entry);
		for (Entry<String, String> pair : entry.getProperties().entrySet())
			if (isNumeric(pair.getValue()))
				keys.add(pair.getKey());
		if (numEntries % 10000 == 0 && numEntries < maxSize)
			log.info("numEntries=" + numEntries);

		// note that for ConcurrentSkipListMap the size method is not a
		// constant-time operation so don't call facade.size()
		numEntries++;
		if (numEntries > maxSize) {
			List<LogEntry> list = facade.removeAll(asSortedMap().firstKey());
			numEntries -= list.size();
		}
		String source = entry.getSource();
		if (source != null)
			sources.add(source);
		incrementCounter();
		return this;
	}

	private SortedMap<Long, Collection<LogEntry>> asSortedMap() {
		return (SortedMap<Long, Collection<LogEntry>>) facade.asMap();
	}

	private boolean isNumeric(String s) {
		try {
			Double.parseDouble(s);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public synchronized Iterable<LogEntry> find(final long startTime,
			final long finishTime) {

		return Iterables.concat(asSortedMap().subMap(startTime, finishTime)
				.values());
	}

	public synchronized Buckets execute(final BucketQuery query) {

		// get the time range of entries
		Iterable<LogEntry> entries = find(query.getStartTime().getTime(),
				query.getFinishTime());

		// filter by field, source, text
		Iterable<LogEntry> filtered = filter(entries, query);

		// get numeric values or count
		Buckets buckets = getBuckets(filtered, query);

		return buckets;
	}

	private Iterable<LogEntry> filter(Iterable<LogEntry> entries,
			BucketQuery query) {

		Iterable<LogEntry> filtered = entries;
		// filter by field
		if (query.getField().isPresent()) {
			filtered = filterByField(filtered, query.getField().get());
		}

		// filter by source
		if (query.getSource().isPresent())
			filtered = filterBySource(filtered, query.getSource().get());

		// filter by text
		if (query.getText().isPresent()) {
			Pattern p = Pattern.compile(query.getText().get());
			filtered = filterByText(filtered, p);
		}

		return filtered;
	}

	private Iterable<LogEntry> filterByField(Iterable<LogEntry> filtered,
			final String field) {
		return Iterables.filter(filtered, new Predicate<LogEntry>() {
			@Override
			public boolean apply(LogEntry entry) {
				String value = entry.getProperties().get(field);
				return value != null;
			}
		});
	}

	private Iterable<LogEntry> filterBySource(Iterable<LogEntry> filtered,
			final String source) {
		return Iterables.filter(filtered, new Predicate<LogEntry>() {
			@Override
			public boolean apply(LogEntry entry) {
				String src = entry.getProperties().get(Field.SOURCE);
				return source.equals(src);
			}
		});
	}

	private Iterable<LogEntry> filterByText(Iterable<LogEntry> filtered,
			final Pattern searchFor) {
		return Iterables.filter(filtered, new Predicate<LogEntry>() {
			@Override
			public boolean apply(LogEntry entry) {
				return contains(entry, Field.MSG, searchFor)
						|| contains(entry, Field.LEVEL, searchFor)
						|| contains(entry, Field.METHOD, searchFor)
						|| contains(entry, Field.SOURCE, searchFor)
						|| contains(entry, Field.THREAD_NAME, searchFor);
			}
		});
	}

	private Buckets getBuckets(Iterable<LogEntry> filtered,
			final BucketQuery query) {
		final Optional<Pattern> delimiterPattern;
		if (query.getDelimiterPattern().isPresent())
			delimiterPattern = Optional.of(Pattern.compile(query
					.getDelimiterPattern().get()));
		else
			delimiterPattern = Optional.absent();

		Buckets buckets = new Buckets(query);
		for (LogEntry entry : filtered) {
			if (query.getField().isPresent()) {
				String s = entry.getProperties().get(query.getField().get());
				try {
					double d = Double.parseDouble(s);
					buckets.add(entry.getTime(), d);
				} catch (NumberFormatException e) {
					// ignored value because non-numeric
				}
			} else if (query.getScan().isPresent()) {
				String msg = entry.getProperties().get(Field.MSG);
				Double d = getDouble(msg, delimiterPattern, query.getScan()
						.get());
				if (d != null)
					buckets.add(entry.getTime(), d);
			} else
				// just count the entries
				buckets.add(entry.getTime(), 1);
		}
		return buckets;
	}

	@VisibleForTesting
	static Double getDouble(String s, Optional<Pattern> delimiterPattern,
			int index) {
		log.info("scanning " + s + " for index " + index);
		if (s == null)
			return null;
		try {
			Scanner scanner = new Scanner(s);
			if (delimiterPattern.isPresent())
				scanner.useDelimiter(delimiterPattern.get());
			Double d = null;
			int i = 0;
			while (i < index && scanner.hasNext()) {
				if (scanner.hasNextDouble()) {
					i++;
					d = scanner.nextDouble();
				} else if (scanner.hasNext())
					System.out.println(scanner.next());
			}
			if (i < index)
				d = null;
			scanner.close();
			log.info("returning " + d);
			return d;
		} catch (RuntimeException e) {
			// could not find in msg
			return null;
		}
	}

	private static boolean contains(LogEntry entry, String field,
			Pattern searchFor) {
		String s = entry.getProperties().get(field);
		if (s == null)
			return false;
		else
			return searchFor.matcher(s).find();
	}

	public synchronized long getNumEntries() {
		return numEntries;
	}

	public synchronized long getNumEntriesAdded() {
		return counter.get();
	}

	public NavigableSet<String> getKeys() {
		return keys;
	}

	public Iterable<String> getLogs(long startTime, long finishTime) {
		return Iterables.transform(find(startTime, finishTime),
				new Function<LogEntry, String>() {
					@Override
					public String apply(LogEntry entry) {
						StringBuilder s = new StringBuilder();
						DateFormat df = new SimpleDateFormat(
								"yyyy-MM-dd HH:mm:ss.SSS");
						df.setTimeZone(TimeZone.getTimeZone("UTC"));
						String level = entry.getProperties().get(Field.LEVEL);
						if (level == null)
							level = "INFO";
						String logger = entry.getProperties().get(Field.LOGGER);
						if (logger == null)
							logger = "unknown";
						s.append(df.format(new Date(entry.getTime())));
						s.append(' ');
						s.append(level);
						s.append(' ');
						s.append(logger);
						s.append(" - ");
						s.append(entry.getProperties().get(Field.MSG));
						return s.toString();
					}
				});
	}

	public NavigableSet<String> getSources() {
		return sources;
	}

	public Date oldestTime() {

		if (asSortedMap().isEmpty())
			return null;
		else
			return new Date(asSortedMap().firstKey());
	}

	private synchronized void incrementCounter() {
		if (counter.incrementAndGet() % 1000 == 0)
			log.info(counter + " log lines processed");
	}

}
