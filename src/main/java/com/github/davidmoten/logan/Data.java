package com.github.davidmoten.logan;

import java.util.Date;
import java.util.NavigableSet;

public interface Data {

	/**
	 * Adds a {@link LogEntry} to the data.
	 * 
	 * @param entry
	 * @return this
	 */
	Data add(LogEntry entry);

	Iterable<LogEntry> find(long startTime, long finishTime);

	Buckets execute(BucketQuery query);

	long getNumEntries();

	long getNumEntriesAdded();

	NavigableSet<String> getKeys();

	Iterable<String> getLogs(long startTime, long finishTime);

	NavigableSet<String> getSources();

	Date oldestTime();

	void close();

}