package com.github.davidmoten.logan;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;

public class Data {

	private TreeMap<Long, Collection<LogEntry>> map;
	private ListMultimap<Long, LogEntry> facade;

	public Data() {
		map = Maps.newTreeMap();
		facade = Multimaps.newListMultimap(map, new Supplier<List<LogEntry>>() {
			@Override
			public List<LogEntry> get() {
				return Lists.newArrayList(); // assuming you want to use
												// ArrayList
			}
		});
	}

	public Data add(LogEntry entry) {
		facade.put(entry.getTime(), entry);
		return this;
	}

	public Iterable<LogEntry> find(final long startTime, final long finishTime,
			final String name) {

		return new Iterable<LogEntry>() {

			@Override
			public Iterator<LogEntry> iterator() {
				return createIterator(startTime, finishTime, name);
			}

		};

	}

	private Iterator<LogEntry> createIterator(final long startTime,
			final long finishTime, String name) {

		return new Iterator<LogEntry>() {

			Long t = map.ceilingKey(startTime);
			Long last = map.floorKey(finishTime);
			Iterator<LogEntry> it = null;

			@Override
			public boolean hasNext() {
				if (it == null || !it.hasNext())
					return t != null && t < last;
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

	public Iterable<Bucket> find(final BucketQuery query) {

		Iterable<LogEntry> entries = find(query.getStartTime().getTime(),
				query.getFinishTime(), query.getName());
		Iterable<LogEntry> filtered = Iterables.filter(entries,
				new Predicate<LogEntry>() {
					@Override
					public boolean apply(LogEntry entry) {
						return entry.getProperties().containsKey(
								query.getName());
					}
				});
		return null;
	}
}
