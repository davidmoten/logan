package com.github.davidmoten.logan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.concurrent.Executors;

import org.junit.Test;

public class LogFileTest {

	@Test
	public void testTailingAFilePutsRecordsIntoDatabase()
			throws InterruptedException {
		Data data = new DataMemory();
		assertEquals(0, data.getNumEntries());
		LogFile log = new LogFile(new File("src/test/resources/test.log"),
				"testing", 300, new LogParser(LogParserOptions.load()),
				Executors.newFixedThreadPool(3));
		log.tail(data, true);
		Thread.sleep(1000);
		log.stop();
		long numEntries = data.getNumEntries();
		System.out.println(numEntries);
		assertTrue(numEntries > 0);
	}

	@Test
	public void testCreateIfDoesntExist() {
		File file = new File("target/temp" + System.currentTimeMillis());
		LogFile.createFileIfDoesntExist(file);
		assertTrue(file.exists());
	}

	@Test
	public void testCallingStopWhenNotStartedDoesNotThrowException() {
		LogFile log = new LogFile(new File("src/test/resources/test.log"),
				"testing", 300, new LogParser(LogParserOptions.load()),
				Executors.newFixedThreadPool(3));
		log.stop();

	}
}
