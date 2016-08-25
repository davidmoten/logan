package com.github.davidmoten.logan;

import java.util.Date;
import java.util.Map;

/**
 * Encapsulates a parsed log line.
 * 
 */
public class LogEntry {

    private final long time;
    private final Map<String, String> properties;

    /**
     * Constructor.
     * 
     * @param time
     *            time
     * @param properties
     *            properties
     */
    public LogEntry(long time, Map<String, String> properties) {
        this.time = time;
        this.properties = properties;
    }

    /**
     * Returns the time in epoch ms for the log entry.
     * 
     * @return time in epoch ms for the log entry
     */
    public long getTime() {
        return time;
    }

    /**
     * Returns the parsed properties of the log line not including the parsing
     * of the log message.
     * 
     * @return a map of properties of the log line
     */
    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("LogEntry [time=");
        builder.append(new Date(time));
        builder.append(", properties=");
        builder.append(properties);
        builder.append("]");
        return builder.toString();
    }

    public String getSource() {
        return properties.get(Field.SOURCE);
    }

    public String getMsg() {
        return properties.get(Field.MSG);
    }

}
