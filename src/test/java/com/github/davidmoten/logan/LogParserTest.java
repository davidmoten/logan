package com.github.davidmoten.logan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.regex.Pattern;

import org.junit.Test;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.io.LineReader;

public class LogParserTest {

	@Test
	public void testParseLine() {
		String line = "2012-11-29 04:39:02.941   INFO  au.gov.amsa.er.craft.tracking.actor.FixesPersisterActor - fixes queue size = 0";
		LogParser p = new LogParser();
		LogEntry entry = p.parse("test", line);
		assertNotNull(entry);
		assertEquals(1354163942941L, entry.getTime());
		assertEquals("INFO", entry.getProperties().get(Field.LEVEL));
		assertEquals("au.gov.amsa.er.craft.tracking.actor.FixesPersisterActor",
				entry.getProperties().get(Field.LOGGER));
		assertEquals(line,
				entry.getProperties().get(Field.MSG));
	}

	/**
	 * Tests the multiline use case.
	 */
	@Test
	public void testParseUtilLoggingLines() {
		String line1 = "23/12/2012 6:58:04 AM org.moten.david.log.core.Database persistDummyRecords";
		String line2 = "INFO: persisted random values=1000 from the last hour to table Dummy";
		String line3 = "some junk";
		String line4 = "23/12/2012 7:00:08 AM org.moten.david.log.core.DatabaseThing persistDummyRecordsAgain";
		String line5 = "DEBUG: something=123";

		Pattern pattern = Pattern
				.compile("^(\\d\\d/\\d\\d/\\d\\d\\d\\d \\d\\d?:\\d\\d:\\d\\d (?:(?:AM)|(?:PM))) +(\\S+) +(\\S+)ZZZ(\\S+): (.*)$");
		Pattern messagePattern = Pattern
				.compile(MessageSplitter.MESSAGE_PATTERN_DEFAULT);
		String format = "dd/MM/yyyy hh:mm:ss a";
		BiMap<String, Integer> map = HashBiMap.create(5);
		map.put(Field.TIMESTAMP, 1);
		map.put(Field.LOGGER, 2);
		map.put(Field.METHOD, 3);
		map.put(Field.LEVEL, 4);
		map.put(Field.MSG, 5);

		LogParserOptions options = new LogParserOptions(pattern, map,
				messagePattern, format, "UTC", true);
		LogParser p = new LogParser(options);
		assertNull(p.parse("test", line1));
		{
			LogEntry entry = p.parse("test", line2);
			assertNotNull(entry);
			assertEquals(1356245884000L, entry.getTime());
			assertEquals("INFO", entry.getProperties().get(Field.LEVEL));
			assertEquals("org.moten.david.log.core.Database", entry
					.getProperties().get(Field.LOGGER));
			assertEquals("persistDummyRecords",
					entry.getProperties().get(Field.METHOD));
			assertEquals(
					line2,
					entry.getProperties().get(Field.MSG));
		}
		assertNull(p.parse("test", line3));
		assertNull(p.parse("test", line4));
		{
			LogEntry entry = p.parse("test", line5);
			assertNotNull(entry);
			assertEquals(1356246008000L, entry.getTime());
			assertEquals("DEBUG", entry.getProperties().get(Field.LEVEL));
			assertEquals("org.moten.david.log.core.DatabaseThing", entry
					.getProperties().get(Field.LOGGER));
			assertEquals("persistDummyRecordsAgain",
					entry.getProperties().get(Field.METHOD));
			assertEquals(line5, entry.getProperties().get(Field.MSG));

		}
	}

	@Test
	public void testParseLineWithThreadName() {
		String line = "2012-11-29 04:39:02.941   INFO  au.gov.amsa.er.craft.tracking.actor.FixesPersisterActor thread_name-1 - fixes queue size = 0";
		LogParser p = new LogParser();
		LogEntry entry = p.parse("test", line);
		assertNotNull(entry);
		assertEquals("INFO", entry.getProperties().get(Field.LEVEL));
		assertEquals("au.gov.amsa.er.craft.tracking.actor.FixesPersisterActor",
				entry.getProperties().get(Field.LOGGER));
		assertEquals(line,
				entry.getProperties().get(Field.MSG));
		assertEquals("thread_name-1",
				entry.getProperties().get(Field.THREAD_NAME));
	}

	@Test
	public void testParseNullLineReturnsNull() {
		LogParser p = new LogParser();
		LogEntry entry = p.parse("test", null);
		assertNull(entry);
	}

