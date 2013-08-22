package com.github.davidmoten.logan.watcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.LogManager;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import com.github.davidmoten.logan.Data;
import com.github.davidmoten.logan.LogParser;
import com.github.davidmoten.logan.config.Configuration;
import com.github.davidmoten.logan.config.Group;
import com.github.davidmoten.logan.config.Log;
import com.google.common.collect.Lists;

public class WatcherTest {

	private static final String TEST_LOG = "target/test.log";

	/**
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws SecurityException
	 */
	@Test
	public void test() throws InterruptedException, SecurityException,
			IOException {
		setupLogging();

		List<Log> list = Lists.newArrayList();
		list.add(new Log(TEST_LOG, true));
		list.add(new Log(TEST_LOG, true));
		Configuration configuration = new Configuration();
		configuration.parser = TestingUtil.createDefaultParser();
		configuration.group.add(new Group(list));

		Data data = new Data();
		Watcher w = new Watcher(data, configuration);
		w.start();
		startWritingToFile(TEST_LOG);
		Thread.sleep(3000);
		w.stop();
	}

	@Test
	public void testSourcePatternWorksOk() {
		Pattern p = Pattern.compile("^[a-zA-Z][^\\.]*");
		Matcher m = p.matcher("test.log");
		assertTrue(m.find());
		assertEquals("test", m.group());
	}

	private void setupLogging() throws IOException {
		LogManager.getLogManager()
				.readConfiguration(
						WatcherTest.class
								.getResourceAsStream("/my-logging.properties"));
	}

	private void startWritingToFile(final String filename) {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					FileOutputStream fos = new FileOutputStream(filename);
					DateFormat df = new SimpleDateFormat(
							LogParser.DATE_FORMAT_DEFAULT);
					for (int i = 1; i <= 5; i++) {
						String line = df.format(new Date())
								+ " INFO org.moten.david.log.something - value="
								+ (System.currentTimeMillis() % 20) + "\n";
						fos.write(line.getBytes());
						fos.flush();
						Thread.sleep(100);
					}
					fos.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
		});
		t.start();
	}

	@Test
	public void testParseTimestampWithoutYear() throws ParseException {
		DateFormat df = new SimpleDateFormat("MMM dd HH:mm:ss");
		Date date = df.parse("Aug 22 14:36:51");
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		int year = calendar.get(Calendar.YEAR);
		calendar.setTime(date);
		if (calendar.get(Calendar.YEAR) == 1970) {
			calendar.set(Calendar.YEAR, year);
		}
		System.out.println(calendar.getTime());
	}

	private Object SimpleDateFormat(String string) {
		// TODO Auto-generated method stub
		return null;
	}
}
