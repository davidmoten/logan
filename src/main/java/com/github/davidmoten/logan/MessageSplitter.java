package com.github.davidmoten.logan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Maps;

/**
 * Extracts key=value pairs from a log line message.
 * 
 */
public class MessageSplitter {

    public static final String MESSAGE_PATTERN_DEFAULT = "(\\b[a-zA-Z](?:\\w| )*)=([^;|,]*)(;|\\||,|$)";
    private final Pattern pattern;
    private final List<String> pairs;

    /**
     * Constructor.
     */
    public MessageSplitter() {
        this(Pattern.compile(MESSAGE_PATTERN_DEFAULT));
    }

    /**
     * Constructor.
     * 
     * @param pattern
     *            pattern
     */
    public MessageSplitter(Pattern pattern) {
        this.pattern = pattern;
        this.pairs = new ArrayList<>();
    }

    /**
     * Extracts key value pairs using regex pattern defined in the constructor.
     * 
     * @param s
     *            string to split
     * @return map of key value pairs
     */
    public Map<String, String> splitAsMap(String s) {
        Map<String, String> map = Maps.newHashMap();
        split(s);
        for (int i = 0; i < pairs.size(); i += 2) {
            map.put(pairs.get(i), pairs.get(i + 1));
        }
        return map;
    }

    // not concurrency safe
    // do this instead of creating a map to save allocations
    public List<String> split(String s) {
        pairs.clear();
        if (s != null && s.length() > 0) {
            Matcher matcher = pattern.matcher(s);
            while (matcher.find()) {
                String value = matcher.group(2).trim();
                pairs.add(matcher.group(1).trim());
                pairs.add(value);
            }
        }
        return pairs;
    }
}
