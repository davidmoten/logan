package com.github.davidmoten.logan;

import com.github.davidmoten.bplustree.LargeByteBuffer;
import com.github.davidmoten.bplustree.Serializer;

public class PropertyWithTimestamp implements Comparable<PropertyWithTimestamp> {

    final String key;
    final double value;
    final long time;

    public PropertyWithTimestamp(String key, double value, long time) {
        this.key = key;
        this.value = value;
        this.time = time;
    }

    public static final Serializer<PropertyWithTimestamp> SERIALIZER = new Serializer<PropertyWithTimestamp>() {

        @Override
        public PropertyWithTimestamp read(LargeByteBuffer bb) {
            String key = bb.getString();
            double value = bb.getDouble();
            long time = bb.getLong();
            return new PropertyWithTimestamp(key, value, time);
        }

        @Override
        public void write(LargeByteBuffer bb, PropertyWithTimestamp t) {
            bb.putString(t.key);
            bb.putDouble(t.value);
            bb.putLong(t.time);
        }

        @Override
        public int maxSize() {
            return 0;
        }
    };

    @Override
    public int compareTo(PropertyWithTimestamp o) {
        int a = Double.compare(value, o.value);
        if (a == 0) {
            return Long.compare(this.time, o.time);
        } else {
            return a;
        }
    }

    @Override
    public String toString() {
        return "PropertyWithTimestamp [key=" + key + ", value=" + value + ", time=" + time + "]";
    }

}
