package com.github.davidmoten.logan;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;

public class Data {

	private static Data instance;

	public static synchronized Data instance() {
		if (instance == null) {
			instance = new Data();
			for (int i = 0; i < 10000; i++)
				instance.add(createLogEntry(i));
		}
		return instance;
	}

	private static LogEntry createLogEntry(int i) {
		Map<String, String> map = Maps.newHashMap();
		map.put("specialNumber", Math.random() * 100 + "");
		return new LogEntry(System.currentTimeMillis()
				- TimeUnit.MINUTES.toMillis(i), map);
	}

	private TreeMap<Long, Collection<LogEntry>> map;
	private ListMultimap<Long, LogEntry> facade;
	private final TreeSet<String> keys = Sets.newTreeSet();

	public Data() {
		map = Maps.newTreeMap();
		facade = Multimaps.newListMultimap(map, new Supplier<List<LogEntry>>() {
			@Override
			public List<LogEntry> get() {
				return Lists.newArrayList();
			}
		});
	}

	public synchronized Data add(LogEntry entry) {
		facade.put(entry.getTime(), entry);
		keys.addAll(entry.getProperties().keySet());
		return this;
	}

	public synchronized Iterable<LogEntry> find(final long startTime,
			final long finishTime) {

		return new Iterable<LogEntry>() {

			@Override
			public Iterator<LogEntry> iterator() {
				return createIterator(startTime, finishTime);
			}

		};
	}

	private synchronized Iterator<LogEntry> createIterator(
			final long startTime, final long finishTime) {

		return new Iterator<LogEntry>() {

			Long t = map.ceilingKey(startTime);
			Long last = map.floorKey(finishTime);
			Iterator<LogEntry> it = null;

			@Override
			public boolean hasNext() {
				if (it == null || !it.hasNext())
					return last != null && t != null && t <= last;
				else
					return it.hasNext();
			}

			@Override
			public LogEntry next() {
				while (it == null || !it.hasNext()) {
					it = map.get(t).iterator();
					t = map.higherKey(t);
				}
				return it.next();
			}

			@Override
			public void remove() {
				throw new RuntimeException("not implemented");
			}

		};

	}

	public synchronized Buckets execute(final BucketQuery query) {

		Iterable<LogEntry> entries = find(query.getStartTime().getTime(),
				query.getFinishTime());
		Iterable<LogEntry> filtered = Iterables.filter(entries,
				new Predicate<LogEntry>() {
					@Override
					public boolean apply(LogEntry entry) {
						return entry.getProperties().containsKey(
								query.getName());
					}
				});

		Buckets buckets = new Buckets(query);
		for (LogEntry entry : filtered) {
			String s = entry.getProperties().get(query.getName());
			try {
				double d = Double.parseDouble(s);
				buckets.add(entry.getTime(), d);
			} catch (NumberFormatException e) {
				// ignored value because non-numeric
			}
		}

		return buckets;
	}

	public synchronized long getNumEntries() {
		return facade.size();
	}

	public TreeSet<String> getKeys() {
		return keys;
	}
}
