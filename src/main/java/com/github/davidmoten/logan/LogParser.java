package com.github.davidmoten.logan;

import static com.github.davidmoten.logan.Field.LEVEL;
import static com.github.davidmoten.logan.Field.LOGGER;
import static com.github.davidmoten.logan.Field.METHOD;
import static com.github.davidmoten.logan.Field.MSG;
import static com.github.davidmoten.logan.Field.THREAD_NAME;
import static com.github.davidmoten.logan.Field.TIMESTAMP;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

import com.google.common.collect.BiMap;
import com.google.common.collect.Maps;

/**
 * Parses log lines. Handles multiline logs by buffering previous line then
 * checking for a pattern match against the concatenation of the previous and
 * current line.
 * 
 */
public class LogParser {

    public static final String DATE_FORMAT_DEFAULT = "yyyy-MM-dd HH:mm:ss.SSS";
    private final LogParserOptions options;
    private String previousLine;
    private final MessageSplitter splitter;

    /**
     * Constructor.
     * 
     * @param options
     *            options
     */
    public LogParser(LogParserOptions options) {
        this.options = options;
        splitter = new MessageSplitter(options.getMessagePattern());
    }

    /**
     * Constructor loads from /log-parser.properties on classpath.
     */
    public LogParser() {
        this(LogParserOptions.load());
    }

    /**
     * Returns the parsed line as a {@link LogEntry}. Note that this method is
     * synchronized because the {@link DateFormat} object used by it is not
     * thread-safe. However, you can safely instantiate multiple
     * {@link LogParser} objects and use them concurrently.
     * 
     * @param source
     *            source
     * @param line
     *            line
     * @return parsed LogEntry
     */
    public synchronized LogEntry parse(String source, String line) {
        if (line == null)
            return null;
        else {
            if (options.isMultiline() && (previousLine == null)) {
                previousLine = line;
                return null;
            } else {
                StringBuilder candidate = new StringBuilder(line);
                if (options.isMultiline()) {
                    candidate.insert(0, "ZZZ");
                    candidate.insert(0, previousLine);
                }
                Matcher matcher = options.getPattern().matcher(candidate);
                if (matcher.find()) {
                    previousLine = null;
                    return createLogEntry(source, matcher);
                } else {
                    previousLine = line;
                    return null;
                }
            }
        }
    }

    private LogEntry createLogEntry(String source, Matcher matcher) {
        BiMap<String, Integer> map = options.getPatternGroups();

        String timestamp = getGroup(matcher, map.get(TIMESTAMP));
        String level = getGroup(matcher, map.get(LEVEL));
        String logger = getGroup(matcher, map.get(LOGGER));
        String threadName = getGroup(matcher, map.get(THREAD_NAME));
        String msg = getGroup(matcher, map.get(MSG));
        String method = getGroup(matcher, map.get(METHOD));

        Long time = parseTime(timestamp);

        Map<String, String> values = getValues(level, logger, threadName, msg, method);
        // persist the split fields from the full message
        Map<String, String> m = splitter.split(msg);
        values.putAll(m);
        values.put(Field.SOURCE, source);
        return new LogEntry(time, values);
    }

    private Long parseTime(String timestamp) {
        Long time;
        for (SimpleDateFormat df : options.getTimestampFormat())
            try {
                StringBuilder s = new StringBuilder(timestamp);
                s.append(" ");
                s.append(options.getTimezone());
                time = df.parse(s.toString()).getTime();

                if (!df.toPattern().contains("yy")) {
                    // set year to most recent possible
                    Calendar cal = Calendar
                            .getInstance(TimeZone.getTimeZone(options.getTimezone()));
                    int year = cal.get(Calendar.YEAR);
                    cal.setTimeInMillis(time);
                    cal.set(Calendar.YEAR, year);
                    // if in future then make it last year
                    if (cal.getTimeInMillis() > System.currentTimeMillis()
                            + TimeUnit.DAYS.toMillis(1)) {
                        cal.set(Calendar.YEAR, year - 1);
                    }
                    time = cal.getTimeInMillis();
                }
                return time;
            } catch (ParseException e) {
            }
        return null;
    }

    private Map<String, String> getValues(String level, String logger, String threadName,
            String msg, String method) {
        Map<String, String> values = Maps.newHashMap();
        if (level != null)
            values.put(LEVEL, level);
        if (logger != null)
            values.put(LOGGER, logger);
        if (msg != null)
            values.put(MSG, msg);
        if (threadName != null && threadName.length() > 0)
            values.put(THREAD_NAME, threadName);
        if (method != null && method.length() > 0)
            values.put(METHOD, method);
        return values;
    }

    private String getGroup(Matcher matcher, Integer index) {
        if (index == null)
            return null;
        return matcher.group(index);
    }

}
