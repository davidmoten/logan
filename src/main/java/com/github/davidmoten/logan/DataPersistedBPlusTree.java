package com.github.davidmoten.logan;

import java.io.File;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.davidmoten.kool.Stream;

import com.github.davidmoten.bplustree.BPlusTree;

public final class DataPersistedBPlusTree implements Data {

    private static final int LOG_COUNT_EVERY = 10000;

    private static final Logger log = Logger.getLogger(DataPersistedBPlusTree.class.getName());

    private final BPlusTree<IntWithTimestamp, PropertyWithTimestamp> properties;

    private final Object changeLock = new Object();

    private long numEntries;

    private final TreeSet<String> keys = new TreeSet<>();

    private final TreeSet<String> sources = new TreeSet<>();

    public DataPersistedBPlusTree(String directory) {
        new File(directory).mkdirs();
        this.properties = BPlusTree //
                .file() //
                .directory(directory) //
                .keySerializer(IntWithTimestamp.SERIALIZER) //
                .valueSerializer(PropertyWithTimestamp.SERIALIZER) //
                .naturalOrder();
        log.info("constructed");
    }

    @Override
    public Buckets execute(BucketQuery query) {
        log.info(query.toString());
        Buckets buckets = new Buckets(query);
        if (query.getField().isPresent()) {
            IntWithTimestamp start = new IntWithTimestamp(query.getField().get().hashCode(), query.getStartTime());
            IntWithTimestamp finish = new IntWithTimestamp(query.getField().get().hashCode(), query.getFinishTime());
            log.info("querying properties for range " + start + " to " + finish);
            Stream.from(properties.find(new IntWithTimestamp(Integer.MIN_VALUE, Long.MIN_VALUE),
                    new IntWithTimestamp(Integer.MAX_VALUE, Long.MAX_VALUE), true)).forEach(System.out::println);
            properties.find(start, finish, true) //
                    .forEach(x -> {
                        log.info("record=" + x);
                        if (x.key.equals(query.getField().get())) {
                            // TODO check source, scan, etc
                            buckets.add(x.time, x.value);
                        }
                    });
        }
        return buckets;
    }

    @Override
    public Iterable<String> getLogs(long startTime, long finishTime) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<LogEntry> find(long startTime, long finishTime) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Data add(LogEntry entry) {
        synchronized (changeLock) {
            log.info("add " + entry);
            numEntries++;
            for (Entry<String, String> pair : entry.getProperties().entrySet()) {
                Double value = Util.parseDouble(pair.getValue());
                if (value != null) {
                    log.info("pair=" + pair);
                    keys.add(pair.getKey());
                    IntWithTimestamp k = new IntWithTimestamp(pair.getKey().hashCode(), entry.getTime());
                    PropertyWithTimestamp v = new PropertyWithTimestamp(pair.getKey(), value, entry.getTime());
                    System.out.println("inserting\n  " + k + "\n->" + v);
                    properties.insert(k, v);
                }
            }
            String source = entry.getSource();
            if (source != null) {
                sources.add(source);
            }

            if (numEntries % LOG_COUNT_EVERY == 0) {
                log.info("numEntries=" + numEntries);
            }
            return this;
        }

    }

    @Override
    public long getNumEntries() {
        return numEntries;
    }

    @Override
    public long getNumEntriesAdded() {
        return numEntries;
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
    public void close() throws Exception {
        properties.close();
    }

}
