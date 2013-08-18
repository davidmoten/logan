package com.github.davidmoten.logan;

import java.util.Date;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

/**
 * Generates sql for an aggregated query from startTime,
 * intervalSizeMs,numIntervals and sql base.
 * 
 * @author dave
 * 
 */
public class BucketQuery {

	private final Date startTime;
	private final double intervalSizeMs;
	private final long numIntervals;
	private final String field;
	private final Optional<String> source;

	/**
	 * Constructor.
	 * 
	 * @param startTime
	 * @param intervalSizeMs
	 * @param numIntervals
	 * @param source
	 * @param sql
	 */
	public BucketQuery(Date startTime, double intervalSizeMs,
			long numIntervals, String field, Optional<String> source) {
		super();
		this.startTime = startTime;
		this.intervalSizeMs = intervalSizeMs;
		this.numIntervals = numIntervals;
		this.field = field;
		Preconditions.checkNotNull(source);
		this.source = source;
	}

	public Date getStartTime() {
		return startTime;
	}

	public double getIntervalSizeMs() {
		return intervalSizeMs;
	}

	public long getNumIntervals() {
		return numIntervals;
	}

	public boolean performAggregation() {
		return numIntervals > 0;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("BucketQuery [startTime=");
		builder.append(startTime.getTime());
		builder.append(", intervalSizeMs=");
		builder.append(intervalSizeMs);
		builder.append(", numIntervals=");
		builder.append(numIntervals);
		builder.append("]");
		return builder.toString();
	}

	public long getFinishTime() {
		long n;
		if (numIntervals == 0)
			n = 1;
		else
			n = numIntervals;
		return Math.round(startTime.getTime() + n * intervalSizeMs);
	}

	public String getField() {
		return field;
	}

	public Optional<String> getSource() {
		return source;
	}

}
