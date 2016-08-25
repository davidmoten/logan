package com.github.davidmoten.logan;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * A group of contiguous buckets.
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
     *            query
     */
    public Buckets(BucketQuery query) {
        this.query = query;
        for (int i = 0; i < query.getNumIntervals(); i++)
            buckets.add(new Bucket(query.getStartTime() + i * query.getIntervalSizeMs(),
                    query.getIntervalSizeMs()));
        allBucket = new Bucket(query.getStartTime(),
                query.getIntervalSizeMs() * query.getNumIntervals());
    }

    /**
     * Aggregates the given (timestamp,value) pair to the individual buckets and
     * the all bucket.
     * 
     * @param timestamp
     *            timestamp
     * @param value
     *            value
     */
    public void add(long timestamp, double value) {
        if (query.performAggregation()) {
            int bucketIndex = (int) ((timestamp - query.getStartTime())
                    / query.getIntervalSizeMs());
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
