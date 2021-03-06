package com.github.davidmoten.logan.watcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import com.github.davidmoten.logan.LogEntry;
import com.github.davidmoten.logan.LogParser;
import com.github.davidmoten.logan.LogParserOptions;
import com.github.davidmoten.logan.config.Configuration;
import com.github.davidmoten.logan.config.Marshaller;
import com.github.davidmoten.logan.config.Parser;

public class SampleConfigurationTest {

	private static final String LINE_1 = "2013-02-05 00:00:03.421 INFO  au.gov.amsa.er.craft.tracking.actor.RootActor - fixes queue size = 33";

	@Test
	public void test() {
		LogParser parser = getParser();
		// matches without a threadname
		LogEntry entry = parser.parse("test", LINE_1);
		assertNotNull(entry);
		assertTrue(entry.getProperties().containsKey("fixes queue size"));
		// matches with a threadname
		entry = parser
				.parse("test",
						"2013-02-05 00:00:03.421 INFO  au.gov.amsa.er.craft.tracking.actor.RootActor threadName    - fixes queue size = 33");
		assertNotNull(entry);

		// check works with both forms of ISO8601 datetime format (with comma)
		entry = parser
				.parse("test",
						"2013-02-05 00:00:03,421 INFO  au.gov.amsa.er.craft.tracking.actor.RootActor threadName    - fixes queue size = 33");
		assertNotNull(entry);

	}

	private LogParser getParser() {
		InputStream is = SampleConfigurationTest.class
				.getResourceAsStream("/sample-configuration.xml");
		Configuration configuration = new Marshaller().unmarshal(is);
		Parser p = configuration.parser;
		LogParserOptions options = LogParserOptions.load(p.pattern,
				p.patternGroups, p.messagePattern, p.timestampFormat,
				p.timezone, p.multiline);
		LogParser parser = new LogParser(options);
		return parser;
	}

	@Test
	public void testPattern() {
		String p = "^(\\d\\d\\d\\d-\\d\\d-\\d\\d \\d\\d:\\d\\d:\\d\\d\\.\\d\\d\\d) +(\\S+) +(\\S+)(?: +(\\S+)?) *- (.*)$";
		Pattern pattern = Pattern.compile(p);
		Matcher m = pattern.matcher(LINE_1);
		assertTrue(m.find());
		for (int group = 1; group <= m.groupCount(); group++)
			System.out.println("group " + group + "=" + m.group(group));
		assertEquals("fixes queue size = 33", m.group(5));
	}

}
