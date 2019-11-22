package com.github.davidmoten.logan;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.SortedMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import org.davidmoten.kool.Stream;

import com.google.common.base.Supplier;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
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

    private final Object changeLock = new Object();

    public DataMemory() {
        this(DEFAULT_MAX_SIZE);
    }

    public DataMemory(int maxSize) {
        this.maxSize = maxSize;
        ConcurrentSkipListMap<Long, Collection<LogEntry>> map = new ConcurrentSkipListMap<Long, Collection<LogEntry>>();
        facade = Multimaps.newListMultimap(map, new Supplier<List<LogEntry>>() {
            @Override
            public List<LogEntry> get() {
                return Lists.newArrayList();
            }
        });
    }

    @Override
    public Data add(LogEntry entry) {
        synchronized (changeLock) {
            facade.put(entry.getTime(), entry);
            for (Entry<String, String> pair : entry.getProperties().entrySet())
                if (Util.isNumeric(pair.getValue()))
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
    }

    private SortedMap<Long, Collection<LogEntry>> asSortedMap() {
        return (SortedMap<Long, Collection<LogEntry>>) facade.asMap();
    }

    @Override
    public Stream<LogEntry> find(final long startTime, final long finishTime) {

        synchronized (changeLock) {
            return Stream.from(Iterables.concat(asSortedMap().subMap(startTime, finishTime).values()));
        }
    }

    @Override
    public Buckets execute(final BucketQuery query) {

        synchronized (changeLock) {
            return DataHelper.execute(this, query);
        }
    }

    @Override
    public long getNumEntries() {
        synchronized (changeLock) {
            return numEntries;
        }
    }

    @Override
    public long getNumEntriesAdded() {
        synchronized (changeLock) {
            return counter.get();
        }
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
    public Stream<String> getLogs(long startTime, long finishTime) {
        return Stream.from(DataHelper.getLogs(this, startTime, finishTime));
    }

    private synchronized void incrementCounter() {
        if (counter.incrementAndGet() % 1000 == 0)
            log.info(counter + " log lines processed");
    }

}
