package com.github.davidmoten.logan;

import java.util.Date;

import com.github.davidmoten.bplustree.LargeByteBuffer;
import com.github.davidmoten.bplustree.Serializer;

public final class IntWithTimestamp implements Comparable<IntWithTimestamp> {

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
        return "IntWithTimestamp [value=" + value + ", time=" + new Date(time) + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (time ^ (time >>> 32));
        result = prime * result + value;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        IntWithTimestamp other = (IntWithTimestamp) obj;
        if (time != other.time)
            return false;
        if (value != other.value)
            return false;
        return true;
    }

    @Override
    public int compareTo(IntWithTimestamp o) {
        int c = Integer.compare(value, o.value);
        if (c == 0) {
            return Long.compare(time, o.time);
        } else {
            return c;
        }
    }

}
