package com.github.davidmoten.logan.config;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import com.google.common.collect.Lists;

/**
 * A group of log files and a parser definition.
 * 
 * @author dave
 * 
 */
public class Group {

	@XmlAttribute(required = false)
	public boolean enabled = true;

	@XmlElement(required = false)
	public Parser parser;

	@XmlElement(required = false)
	public List<Log> log = Lists.newArrayList();

	/**
	 * Constructor.
	 * 
	 * @param log
	 * @param parser
	 */
	public Group(List<Log> log, Parser parser) {
		super();
		this.log = log;
		this.parser = parser;
	}

	/**
	 * Constructor.
	 * 
	 * @param log
	 */
	public Group(List<Log> log) {
		this(log, null);
	}

	/**
	 * Constructor.
	 */
	public Group() {
		// no-args constructor required by jaxb
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Group [log=");
		builder.append(log);
		builder.append(", parser=");
		builder.append(parser);
		builder.append("]");
		return builder.toString();
	}

}
