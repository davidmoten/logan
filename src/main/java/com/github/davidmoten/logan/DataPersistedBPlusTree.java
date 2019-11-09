package com.github.davidmoten.logan;

import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import com.github.davidmoten.bplustree.BPlusTree;

public class DataPersistedBPlusTree implements Data {

    private static final Logger log = Logger.getLogger(DataPersistedBPlusTree.class.getName());

    private final BPlusTree<IntWithTimestamp, PropertyWithTimestamp> properties;

    private final Object changeLock = new Object();

    private long numEntries;

    private final TreeSet<String> keys = new TreeSet<>();

    private final TreeSet<String> sources = new TreeSet<>();

    public DataPersistedBPlusTree() {
        this.properties = BPlusTree //
                .file() //
                .directory(System.getProperty("java.io.tmp")) //
                .keySerializer(IntWithTimestamp.SERIALIZER) //
                .valueSerializer(PropertyWithTimestamp.SERIALIZER) //
                .naturalOrder();
    }

    @Override
    public Buckets execute(BucketQuery query) {
        // TODO Auto-generated method stub
        return null;
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
            numEntries++;
            for (Entry<String, String> pair : entry.getProperties().entrySet()) {
                Double value = Util.parseDouble(pair.getValue());
                if (value != null) {
                    keys.add(pair.getKey());
                    IntWithTimestamp k = new IntWithTimestamp(pair.getKey().hashCode(),
                            entry.getTime());
                    PropertyWithTimestamp v = new PropertyWithTimestamp(pair.getKey(), value,
                            entry.getTime());
                    properties.insert(k, v);
                }
            }

            String source = entry.getSource();
            if (source != null) {
                sources.add(source);
            }

            if (numEntries % 10000 == 0) {
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
