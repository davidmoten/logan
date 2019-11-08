package com.github.davidmoten.logan;

import java.util.NavigableSet;

import com.github.davidmoten.bplustree.BPlusTree;
import com.github.davidmoten.bplustree.Serializer;

public class DataPersistedBPlusTree implements Data {

    private final BPlusTree<StringWithTimestamp, String> properties;

    public DataPersistedBPlusTree() {
        this.properties = BPlusTree //
                .file() //
                .directory(System.getProperty("java.io.tmp")) //
                .keySerializer(StringWithTimestamp.SERIALIZER) //
                .valueSerializer(Serializer.utf8(0)) //
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getNumEntries() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long getNumEntriesAdded() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public NavigableSet<String> getKeys() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public NavigableSet<String> getSources() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void close() {
        // TODO Auto-generated method stub

    }

}
