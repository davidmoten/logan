package com.github.davidmoten.logan;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.SortedMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import com.google.common.base.Supplier;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;

public class DataMemory implements Data {

	private static final int DEFAULT_MAX_SIZE = 1000000;

	private static Logger log = Logger.getLogger(DataMemory.class.getName());

	private ListMultimap<Long, LogEntry> facade;
	private final TreeSet<String> keys = Sets.newTreeSet();
	private final TreeSet<String> sources = Sets.newTreeSet();

	private final int maxSize;
	private final AtomicLong counter = new AtomicLong();
	private int numEntries;

	public DataMemory() {
		this(DEFAULT_MAX_SIZE, false);
	}

	public DataMemory(int maxSize, boolean loadDummyData) {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.github.davidmoten.logan.Data#add(com.github.davidmoten.logan.LogEntry
	 * )
	 */
	@Override
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

	@Override
	public synchronized Iterable<LogEntry> find(final long startTime,
			final long finishTime) {

		return Iterables.concat(asSortedMap().subMap(startTime, finishTime)
				.values());
	}

	@Override
	public synchronized Buckets execute(final BucketQuery query) {

		return DataCore.Singleton.INSTANCE.instance().execute(this, query);
	}

	@Override
	public synchronized long getNumEntries() {
		return numEntries;
	}

	@Override
	public synchronized long getNumEntriesAdded() {
		return counter.get();
	}

	@Override
	public NavigableSet<String> getKeys() {
		return keys;
	}

	@Override
	public NavigableSet<String> getSources() {
		return sources;
	}

	@Override
	public void close() {
		// do nothing
	}

	@Override
	public Date oldestTime() {

		if (asSortedMap().isEmpty())
			return null;
		else
			return new Date(asSortedMap().firstKey());
	}

	@Override
	public Iterable<String> getLogs(long startTime, long finishTime) {
		return DataCore.Singleton.INSTANCE.instance().getLogs(this, startTime,
				finishTime);
	}

	private synchronized void incrementCounter() {
		if (counter.incrementAndGet() % 1000 == 0)
			log.info(counter + " log lines processed");
	}

}
