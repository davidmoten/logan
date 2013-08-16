package com.github.davidmoten.logan;

import java.util.Date;
import java.util.logging.Logger;

/**
 * Generates sql for an aggregated query from startTime,
 * intervalSizeMs,numIntervals and sql base.
 * 
 * @author dave
 * 
 */
public class BucketQuery {

	private static final Logger log = Logger.getLogger(BucketQuery.class
			.getName());

	private final Date startTime;
	private final double intervalSizeMs;
	private final long numIntervals;
	private final String name;

	/**
	 * Constructor.
	 * 
	 * @param startTime
	 * @param intervalSizeMs
	 * @param numIntervals
	 * @param sql
	 */
	public BucketQuery(Date startTime, double intervalSizeMs,
			long numIntervals, String name) {
		super();
		this.startTime = startTime;
		this.intervalSizeMs = intervalSizeMs;
		this.numIntervals = numIntervals;
		this.name = name;
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

	public String getName() {
		return name;
	}

}
