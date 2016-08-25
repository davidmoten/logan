package com.github.davidmoten.logan;

import java.util.Date;
import java.util.NavigableSet;

public interface Data {

    Buckets execute(BucketQuery query);

    Iterable<String> getLogs(long startTime, long finishTime);

    Iterable<LogEntry> find(long startTime, long finishTime);

    /**
     * Adds a {@link LogEntry} to the data.
     * 
     * @param entry
     *            entry
     * @return this this
     */
    Data add(LogEntry entry);

    long getNumEntries();

    long getNumEntriesAdded();

    NavigableSet<String> getKeys();

    NavigableSet<String> getSources();

    void close();

    Date oldestTime();

}