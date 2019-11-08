package com.github.davidmoten.logan;

import java.nio.charset.StandardCharsets;

import com.github.davidmoten.bplustree.LargeByteBuffer;
import com.github.davidmoten.bplustree.Serializer;

public class PropertyWithTimestamp implements Comparable<PropertyWithTimestamp> {

    final String key;
    final String value;
    final long time;

    public PropertyWithTimestamp(String key, String value, long time) {
        this.key = key;
        this.value = value;
        this.time = time;
    }

    public static final Serializer<PropertyWithTimestamp> SERIALIZER = new Serializer<PropertyWithTimestamp>() {

        @Override
        public PropertyWithTimestamp read(LargeByteBuffer bb) {
            String key = Serializer.readString(bb);
            String value = Serializer.readString(bb);
            long time = bb.getLong();
            return new PropertyWithTimestamp(key, value, time);
        }

        @Override
        public void write(LargeByteBuffer bb, PropertyWithTimestamp t) {
            Serializer.writeString(bb, t.key);
            Serializer.writeString(bb, t.value);
            bb.putLong(t.time);
        }

        @Override
        public int maxSize() {
            return 0;
        }
    };

    @Override
    public int compareTo(PropertyWithTimestamp o) {
        int a = this.value.compareTo(o.value);
        if (a == 0) {
            return Long.compare(this.time, o.time);
        } else {
            return a;
        }
    }

}
