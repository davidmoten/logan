package com.github.davidmoten.logan;

import java.util.Date;

import com.github.davidmoten.bplustree.LargeByteBuffer;
import com.github.davidmoten.bplustree.Serializer;

public class PropertyWithTimestamp {

    final String key;
    final double value;
    final long time;
    final String stringValue;

    /**
     * If stringValue is not null then this is a string property (double value
     * ignored) otherwise this is a numeric (double) value.
     * 
     * @param key
     *            the property key
     * @param value
     *            the property value if numeric
     * @param stringValue
     *            the property value if non-numeric
     * @param time
     *            property timestamp
     */
    public PropertyWithTimestamp(String key, double value, String stringValue, long time) {
        this.key = key;
        this.value = value;
        this.stringValue = stringValue;
        this.time = time;
    }

    public static final Serializer<PropertyWithTimestamp> SERIALIZER = new Serializer<PropertyWithTimestamp>() {

        @Override
        public PropertyWithTimestamp read(LargeByteBuffer bb) {
            String key = bb.getString();
            byte isString = bb.get();
            if (isString == 0) {
                double value = bb.getDouble();
                long time = bb.getLong();
                return new PropertyWithTimestamp(key, value, null, time);
            } else {
                String stringValue = bb.getString();
                long time = bb.getLong();
                return new PropertyWithTimestamp(key, 0, stringValue, time);
            }
        }

        @Override
        public void write(LargeByteBuffer bb, PropertyWithTimestamp t) {
            bb.putString(t.key);
            if (t.stringValue == null) {
                bb.put((byte) 0);
                bb.putDouble(t.value);
                bb.putLong(t.time);
            } else {
                bb.put((byte) 1);
                bb.putString(t.stringValue);
                bb.putLong(t.time);
            }
        }

        @Override
        public int maxSize() {
            return 0;
        }
    };

    @Override
    public String toString() {
        return "PropertyWithTimestamp [key=" + key + ", value=" + (stringValue == null? value:"\"" + stringValue + "\"") + ", time=" + new Date(time) + "]";
    }

}
