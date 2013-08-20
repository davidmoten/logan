package com.github.davidmoten.logan.config;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.google.common.collect.Lists;

public class ConfigurationTest {

	@Test
	public void testInstantiation() {
		new Configuration(new Parser(), Lists.<Group> newArrayList());
	}

	@Test
	public void testLoadFromClasspath() {
		String temp = System.getProperty("logan.config");
		System.setProperty("logan.config", "/sample-configuration.xml");
		Configuration c = Configuration.getConfiguration();
		assertEquals(1000000, c.maxSize);
		if (temp == null)
			System.clearProperty("logan.config");
		else
			System.setProperty("logan.config", temp);
	}

	@Test
	public void testLoadFromFileSystem() {
		String temp = System.getProperty("logan.config");
		System.setProperty("logan.config",
				"src/test/resources/sample-configuration.xml");
		Configuration c = Configuration.getConfiguration();
		assertEquals(1000000, c.maxSize);
		if (temp == null)
			System.clearProperty("logan.config");
		else
			System.setProperty("logan.config", temp);
	}

	@Test(expected = RuntimeException.class)
	public void testLoadThrowsExceptionWhenFileDoesNotExist() {
		String temp = null;
		try {
			temp = System.getProperty("logan.config");
			System.setProperty("logan.config", "zzz123zzz");
			Configuration c = Configuration.getConfiguration();
			assertEquals(1000000, c.maxSize);
		} finally {
			if (temp == null)
				System.clearProperty("logan.config");
			else
				System.setProperty("logan.config", temp);
		}

	}

}
