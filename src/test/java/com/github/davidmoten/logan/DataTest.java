package com.github.davidmoten.logan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;

public class DataTest {

	private static final double PRECISION = 0.00001;

	@Test
	public void testFindWithNoData() {
		Data d = new Data();
		assertEquals(0, d.getNumEntries());
		Iterator<LogEntry> it = d.find(0, 200).iterator();
		assertFalse(it.hasNext());
	}

	@Test
	public void testFindWithOneEntryBetweenRangeAndNoOtherEntries() {
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
	public void testFindWithThreeEntriesWhereOneEntryIsBetweenRange() {
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

	@Test
	public void testFindWithNoDataNonAggregated() {
		Data d = new Data();
		Map<String, String> map = Maps.newHashMap();
		map.put("n", "123");
		d.add(new LogEntry(100L, map));
		assertEquals(1, d.getNumEntries());
		{
			BucketQuery q = new BucketQuery(new java.util.Date(0), 101, 0, "n");
			Buckets buckets = d.execute(q);
			assertEquals(1, buckets.getBuckets().size());
		}
		{
			BucketQuery q = new BucketQuery(new java.util.Date(0), 99, 0, "n");
			Buckets buckets = d.execute(q);
			assertEquals(0, buckets.getBuckets().size());
		}
		{
			BucketQuery q = new BucketQuery(new java.util.Date(101), 100, 0,
					"n");
			Buckets buckets = d.execute(q);
			assertEquals(0, buckets.getBuckets().size());
		}
		{
			BucketQuery q = new BucketQuery(new java.util.Date(99), 2, 0, "n");
			Buckets buckets = d.execute(q);
			assertEquals(1, buckets.getBuckets().size());
		}

	}

	@Test
	public void testFindUsingScan() {
		Data d = new Data();
		Map<String, String> map = Maps.newHashMap();
		map.put("n", "123");
		map.put(Field.MSG, "n=123, aborted 5 of 10 attempts");
		d.add(new LogEntry(100L, map));
		BucketQuery q = new BucketQuery(new java.util.Date(0), 101, 0,
				Optional.<String> absent(), Optional.<String> absent(),
				Optional.<String> absent(), Optional.of(3),
				Optional.<String> absent());
		Buckets buckets = d.execute(q);
		assertEquals(1, buckets.getBuckets().size());
		assertEquals(10.0, buckets.getBucketForAll().sum(), PRECISION);
	}

	@Test
	public void testScanForDoubleFindsFirstDoubleInMiddleOfString() {
		Double d = Data.getDouble("hello there 1.3 and 1.5",
				Optional.<Pattern> absent(), 1);
		assertEquals(1.3, d, PRECISION);
	}

	@Test
	public void testScan() {

		String line = "processing=true,specialNumber=10.264801185812955,specialNumber2=47.90687220218723";
		Double d = Data.getDouble(line,
				Optional.of(Pattern.compile("(\\s|,|:|\\|;|=)+")), 2);
		assertEquals(47.90687220218723, d, PRECISION);
	}

	@Test
	public void testScanForDoubleFindsFirstDoubleAtStartOfString() {
		Double d = Data
				.getDouble("1.3 and 1.5", Optional.<Pattern> absent(), 1);
		assertEquals(1.3, d, PRECISION);
	}

	@Test
	public void testScanForDoubleFindsSecondDoubleInMiddleOfString() {
		Double d = Data.getDouble("hello there 1.3 and 1.5 boo",
				Optional.<Pattern> absent(), 2);
		assertEquals(1.5, d, PRECISION);
	}

	@Test
	public void testScanForDoubleFindsSecondDoubleAtEndOfString() {
		Double d = Data.getDouble("hello there 1.3 and 1.5",
				Optional.<Pattern> absent(), 2);
		assertEquals(1.5, d, PRECISION);
	}

}
