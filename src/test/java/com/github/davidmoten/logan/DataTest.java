package com.github.davidmoten.logan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.Map;

import org.junit.Test;

import com.google.common.collect.Maps;

public class DataTest {

	@Test
	public void testFindWithNoData() {
		Data d = new Data();
		assertEquals(0, d.getNumEntries());
		Iterator<LogEntry> it = d.find(0, 200).iterator();
		assertFalse(it.hasNext());
	}

	@Test
	public void testData() {
		Data d = new Data();
		Map<String, String> map = Maps.newHashMap();
		map.put("n", "123");
		d.add(new LogEntry(100L, map));
		assertEquals(1, d.getNumEntries());
		Iterator<LogEntry> it = d.find(0, 200).iterator();
		assertTrue(it.hasNext());
		assertEquals(100L, it.next().getTime());
		assertFalse(it.hasNext());
	}

	@Test
	public void testData2() {
		Data d = new Data();
		Map<String, String> map = Maps.newHashMap();
		map.put("n", "123");
		d.add(new LogEntry(-10L, map));
		d.add(new LogEntry(100L, map));
		d.add(new LogEntry(230L, map));
		assertEquals(3, d.getNumEntries());
		Iterator<LogEntry> it = d.find(0, 200).iterator();
		assertTrue(it.hasNext());
		assertEquals(100L, it.next().getTime());
		assertFalse(it.hasNext());
	}

}
