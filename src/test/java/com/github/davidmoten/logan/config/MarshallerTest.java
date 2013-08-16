package com.github.davidmoten.logan.config;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.davidmoten.logan.LogParserOptions;

public class MarshallerTest {

	private static final String PERSISTER_CONFIGURATION_TEST_XML = "/configuration-test.xml";

	@Test
	public void testMarshall() {
		Marshaller marshaller = new Marshaller();
		Configuration configuration = new Configuration();
		Group group = new Group();
		configuration.group.add(group);
		group.log
				.add(new Log("/home/dave/logs/app/tomcatlog4j.log\\..*", true));
		// TOOD use constructor
		Parser parser = new Parser();
		group.parser = parser;
		parser.pattern = "^(\\d\\d\\d\\d-\\d\\d-\\d\\d \\d\\d:\\d\\d:\\d\\d\\.\\d\\d\\d) +(\\S+) +(\\S+) +(\\S+)? ?- (.*)$";
		parser.timestampFormat = "yyyy-MM-dd HH:mm:ss.SSS";
		parser.patternGroups = "logTimestamp,logLevel,logLogger,threadName,logMsg";
		parser.timezone = "UTC";
		parser.multiline = false;
		marshaller.marshal(configuration, System.out);
	}

	@Test
	public void testUnmarshall() {
		Marshaller marshaller = new Marshaller();
		Configuration c = marshaller.unmarshal(MarshallerTest.class
				.getResourceAsStream(PERSISTER_CONFIGURATION_TEST_XML));
		assertEquals("UTC", c.group.get(0).parser.timezone);
		// get coverage of toString methods
		System.out.println(c);
	}

	@Test
	public void testLoadLogParserOptions() {
		Marshaller marshaller = new Marshaller();
		Configuration c = marshaller.unmarshal(MarshallerTest.class
				.getResourceAsStream(PERSISTER_CONFIGURATION_TEST_XML));
		LogParserOptions options = LogParserOptions.load(c.parser,
				c.group.get(0));
		assertEquals("UTC", options.getTimezone());
	}
}
