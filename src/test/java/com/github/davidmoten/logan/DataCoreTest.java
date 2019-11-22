package com.github.davidmoten.logan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.regex.Pattern;

import org.junit.Test;

import com.google.common.base.Optional;

public class DataCoreTest {

	private static final double PRECISION = 0.00001;

	@Test
	public void testScanForDoubleFindsFirstDoubleInMiddleOfString() {
		Double d = DataHelper.getDouble("hello there 1.3 and 1.5",
				Optional.<Pattern> absent(), 1);
		assertEquals(1.3, d, PRECISION);
	}

	@Test
	public void testScan() {
		String line = "processing=true,specialNumber=10.264801185812955,specialNumber2=47.90687220218723";
		Double d = DataHelper.getDouble(line,
				Optional.of(Pattern.compile("(\\s|,|:|\\|;|=)+")), 2);
		assertEquals(47.90687220218723, d, PRECISION);
	}

	@Test
	public void testScanForDoubleFindsFirstDoubleAtStartOfString() {
		Double d = DataHelper.getDouble("1.3 and 1.5",
				Optional.<Pattern> absent(), 1);
		assertEquals(1.3, d, PRECISION);
	}

	@Test
	public void testScanForDoubleFindsSecondDoubleInMiddleOfString() {
		Double d = DataHelper.getDouble("hello there 1.3 and 1.5 boo",
				Optional.<Pattern> absent(), 2);
		assertEquals(1.5, d, PRECISION);
	}

	@Test
	public void testScanForDoubleFindsSecondDoubleAtEndOfString() {
		Double d = DataHelper.getDouble("hello there 1.3 and 1.5",
				Optional.<Pattern> absent(), 2);
		assertEquals(1.5, d, PRECISION);
	}

	@Test
	public void testScanForDoubleReturnsNullForThirdDouble() {
		Double d = DataHelper.getDouble("hello there 1.3 and 1.5",
				Optional.<Pattern> absent(), 3);
		assertNull(d);
	}
}
