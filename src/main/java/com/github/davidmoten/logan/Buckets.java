package com.github.davidmoten.logan;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * A group of contiguous buckets.
 * 
 * @author dave
 * 
 */
public class Buckets {

	private final List<Bucket> buckets = Lists.newArrayList();
	private final Bucket allBucket;
	private final BucketQuery query;

	/**
	 * Constructor.
	 * 
	 * @param query
	 */
	public Buckets(BucketQuery query) {
		this.query = query;
		for (int i = 0; i < query.getNumIntervals(); i++)
			buckets.add(new Bucket(query.getStartTime().getTime() + i
					* query.getIntervalSizeMs(), query.getIntervalSizeMs()));
		allBucket = new Bucket(query.getStartTime().getTime(),
				query.getIntervalSizeMs() * query.getNumIntervals());
	}

	/**
	 * Aggregates the given (timestamp,value) pair to the individual buckets and
	 * the all bucket.
	 * 
	 * @param timestamp
	 * @param value
	 */
	public void add(long timestamp, double value) {
		if (collate()) {
			int bucketIndex = (int) ((timestamp - query.getStartTime()
					.getTime()) / query.getIntervalSizeMs());
			if (bucketIndex < buckets.size()) {
				buckets.get(bucketIndex).add(timestamp, value);
				allBucket.add(timestamp, value);
			}
		} else {
			// no collation, each new pair gets a new Bucket
			Bucket bucket = new Bucket(timestamp);
			bucket.add(timestamp, value);
			buckets.add(bucket);
			allBucket.add(timestamp, value);
		}
	}

	private boolean collate() {
		return query.getNumIntervals() > 0;
	}

	public List<Bucket> getBuckets() {
		return buckets;
	}

	public Bucket getBucketForAll() {
		return allBucket;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Buckets [buckets=");
		builder.append(buckets);
		builder.append(", allBucket=");
		builder.append(allBucket);
		builder.append(", query=");
		builder.append(query);
		builder.append("]");
		return builder.toString();
	}

}
