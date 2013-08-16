package com.github.davidmoten.logan.watcher;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.LogManager;

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
}