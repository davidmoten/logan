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

	private final long startTime;
	private final double intervalSizeMs;
	private final long numIntervals;
	private final Optional<String> field;
	private final Optional<String> source;
	private final Optional<String> text;
	private final Optional<Integer> scan;
	private final Optional<String> delimiterPattern;

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
	 * @param text
	 * @param scan
	 *            the 1 based index of the double after a text field to find
	 * 
	 */
	public BucketQuery(long startTime, double intervalSizeMs,
			long numIntervals, Optional<String> field, Optional<String> source,
			Optional<String> text, Optional<Integer> scan,
			Optional<String> delimiterPattern) {
		Preconditions.checkNotNull(source,
				"source must not be null but can be Optional.absent()");
		Preconditions.checkNotNull(text,
				"text must not be null but can be Optional.absent()");
		Preconditions.checkNotNull(startTime, "startTime must not be null");
		Preconditions.checkNotNull(field,
				"field must not be null but can be Optional.absent()");
		Preconditions.checkNotNull(scan,
				"scan must not be null but can be Optional.absent()");
		Preconditions.checkArgument(!(scan.isPresent() && field.isPresent()),
				"if scan is specified then field must not be specified");
		Preconditions.checkArgument(!(scan.isPresent() && scan.get() <= 0),
				"scan must be >0");
		Preconditions
				.checkNotNull(delimiterPattern,
						"delimiterPattern must not be null but can be Optional.absent()");

		this.startTime = startTime;
		this.intervalSizeMs = intervalSizeMs;
		this.numIntervals = numIntervals;
		this.field = field;
		this.source = source;
		this.text = text;
		this.scan = scan;
		this.delimiterPattern = delimiterPattern;

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
		this(startTime.getTime(), intervalSizeMs, numIntervals, Optional
				.of(field), Optional.<String> absent(), Optional
				.<String> absent(), Optional.<Integer> absent(), Optional
				.<String> absent());
	}

	/**
	 * Returns the start time.
	 * 
	 * @return
	 */
	public long getStartTime() {
		return startTime;
	}

	/**
	 * Returns the size of the interval in millis. If numIntervals is 0 then
	 * this is the full extent of the time range and a bucket is created for
	 * every point.
	 * 
	 * @return
	 */
	public double getIntervalSizeMs() {
		return intervalSizeMs;
	}

	/**
	 * Returns the number of intervals. If 0 then this query corresponds to
	 * using a separate bucket for each data point.
	 * 
	 * @return
	 */
	public long getNumIntervals() {
		return numIntervals;
	}

	/**
	 * Returns true if and only if aggregation is requested for this query.
	 * 
	 * @return
	 */
	public boolean performAggregation() {
		return numIntervals > 0;
	}

	/**
	 * Returns finish time for the query.
	 * 
	 * @return
	 */
	public long getFinishTime() {
		long n;
		if (numIntervals == 0)
			n = 1;
		else
			n = numIntervals;
		return Math.round(startTime + n * intervalSizeMs);
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

	public Optional<Integer> getScan() {
		return scan;
	}

	public Optional<String> getDelimiterPattern() {
		return delimiterPattern;
	}

	@Override
	public String toString() {
		return "BucketQuery [startTime=" + startTime + ", intervalSizeMs="
				+ intervalSizeMs + ", numIntervals=" + numIntervals
				+ ", field=" + field + ", source=" + source + ", text=" + text
				+ ", scan=" + scan + "]";
	}

}
