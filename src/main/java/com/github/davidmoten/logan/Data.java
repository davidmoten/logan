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
     * @param entry entry
     * @return this this
     */
    Data add(LogEntry entry);

    long getNumEntries();

    /**
     * Returns total number of entries added so far (older entries may have been
     * discarded so this is not the same as the current number of entries).
     * 
     * @return total number of entries added so far
     */
    long getNumEntriesAdded();

    NavigableSet<String> getKeys();

    NavigableSet<String> getSources();

    void close() throws Exception;
    
}