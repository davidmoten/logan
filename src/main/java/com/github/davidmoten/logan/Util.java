package com.github.davidmoten.logan;

import java.io.PrintWriter;
import java.util.logging.Logger;

/**
 * Utility methods for queries/buckets.
 * 
 */
public class Util {

    private static Logger log = Logger.getLogger(Util.class.getName());

    public static String toJson(Buckets buckets, Metric metric) {
        StringBuilder s = new StringBuilder();
        for (Bucket bucket : buckets.getBuckets()) {
            Double value = bucket.get(metric);
            if (value != null) {
                if (s.length() > 0)
                    s.append(",");
                add(s, bucket, value);
            }
        }
        s.insert(0, "{ \"data\": [");
        s.append("]");
        StringBuilder s2 = new StringBuilder();
        Bucket b = buckets.getBucketForAll();
        for (Metric m : Metric.values()) {
            try {
                Double value = b.get(m);
                if (s2.length() > 0)
                    s2.append(",\n\t");
                s2.append("\t\"" + m + "\": " + value);
            } catch (RuntimeException e) {
                // ignore unimplemented metric
            }
        }
        s.append(",\n");
        s.append("\"stats\": {\n");
        s.append(s2);
        s.append("    }\n");
        s.append("}");
        return s.toString();
    }

    public static void writeJson(Buckets buckets, Metric metric, PrintWriter writer) {
        writer.print("{ \"data\": [");
        boolean start = true;
        for (Bucket bucket : buckets.getBuckets()) {
            Double value = bucket.get(metric);
            if (value != null) {
                if (!start)
                    writer.print(",");
                else
                    start = false;
                add(writer, bucket, value);
            }
        }
        writer.print("]");
        StringBuilder s2 = new StringBuilder();
        Bucket b = buckets.getBucketForAll();
        for (Metric m : Metric.values()) {
            try {
                Double value = b.get(m);
                if (s2.length() > 0)
                    s2.append(",\n\t");
                s2.append("\t\"" + m + "\": " + value);
            } catch (RuntimeException e) {
                // ignore unimplemented metric
            }
        }
        writer.println(",");
        writer.println("\"stats\": {");
        writer.print(s2);
        writer.println("    }");
        writer.print("}");
    }

    private static void add(StringBuilder s, Bucket bucket, Double value) {
        s.append('[');
        s.append(bucket.getStart());
        s.append(',');
        s.append(value);
        s.append(']');
    }

    private static void add(PrintWriter w, Bucket bucket, Double value) {
        w.print('[');
        w.print(bucket.getStart());
        w.print(',');
        w.print(value);
        w.print(']');
    }

    public static void addDummyData(Data data) {
        int n;
        if (System.getProperty("n") != null)
            n = Integer.parseInt(System.getProperty("n"));
        else
            n = 1000;
        long t = System.currentTimeMillis();
        DataHelper.addRandomLogEntry(data, 100, n);
        double ratePerSecond = n * 1000.0 / (System.currentTimeMillis() - t);
        log.info("added " + n + " records " + ", insertsPerSecond=" + ratePerSecond + "");
    }

    public static boolean isNumeric(String s) {
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static Double parseDouble(String s) {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
