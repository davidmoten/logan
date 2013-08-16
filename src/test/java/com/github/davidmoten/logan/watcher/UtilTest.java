package com.github.davidmoten.logan.watcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.junit.Test;

import com.google.common.collect.Sets;

public class UtilTest {

	private static final Logger log = Logger
			.getLogger(UtilTest.class.getName());

	@Test
	public void testGetLogsNoWildcardsRelativePath() {

		List<File> files = Util
				.getFilesFromPathWithRegexFilename("src/test/resources/test.log");
		assertEquals("test.log", files.get(0).getName());
		assertEquals(1, files.size());
	}

	@Test
	public void testGetLogsNoWildcardsAbsolutePath() {
		File file = new File("src/test/resources/test.log");
		List<File> files = Util.getFilesFromPathWithRegexFilename(file
				.getAbsolutePath());
		Set<String> set = toSet(files);
		assertTrue(set.contains("test.log"));
		assertEquals(1, set.size());
	}

	@Test
	public void testGetDirectoriesUsingRelativePath() {

		List<File> list = Util
				.getDirectories("src/test/resources/matching-test/**");

		boolean found = false;
		for (File f : list) {
			found |= f.getAbsolutePath().endsWith(
					"src/test/resources/matching-test/test1");
			assertTrue(f.exists());
		}
		assertTrue(found);
		Set<String> set = toSet(list);
		System.out.println(set);
		assertTrue(set.contains("test1"));
		assertTrue(set.contains("test2"));
		assertEquals(7, set.size());
	}

	@Test
	public void testGetDirectoriesUsingAbsolutePath() {

		File file = new File("src/test/resources/matching-test/**");
		List<File> list = Util.getDirectories(file.getAbsolutePath());
		boolean found = false;
		for (File f : list) {
			found |= f.getAbsolutePath().endsWith(
					"src/test/resources/matching-test/test1");
			assertTrue(f.exists());
		}
		assertTrue(found);
		Set<String> set = toSet(list);
		System.out.println(set);
		assertTrue(set.contains("test1"));
		assertTrue(set.contains("test2"));
		assertEquals(7, set.size());
	}

	@Test
	public void testStringContains() {
		assertEquals(1, "abc".indexOf("bc"));
	}

	@Test
	public void testGetMatchingFiles() {
		List<File> files = Util
				.getFilesFromPathWithRegexFilename("src/test/resources/test(2|3)\\.log");
		Set<String> paths = Sets.newHashSet();
		paths.add(files.get(0).getName());
		paths.add(files.get(1).getName());
		log.info("paths=" + paths);
		assertTrue(paths.contains("test2.log"));
		assertTrue(paths.contains("test3.log"));
		assertEquals(2, files.size());
	}

	@Test
	public void testGetMatchingFilesWithDirectoryWildcard() {
		List<File> files = Util
				.getFilesFromPathWithRegexFilename("src/test/resources/matching-test/**/.*\\.log");
		Set<String> set = toSet(files);
		assertEquals(7, set.size());
		assertTrue(set.contains("a.log"));
		assertTrue(set.contains("b.log"));
		assertTrue(set.contains("c.log"));
		assertTrue(set.contains("d.log"));
		assertTrue(set.contains("e.log"));
		assertTrue(set.contains("f.log"));
		assertTrue(set.contains("g.log"));
	}

	@Test
	public void testGetMatchingFilesWithDirectoryWildcardPrefixed() {
		List<File> files = Util
				.getFilesFromPathWithRegexFilename("src/test/resources/matching-test/test**/.*\\.log");
		Set<String> set = toSet(files);
		assertEquals(4, set.size());
		assertTrue(set.contains("a.log"));
		assertTrue(set.contains("b.log"));
		assertTrue(set.contains("c.log"));
		assertTrue(set.contains("d.log"));
	}

	@Test
	public void testGetMatchingFilesWithDirectoryWildcardPrefixedWithFullDirectoryName() {
		List<File> files = Util
				.getFilesFromPathWithRegexFilename("src/test/resources/matching-test/test1**/.*\\.log");
		Set<String> set = toSet(files);
		assertEquals(2, set.size());
		assertTrue(set.contains("a.log"));
		assertTrue(set.contains("b.log"));
	}

	// TODO add unit tests for when directory wildcard is followed by another
	// directory
	// for example src/test/resources/matching-test-2/apps/**/logs/.*\\.log

	private static Set<String> toSet(Collection<File> files) {
		Set<String> set = Sets.newHashSet();
		for (File file : files)
			set.add(file.getName());
		return set;
	}

	@Test
	public void testGetPath() {
		assertEquals("/src/test/resources/",
				Util.getDirectory("/src/test/resources/a\\.[0-9]?\\.log"));
	}

	@Test
	public void testGetFilename() {
		assertEquals("a\\.[0-9]?\\.log",
				Util.getFilename("/src/test/resources/a\\.[0-9]?\\.log"));
	}

	@Test
	public void testParsePath() {
		assertEquals("/ausdev/container/logs/cts/",
				Util.getDirectory("/ausdev/container/logs/cts/cts.log.*"));
	}

	@Test
	public void testParseFilename() {
		assertEquals("cts.log.*",
				Util.getFilename("/ausdev/container/logs/cts/cts.log.*"));
	}
}
