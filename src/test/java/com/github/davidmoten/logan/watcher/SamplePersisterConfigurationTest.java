package com.github.davidmoten.logan.watcher;

import static org.junit.Assert.assertNotNull;

import java.io.InputStream;

import org.junit.Test;

import com.github.davidmoten.logan.LogEntry;
import com.github.davidmoten.logan.LogParser;
import com.github.davidmoten.logan.LogParserOptions;
import com.github.davidmoten.logan.config.Configuration;
import com.github.davidmoten.logan.config.Marshaller;
import com.github.davidmoten.logan.config.Parser;

public class SamplePersisterConfigurationTest {

	@Test
	public void test() {
		InputStream is = SamplePersisterConfigurationTest.class
				.getResourceAsStream("/sample-persister-configuration.xml");
		Configuration configuration = new Marshaller().unmarshal(is);
		Parser p = configuration.parser;
		LogParserOptions options = LogParserOptions.load(p.pattern,
				p.patternGroups, p.messagePattern, p.timestampFormat,
				p.timezone, p.multiline);
		LogParser parser = new LogParser(options);
		// matches without a threadname
		LogEntry entry = parser
				.parse("test",
						"2013-02-05 00:00:03.421 INFO  au.gov.amsa.er.craft.tracking.actor.RootActor - fixes queue size = 33");
		assertNotNull(entry);
		// matches with a threadname
		entry = parser
				.parse("test",
						"2013-02-05 00:00:03.421 INFO  au.gov.amsa.er.craft.tracking.actor.RootActor threadName    - fixes queue size = 33");
		assertNotNull(entry);
	}

}
