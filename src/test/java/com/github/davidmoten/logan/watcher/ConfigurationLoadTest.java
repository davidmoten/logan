package com.github.davidmoten.logan.watcher;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.davidmoten.logan.config.Configuration;
import com.github.davidmoten.logan.config.Marshaller;

public class ConfigurationLoadTest {

	@Test
	public void testLoadOfSampleConfiguration() {
		Configuration configuration = new Marshaller()
				.unmarshal(ConfigurationLoadTest.class
						.getResourceAsStream("/sample-configuration.xml"));
		assertEquals("UTC", configuration.parser.timezone);
	}
}
