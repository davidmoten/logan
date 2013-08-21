package com.github.davidmoten.logan;

import java.util.Date;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

/**
 * Parameters for an aggregated query from startTime,
 * intervalSizeMs,numIntervals.
 * 
 * @author dave
 * 
 */
public class BucketQuery {

	private final Date startTime;
	private final double intervalSizeMs;
	private final long numIntervals;
	private final Optional<String> field;
	private final Optional<String> source;
	private final Optional<String> text;

	/**
	 * Constructor.
	 * 
	 * @param startTime
	 * @param intervalSizeMs
	 * @param numIntervals
	 *            if 0 then one interval used of size intervalSizeMs and all
	 *            points get their own bucket
	 * @param field
	 *            field to filter on
	 * @param source
	 *            source to filter on.
	 * @param optional
	 */
	public BucketQuery(Date startTime, double intervalSizeMs,
			long numIntervals, Optional<String> field, Optional<String> source,
			Optional<String> text) {
		Preconditions.checkNotNull(source,
				"source must not be null but can be Optional.absent()");
		Preconditions.checkNotNull(text,
				"text must not be null but can be Optional.absent()");
		Preconditions.checkNotNull(startTime, "startTime must not be null");
		Preconditions.checkNotNull(field,
				"field must not be null but can be Optional.absent()");
		this.startTime = startTime;
		this.intervalSizeMs = intervalSizeMs;
		this.numIntervals = numIntervals;
		this.field = field;
		this.source = source;
		this.text = text;
	}

	/**
	 * Constructor.
	 * 
	 * @param startTime
	 * @param intervalSizeMs
	 * @param numIntervals
	 *            if 0 then one interval used of size intervalSizeMs and all
	 *            points get their own bucket
	 * @param field
	 *            field to filter on
	 */
	public BucketQuery(Date startTime, double intervalSizeMs,
			long numIntervals, String field) {
		this(startTime, intervalSizeMs, numIntervals, Optional.of(field),
				Optional.<String> absent(), Optional.<String> absent());
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

	public long getFinishTime() {
		long n;
		if (numIntervals == 0)
			n = 1;
		else
			n = numIntervals;
		return Math.round(startTime.getTime() + n * intervalSizeMs);
	}

	public Optional<String> getField() {
		return field;
	}

	public Optional<String> getSource() {
		return source;
	}

	public Optional<String> getText() {
		return text;
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

}
