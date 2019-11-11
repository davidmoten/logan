package com.github.davidmoten.logan;

import com.github.davidmoten.bplustree.LargeByteBuffer;
import com.github.davidmoten.bplustree.Serializer;

public final class IntWithTimestamp {

    final int value;
    final long time;

    public IntWithTimestamp(int value, long time) {
        this.value = value;
        this.time = time;
    }

    public static final Serializer<IntWithTimestamp> SERIALIZER = new Serializer<IntWithTimestamp>() {

        @Override
        public IntWithTimestamp read(LargeByteBuffer bb) {
            int value = bb.getInt();
            long time = bb.getLong();
            return new IntWithTimestamp(value, time);
        }

        @Override
        public void write(LargeByteBuffer bb, IntWithTimestamp t) {
            bb.putInt(t.value);
            bb.putLong(t.time);
        }

        @Override
        public int maxSize() {
            return Integer.BYTES + Long.BYTES;
        }
    };

    @Override
    public String toString() {
        return "IntWithTimestamp [value=" + value + ", time=" + time + "]";
    }
    
    
}
