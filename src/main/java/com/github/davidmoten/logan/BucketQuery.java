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
	private final Optional<Integer> scan;

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
	public BucketQuery(Date startTime, double intervalSizeMs,
			long numIntervals, Optional<String> field, Optional<String> source,
			Optional<String> text, Optional<Integer> scan) {
		Preconditions.checkNotNull(source,
				"source must not be null but can be Optional.absent()");
		Preconditions.checkNotNull(text,
				"text must not be null but can be Optional.absent()");
		Preconditions.checkNotNull(startTime, "startTime must not be null");
		Preconditions.checkNotNull(field,
				"field must not be null but can be Optional.absent()");
		Preconditions.checkNotNull(scan,
				"scan must not be null but can be Optional.absent()");
		Preconditions.checkArgument(!scan.isPresent() || scan.isPresent()
				&& text.isPresent(),
				"if scan is specified then text must be specified");
		Preconditions.checkArgument(!scan.isPresent() || scan.isPresent()
				&& !field.isPresent(),
				"if scan is specified then field must not be specified");

		this.startTime = startTime;
		this.intervalSizeMs = intervalSizeMs;
		this.numIntervals = numIntervals;
		this.field = field;
		this.source = source;
		this.text = text;
		this.scan = scan;
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
				Optional.<String> absent(), Optional.<String> absent(),
				Optional.<Integer> absent());
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

	public Optional<Integer> getScan() {
		return scan;
	}

	@Override
	public String toString() {
		return "BucketQuery [startTime=" + startTime + ", intervalSizeMs="
				+ intervalSizeMs + ", numIntervals=" + numIntervals
				+ ", field=" + field + ", source=" + source + ", text=" + text
				+ ", scan=" + scan + "]";
	}

}
