package com.github.davidmoten.logan;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;

public class MessageSplitterTest {

	@Test
	public void testReturnsNullGivenNull() {
		MessageSplitter m = new MessageSplitter();
		assertEquals(0, m.split(null).size());
	}

	@Test
	public void testReturnsNullGivenBlank() {
		MessageSplitter m = new MessageSplitter();
		assertEquals(0, m.split("").size());
	}

	@Test
	public void testReturnsNullGivenTextWithoutEquals() {
		MessageSplitter m = new MessageSplitter();
		assertEquals(0, m.split("abc def").size());
	}

	@Test
	public void testReturnsMapGivenEqualsStatement() {
		MessageSplitter m = new MessageSplitter();
		Map<String, String> map = m.split("a=bcd;");
		assertEquals("bcd", map.get("a"));
	}

	@Test
	public void testReturnsMapGivenTwoEqualsStatementsDelimitedBySemicolon() {
		MessageSplitter m = new MessageSplitter();
		Map<String, String> map = m.split("a=bcd; b=hello there;");
		assertEquals("bcd", map.get("a"));
		assertEquals("hello there", map.get("b"));
	}

	@Test
	public void testReturnsMapGivenTwoEqualsStatementsDelimitedByPipe() {
		MessageSplitter m = new MessageSplitter();
		Map<String, String> map = m.split("a=bcd| b=hello there|");
		assertEquals("bcd", map.get("a"));
		assertEquals("hello there", map.get("b"));
	}

	@Test
	public void testReturnsMapGivenTwoEqualsStatementsDelimitedByPipeValueLeadingAndTrailingSpacesShouldBeIgnored() {
		MessageSplitter m = new MessageSplitter();
		Map<String, String> map = m.split("a= bcd    | b= hello there  |");
		assertEquals("bcd", map.get("a"));
		assertEquals("hello there", map.get("b"));
	}

	@Test
	public void testReturnsMapGivenTwoEqualsStatementsDelimitedByCommaValueLeadingAndTrailingSpacesShouldBeIgnored() {
		MessageSplitter m = new MessageSplitter();
		Map<String, String> map = m.split("a= bcd    , b= hello there  ,");
		assertEquals("bcd", map.get("a"));
		assertEquals("hello there", map.get("b"));
	}

	@Test
	public void testReturnsMapGivenTwoEqualsStatementsDelimitedByCommaValueFinalDelimiterEOL() {
		MessageSplitter m = new MessageSplitter();
		Map<String, String> map = m.split("a= bcd,b=hello there");
		assertEquals("bcd", map.get("a"));
		assertEquals("hello there", map.get("b"));
	}

	@Test
	public void testReturnsMapGivenTwoEqualsStatementsVariableHasSpaces() {
		MessageSplitter m = new MessageSplitter();
		Map<String, String> map = m.split(" run time  = 200");
		assertEquals("200", map.get("run time"));
	}

	@Test
	public void testReturnsMapGivenTwoEqualsStatementsVariableHasSpacesPrecededByStatementThatFinishesWithPeriod() {
		MessageSplitter m = new MessageSplitter();
		Map<String, String> map = m.split("Finished run. run time  = 200");
		assertEquals("200", map.get("run time"));
		assertEquals(1, map.size());
	}

	@Test
	public void testReturnsNumericAndIgnoresUnits() {
		MessageSplitter m = new MessageSplitter();
		Map<String, String> map = m
				.split("Finished run. run time  = 200 msg/s");
		assertEquals("200", map.get("run time"));
		assertEquals(1, map.size());
	}

	@Test
	public void testVariableNameStartsWithALetter() {
		MessageSplitter m = new MessageSplitter();
		Map<String, String> map = m.split("Finished run. 1run time  = 200");
		assertEquals("200", map.get("time"));
		assertEquals(1, map.size());
	}

	@Test
	public void testVariableNameHasHyphenMeansLatterPartOnlyIsInterpretedAsName() {
		MessageSplitter m = new MessageSplitter();
		Map<String, String> map = m.split("SKY-CONNECT= 200");
		assertEquals("200", map.get("CONNECT"));
		assertEquals(1, map.size());
	}
}
