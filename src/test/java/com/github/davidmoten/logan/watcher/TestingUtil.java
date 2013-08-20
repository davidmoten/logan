package com.github.davidmoten.logan.watcher;

import com.github.davidmoten.logan.MessageSplitter;
import com.github.davidmoten.logan.config.Parser;
import com.google.common.collect.Lists;

public class TestingUtil {

	static Parser createDefaultParser() {
		String pattern = "^(\\d\\d\\d\\d-\\d\\d-\\d\\d \\d\\d:\\d\\d:\\d\\d\\.\\d\\d\\d) +(\\S+) +(\\S+) +(\\S+)? ?- (.*)$";
		String patternGroups = "logTimestamp,logLevel,logLogger,threadName,logMsg";
		String timestampFormat = "yyyy-MM-dd HH:mm:ss.SSS";
		String timezone = "UTC";
		String sourcePattern = "^[a-zA-Z][^\\.]*";
		boolean multiline = false;
		return new Parser(pattern, patternGroups,
				MessageSplitter.MESSAGE_PATTERN_DEFAULT,
				Lists.newArrayList(timestampFormat), timezone, multiline,
				sourcePattern);
	}

}
