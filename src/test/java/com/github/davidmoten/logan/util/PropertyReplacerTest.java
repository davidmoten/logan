package com.github.davidmoten.logan.util;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class PropertyReplacerTest {

	@Test
	public void testReplaceWhenPropertySetToNullReplacesNothing()
			throws IOException {
		String text = "hello ${my.name}, how are you ${my.name}";
		InputStream is = new ByteArrayInputStream(text.getBytes());
		System.clearProperty("my.name");
		String result = IOUtils.toString(PropertyReplacer
				.replaceSystemProperties(is));
		System.out.println(result);
		assertEquals(text, result);
	}

	@Test
	public void testReplaceWhenPropertySetToSomethingDoesReplacement()
			throws IOException {
		String text = "hello ${my.name}, how are you ${my.name}";
		InputStream is = new ByteArrayInputStream(text.getBytes());
		System.setProperty("my.name", "Dave");
		String result = IOUtils.toString(PropertyReplacer
				.replaceSystemProperties(is));
		System.out.println(result);
		assertEquals("hello Dave, how are you Dave", result);
	}

	@Test
	public void testReplaceWhenPropertySetToSomethingDoesReplacementOnTwoLines()
			throws IOException {
		String text = "hello ${my.name}, how are you ${my.name}\nseeya ${my.name}";
		InputStream is = new ByteArrayInputStream(text.getBytes());
		System.setProperty("my.name", "Dave");
		String result = IOUtils.toString(PropertyReplacer
				.replaceSystemProperties(is));
		System.out.println(result);
		assertEquals("hello Dave, how are you Dave\nseeya Dave", result);
	}
}
