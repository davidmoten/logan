package com.github.davidmoten.logan;

import java.util.Date;

import com.github.davidmoten.bplustree.LargeByteBuffer;
import com.github.davidmoten.bplustree.Serializer;
import com.github.davidmoten.jsmaz.Smaz;

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
            
            final double value;
            final String stringValue;
            
            if (isString == 0) {
                value = bb.getDouble();
                stringValue = null;
            } else {
                value = 0;
                stringValue = getString(bb);
            }
            long time = bb.getLong();
            return new PropertyWithTimestamp(key, value, stringValue, time);
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
                putString(bb, t.stringValue);
                bb.putLong(t.time);
            }
        }
        
        private String getString(LargeByteBuffer bb) {
            int length = bb.getInt();
            byte[] bytes = new byte[length];
            bb.get(bytes);
            return Smaz.decompress(bytes);
        }

        private void putString(LargeByteBuffer bb, String s) {
            byte[] bytes = Smaz.compress(s);
            bb.putInt(bytes.length);
            bb.put(bytes);
        }

        @Override
        public int maxSize() {
            return 0;
        }
    };

    @Override
    public String toString() {
        return "PropertyWithTimestamp [key=" + key + ", value="
                + (stringValue == null ? value : "\"" + stringValue + "\"") + ", time=" + new Date(time) + "]";
    }

}