	@Test
	public void testParseMultipleLines() throws IOException {
		LogParser p = new LogParser();
		LineReader reader = new LineReader(new InputStreamReader(
				LogParserTest.class.getResourceAsStream("/test.log")));
		String line;
		MessageSplitter splitter = new MessageSplitter();
		while ((line = reader.readLine()) != null) {
			System.out.println(line);
			LogEntry entry = p.parse("test", line);
			if (entry != null) {
				Map<String, String> map = splitter.splitAsMap(entry.getProperties()
						.get(Field.MSG));
				if (map.size() > 0)
					System.out.println(map);
			}
		}
	}

	@Test
	public void testParseLineWithComma() {
		String line = "2013-08-20 07:20:10,228 INFO  au.gov.amsa.watch.WatchTask - checked 66 in 10219ms, rate=6.5 watches/s";
		LogParser p = new LogParser();
		LogEntry entry = p.parse("temp", line);
		System.out.println(entry);
	}

	@Test
	public void testParseLineWithSpacePaddedDayFromSyslog() {
		String line = "Aug  3 03:19:02 sarcnode sendmail[12325]: [ID 801593 mail.info] r72DY20O010335: to=root, delay=03:45:00, xdelay=00:00:00, mailer=relay, pri=1381565, relay=[127.0.0.1] [127.0.0.1], dsn=4.0.0, stat=Deferred: Connection refused by [127.0.0.1]";

		Pattern pattern = Pattern
				.compile("^(\\w\\w\\w\\s+\\d+ \\d\\d:\\d\\d:\\d\\d) (\\S+) ([^\\[]+)\\[(\\d+)\\]: (.*)$");
		assertTrue(pattern.matcher(line).matches());

		Pattern messagePattern = Pattern
				.compile(MessageSplitter.MESSAGE_PATTERN_DEFAULT);
		String format = "MMM d HH:mm:ss";
		BiMap<String, Integer> map = HashBiMap.create(5);
		map.put(Field.TIMESTAMP, 1);
		map.put("hostname", 2);
		map.put(Field.LOGGER, 3);
		map.put("pid", 4);
		map.put(Field.MSG, 5);

		LogParserOptions options = new LogParserOptions(pattern, map,
				messagePattern, format, "UTC", false);
		LogParser p = new LogParser(options);
		LogEntry entry = p.parse("syslog", line);
		assertNotNull(entry);
		System.out.println(entry);
		assertEquals("sendmail", entry.getProperties().get("logLogger"));

	}

	@Test
	public void testParseGlassfishLog() {
		String line = "[2014-11-20T22:37:30.319+0000] [glassfish 4.0] [INFO] [] [au.gov.amsa.util.messaging.jms.HistoryUtil] [tid: _ThreadID=524 _ThreadName=p: thread-pool-1; w: 7] [timeMillis: 1416523050319] [levelValue: 800] contentHash=ed852bd0~file moved to history directory n0937270.002";

		String regex = "^\\[(\\d\\d\\d\\d-\\d\\d-\\d\\dT\\d\\d:\\d\\d:\\d\\d[\\.,]\\d\\d\\d)\\+\\d\\d\\d\\d\\] +\\[[^\\]]*\\] +\\[(\\S+)\\] +\\[[^\\]]*\\] +\\[([^\\]]*)\\] +\\[tid: (\\S+)[^\\]]*\\] \\[[^\\]]*\\] \\[[^\\]]*\\] (.*)$";
		Pattern pattern = Pattern.compile(regex);
		System.out.println("glassfish pattern=" + regex);

		assertTrue(pattern.matcher(line).matches());

		Pattern messagePattern = Pattern
				.compile(MessageSplitter.MESSAGE_PATTERN_DEFAULT);
		String format = "yyyy-MM-dd'T'HH:mm:ss.SSS";
		BiMap<String, Integer> map = HashBiMap.create(5);
		map.put(Field.TIMESTAMP, 1);
		map.put(Field.LEVEL, 2);
		map.put(Field.LOGGER, 3);
		map.put(Field.THREAD_NAME, 4);
		map.put(Field.MSG, 5);

		LogParserOptions options = new LogParserOptions(pattern, map,
				messagePattern, format, "UTC", false);
		LogParser p = new LogParser(options);
		LogEntry entry = p.parse("glassfish", line);
		assertNotNull(entry);
		System.out.println(entry);
		assertEquals("INFO", entry.getProperties().get("logLevel"));
		assertEquals("glassfish", entry.getProperties().get("logSource"));
		assertEquals("au.gov.amsa.util.messaging.jms.HistoryUtil", entry
				.getProperties().get("logLogger"));
		assertEquals("_ThreadID=524", entry.getProperties().get("threadName"));
	}
}
