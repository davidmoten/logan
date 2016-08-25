package com.github.davidmoten.logan;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import com.github.davidmoten.logan.config.Group;
import com.github.davidmoten.logan.config.Parser;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;

/**
 * Options for a {@link LogParser}.
 * 
 * @author dave
 * 
 */
public class LogParserOptions {

    private final Pattern pattern;
    private final BiMap<String, Integer> patternGroups;
    private final Pattern messagePattern;

    private final List<SimpleDateFormat> timestampFormat;
    private final String timezone;
    private final boolean multiline;

    /**
     * Constructor.
     * 
     * @param pattern
     *            pattern
     * @param patternGroups
     *            pattern groups
     * @param messagePattern
     *            message pattern
     * @param timestampFormat
     *            timestamp format
     * @param timezone
     *            timezone
     * @param multiline
     *            multiline
     */
    public LogParserOptions(Pattern pattern, BiMap<String, Integer> patternGroups,
            Pattern messagePattern, List<SimpleDateFormat> timestampFormat, String timezone,
            boolean multiline) {
        super();
        this.pattern = pattern;
        this.patternGroups = patternGroups;
        this.messagePattern = messagePattern;
        this.timestampFormat = timestampFormat;
        this.timezone = timezone;
        this.multiline = multiline;
    }

    /**
     * Constructor.
     * 
     * @param pattern
     *            pattern
     * @param patternGroups
     *            pattern groups
     * @param messagePattern
     *            message pattern
     * @param timestampFormat
     *            timestamp format
     * @param timezone
     *            timezone
     * @param multiline
     *            multiline
     */
    public LogParserOptions(Pattern pattern, BiMap<String, Integer> patternGroups,
            Pattern messagePattern, String timestampFormat, String timezone, boolean multiline) {
        this(pattern, patternGroups, messagePattern,
                Lists.newArrayList(createDateFormat(timestampFormat)), timezone, multiline);
    }

    private static LogParserOptions load(InputStream is) {
        Properties p = new Properties();
        try {
            p.load(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Pattern pattern = Pattern.compile(p.getProperty("pattern"));
        Pattern messagePattern = Pattern.compile(p.getProperty("message.pattern"));
        String timestampFormat = p.getProperty("timestamp.format");
        SimpleDateFormat df = createDateFormat(timestampFormat);
        String timezone = p.getProperty("timestamp.timezone");
        BiMap<String, Integer> patternGroups = createGroupMap(p.getProperty("pattern.groups"));
        boolean multiline = "true".equalsIgnoreCase(p.getProperty("multiline"));
        return new LogParserOptions(pattern, patternGroups, messagePattern, Lists.newArrayList(df),
                timezone, multiline);
    }

    private static SimpleDateFormat createDateFormat(String timestampFormat) {
        return new SimpleDateFormat(timestampFormat + " Z");
    }

    public static LogParserOptions load(String pPattern, String pPatternGroups,
            String pMessagePattern, List<String> pTimestampFormat, String pTimezone,
            boolean pMultiline) {
        Pattern pattern = Pattern.compile(pPattern);
        Pattern messagePattern = Pattern.compile(pMessagePattern);
        List<SimpleDateFormat> dfs = toDateFormats(pTimestampFormat);
        BiMap<String, Integer> patternGroups = createGroupMap(pPatternGroups);
        return new LogParserOptions(pattern, patternGroups, messagePattern, dfs, pTimezone,
                pMultiline);
    }

    private static List<SimpleDateFormat> toDateFormats(List<String> formats) {
        List<SimpleDateFormat> list = Lists.newArrayList();
        for (String format : formats)
            list.add(new SimpleDateFormat(format));
        return list;
    }

    /**
     * Returns {@link LogParserOptions} from properties in
     * /log-parser.properties on classpath.
     * 
     * @return options
     */
    public static LogParserOptions load() {
        return load(LogParserOptions.class.getResourceAsStream("/log-parser.properties"));
    }

    /**
     * Returns the log line high level {@link Pattern} (not including the regex
     * for extraction of key values from log message).
     * 
     * @return pattern
     */
    public Pattern getPattern() {
        return pattern;
    }

    /**
     * Returns the positions of standard log line properties in the pattern.
     * logTimestamp -&gt; 1 means that logTimestamp is group 1 in the regex
     * pattern.
     * 
     * @return pattern groups
     */
    public BiMap<String, Integer> getPatternGroups() {
        return patternGroups;
    }

    /**
     * Returns {@link DateFormat} in use once the timestap format is combined
     * with the timezone.
     * 
     * @return timestamp format
     */
    public List<SimpleDateFormat> getTimestampFormat() {
        return timestampFormat;
    }

    /**
     * Timezone for the log line timestamp part.
     * 
     * @return timezone
     */
    public String getTimezone() {
        return timezone;
    }

    /**
     * Returns true if and only if the logs are split over lines like for
     * instance default java.util.logging logs.
     * 
     * @return multiline
     */
    public boolean isMultiline() {
        return multiline;
    }

    /**
     * Returns the message regex {@link Pattern} used to extract keys and values
     * from the log line message.
     * 
     * @return message pattern
     */
    public Pattern getMessagePattern() {
        return messagePattern;
    }

    private static BiMap<String, Integer> createGroupMap(String list) {
        BiMap<String, Integer> map = HashBiMap.create(5);
        String[] items = list.split(",");
        for (int i = 0; i < items.length; i++)
            map.put(items[i], i + 1);
        return map;
    }

    public static LogParserOptions load(Parser defaultParser, Group group) {
        Parser parser = defaultParser;
        if (group.parser != null)
            parser = group.parser;

        return load(parser.pattern, parser.patternGroups, parser.messagePattern,
                parser.timestampFormat, parser.timezone, parser.multiline);
    }

}
