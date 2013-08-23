package com.github.davidmoten.logan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.google.common.collect.Maps;

public class DataPersistedTest {

	@Test
	public void testCreateDatabase() {

		DataPersisted d = createData("db1");
		Map<String, String> map = Maps.newHashMap();
		map.put("n", "123");
		map.put(Field.MSG, "n=123, not finished yet");
		d.add(new LogEntry(100L, map));
		assertEquals(1, d.getNumEntries());
		d.close();
	}

	@Test
	public void testFindWithOneEntryBetweenRangeAndNoOtherEntries() {
		DataPersisted d = createData("db2");
		Map<String, String> map = Maps.newHashMap();
		map.put("n", "123");
		map.put(Field.MSG, "n=123, not finished yet");
		d.add(new LogEntry(100L, map));
		assertEquals(1, d.getNumEntries());
		Iterator<LogEntry> it = d.find(0, 200).iterator();
		assertTrue(it.hasNext());
		assertEquals(100L, it.next().getTime());
		assertFalse(it.hasNext());
	}

	@Test
	public void testFindWithThreeEntriesWhereOneEntryIsBetweenRange() {
		Data d = createData("db3");
		Map<String, String> map = Maps.newHashMap();
		map.put("n", "123");
		map.put(Field.MSG, "n=123, not finished yet");
		d.add(new LogEntry(-10L, map));
		d.add(new LogEntry(100L, map));
		d.add(new LogEntry(230L, map));
		assertEquals(3, d.getNumEntries());
		Iterator<LogEntry> it = d.find(0, 200).iterator();
		assertTrue(it.hasNext());
		assertEquals(100L, it.next().getTime());
		assertFalse(it.hasNext());
	}

	private static DataPersisted createData(String id) {
		File file = new File("target/" + id + "/db");
		try {
			FileUtils.deleteDirectory(new File("target/" + id));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		DataPersisted d = new DataPersisted(file);
		return d;
	}

}