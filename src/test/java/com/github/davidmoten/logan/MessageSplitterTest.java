package com.github.davidmoten.logan;

import static org.junit.Assert.assertEquals;

import java.util.Map;
import java.util.regex.Pattern;

import org.junit.Test;

public class MessageSplitterTest {

	@Test
	public void testReturnsNullGivenNull() {
		MessageSplitter m = new MessageSplitter();
		assertEquals(0, m.splitAsMap(null).size());
	}

	@Test
	public void testReturnsNullGivenBlank() {
		MessageSplitter m = new MessageSplitter();
		assertEquals(0, m.splitAsMap("").size());
	}

	@Test
	public void testReturnsNullGivenTextWithoutEquals() {
		MessageSplitter m = new MessageSplitter();
		assertEquals(0, m.splitAsMap("abc def").size());
	}

	@Test
	public void testReturnsMapGivenEqualsStatement() {
		MessageSplitter m = new MessageSplitter();
		Map<String, String> map = m.splitAsMap("a=bcd;");
		assertEquals("bcd", map.get("a"));
	}

	@Test
	public void testReturnsMapGivenTwoEqualsStatementsDelimitedBySemicolon() {
		MessageSplitter m = new MessageSplitter();
		Map<String, String> map = m.splitAsMap("a=bcd; b=hello there;");
		assertEquals("bcd", map.get("a"));
		assertEquals("hello there", map.get("b"));
	}

	@Test
	public void testReturnsMapGivenTwoEqualsStatementsDelimitedByPipe() {
		MessageSplitter m = new MessageSplitter();
		Map<String, String> map = m.splitAsMap("a=bcd| b=hello there|");
		assertEquals("bcd", map.get("a"));
		assertEquals("hello there", map.get("b"));
	}

	@Test
	public void testReturnsMapGivenTwoEqualsStatementsDelimitedByPipeValueLeadingAndTrailingSpacesShouldBeIgnored() {
		MessageSplitter m = new MessageSplitter();
		Map<String, String> map = m.splitAsMap("a= bcd    | b= hello there  |");
		assertEquals("bcd", map.get("a"));
		assertEquals("hello there", map.get("b"));
	}

	@Test
	public void testReturnsMapGivenTwoEqualsStatementsDelimitedByCommaValueLeadingAndTrailingSpacesShouldBeIgnored() {
		MessageSplitter m = new MessageSplitter();
		Map<String, String> map = m.splitAsMap("a= bcd    , b= hello there  ,");
		assertEquals("bcd", map.get("a"));
		assertEquals("hello there", map.get("b"));
	}

	@Test
	public void testReturnsMapGivenTwoEqualsStatementsDelimitedByCommaValueFinalDelimiterEOL() {
		MessageSplitter m = new MessageSplitter();
		Map<String, String> map = m.splitAsMap("a= bcd,b=hello there");
		assertEquals("bcd", map.get("a"));
		assertEquals("hello there", map.get("b"));
	}

	@Test
	public void testReturnsMapGivenTwoEqualsStatementsVariableHasSpaces() {
		MessageSplitter m = new MessageSplitter();
		Map<String, String> map = m.splitAsMap(" run time  = 200");
		assertEquals("200", map.get("run time"));
	}

	@Test
	public void testReturnsMapGivenTwoEqualsStatementsVariableHasSpacesPrecededByStatementThatFinishesWithPeriod() {
		MessageSplitter m = new MessageSplitter();
		Map<String, String> map = m.splitAsMap("Finished run. run time  = 200");
		assertEquals("200", map.get("run time"));
		assertEquals(1, map.size());
	}

	@Test
	public void testVariableNameStartsWithALetter() {
		MessageSplitter m = new MessageSplitter();
		Map<String, String> map = m.splitAsMap("Finished run. 1run time  = 200");
		assertEquals("200", map.get("time"));
		assertEquals(1, map.size());
	}

	@Test
	public void testVariableNameHasHyphenMeansLatterPartOnlyIsInterpretedAsName() {
		MessageSplitter m = new MessageSplitter();
		Map<String, String> map = m.splitAsMap("SKY-CONNECT= 200");
		assertEquals("200", map.get("CONNECT"));
		assertEquals(1, map.size());
	}
	
	@Test
	public void testGlassfishPattern() {
		MessageSplitter m = new MessageSplitter(Pattern.compile("(\\b[a-zA-Z](?:\\w| )*)=([^;|,~]*)(;|\\||,|$|~)"));
		Map<String, String> map = m.splitAsMap("contentHash=9a9d4311~messagePersistTimeMs=124");
		assertEquals("9a9d4311", map.get("contentHash"));
		assertEquals("124", map.get("messagePersistTimeMs"));
	}
}
