package com.github.davidmoten.logan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class DataPersistedTest {

	private static int counter = 1;

	@Test
	public void testCreateDatabase() {
		DataPersisted d = createData();
		Map<String, String> map = Maps.newHashMap();
		map.put("n", "123");
		map.put(Field.MSG, "n=123, not finished yet");
		d.add(new LogEntry(100L, map));
		assertEquals(1, d.getNumEntries());
		d.close();
	}

	@Test
	public void testInsertMany() {
		DataPersisted d = createData();
		Map<String, String> map = Maps.newHashMap();
		map.put("n", "123");
		map.put(Field.MSG, "n=123, not finished yet");
		for (int i = 0; i < 1000; i++)
			d.add(new LogEntry(100L, map));
		d.close();
	}

	@Test
	public void testFindWithOneEntryBetweenRangeAndNoOtherEntries() {
		DataPersisted d = createData();
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
		Data d = createData();
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

	@Test
	public void testGetKeys() {
		DataPersisted d = createData();
		Map<String, String> map = Maps.newHashMap();
		map.put("n", "123");
		map.put(Field.MSG, "n=123, not finished yet");
		d.add(new LogEntry(100L, map));
		assertEquals(Sets.newHashSet("n", Field.MSG), d.getKeys());
	}

	@Test
	public void testGetSources() {
		DataPersisted d = createData();
		Map<String, String> map = Maps.newHashMap();
		map.put("n", "123");
		map.put(Field.MSG, "n=123, not finished yet");
		map.put(Field.SOURCE, "src");
		d.add(new LogEntry(100L, map));
		assertEquals(Sets.newHashSet("src"), d.getSources());
	}

	@Test
	public void testOldestTime() {
		DataPersisted d = createData();
		Map<String, String> map = Maps.newHashMap();
		map.put("n", "123");
		map.put(Field.MSG, "n=123, not finished yet");
		map.put(Field.SOURCE, "src");
		d.add(new LogEntry(100L, map));
		assertEquals(new Date(100L), d.oldestTime());
	}

	private static DataPersisted createData() {
		int id = counter++;
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