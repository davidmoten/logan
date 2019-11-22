package com.github.davidmoten.logan;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Scanner;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

public final class DataHelper {

    private DataHelper() {
        // prevent instantiation
    }
    
	public static Buckets execute(Data data, BucketQuery query) {
		// get the time range of entries
		Iterable<LogEntry> entries = data.find(query.getStartTime(),
				query.getFinishTime());

		// filter by field, source, text
		Iterable<LogEntry> filtered = filter(entries, query);

		// get numeric values or count
		return getBuckets(filtered, query);
	}

	private static Iterable<LogEntry> filter(Iterable<LogEntry> entries,
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

	private static Iterable<LogEntry> filterByField(Iterable<LogEntry> filtered,
			final String field) {
		return Iterables.filter(filtered, new Predicate<LogEntry>() {
			@Override
			public boolean apply(LogEntry entry) {
				String value = entry.getProperties().get(field);
				return value != null;
			}
		});
	}

	private static Iterable<LogEntry> filterBySource(Iterable<LogEntry> filtered,
			final String source) {
		return Iterables.filter(filtered, new Predicate<LogEntry>() {
			@Override
			public boolean apply(LogEntry entry) {
				String src = entry.getProperties().get(Field.SOURCE);
				return source.equals(src);
			}
		});
	}

	private static Iterable<LogEntry> filterByText(Iterable<LogEntry> filtered,
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

	private static Buckets getBuckets(Iterable<LogEntry> filtered,
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
					scanner.next();
			}
			if (i < index)
				d = null;
			scanner.close();
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

	public static Iterable<String> getLogs(Data data, long startTime, long finishTime) {
		return Iterables.transform(data.find(startTime, finishTime),
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

	public static void addRandomLogEntry(Data data, int range, int n) {
		for (int i = 1; i <= n; i++) {
			data.add(createRandomLogEntry(i, range));
		}
	}

	private static LogEntry createRandomLogEntry(int i, int range) {
		Map<String, String> map = Maps.newHashMap();
		String sp1 = Math.random() * range + "";
		map.put("specialNumber", sp1);
		String sp2 = Math.random() * range / 2 + "";
		map.put("specialNumber2", sp2);
		boolean processing = Math.random() > 0.5;
		map.put("processing", processing + "");
		long time = System.currentTimeMillis()
				- TimeUnit.MINUTES.toMillis(i);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-dd-MM HH:mm:ss.SSS");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		map.put(Field.MSG,sdf.format(new Date(time)) +  " INFO processing=" + processing + ",specialNumber=" + sp1
                + ",specialNumber2=" + sp2);
        return new LogEntry(time, map);
	}

}
