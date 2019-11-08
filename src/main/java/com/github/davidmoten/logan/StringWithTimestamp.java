package com.github.davidmoten.logan;

import java.nio.charset.StandardCharsets;
import java.util.Comparator;

import com.github.davidmoten.bplustree.LargeByteBuffer;
import com.github.davidmoten.bplustree.Serializer;

public class StringWithTimestamp implements Comparable<StringWithTimestamp> {

    final String value;
    final long time;

    public StringWithTimestamp(String value, long time) {
        this.value = value;
        this.time = time;
    }

    public static final Serializer<StringWithTimestamp> SERIALIZER = new Serializer<StringWithTimestamp>() {

        @Override
        public StringWithTimestamp read(LargeByteBuffer bb) {
            int length = bb.getInt();
            byte[] bytes = new byte[length];
            bb.get(bytes);
            String s = new String(bytes, StandardCharsets.UTF_8);
            long time = bb.getLong();
            return new StringWithTimestamp(s, time);
        }

        @Override
        public void write(LargeByteBuffer bb, StringWithTimestamp t) {
            byte[] bytes = t.value.getBytes(StandardCharsets.UTF_8);
            bb.putInt(bytes.length);
            bb.put(bytes);
            bb.putLong(t.time);
        }

        @Override
        public int maxSize() {
            return 128;
        }
    };

    @Override
    public int compareTo(StringWithTimestamp o) {
        int a = this.value.compareTo(o.value);
        if (a == 0) {
            return Long.compare(this.time, o.time);
        } else {
            return a;
        }
    }

}
