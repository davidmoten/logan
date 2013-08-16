package com.github.davidmoten.logan.config;

import javax.xml.bind.annotation.XmlElement;

import com.github.davidmoten.logan.MessageSplitter;

/**
 * 
 * Parser options.
 * 
 * @author dave
 * 
 */
public class Parser {

	@XmlElement(required = false)
	public String sourcePattern;
	@XmlElement(required = true)
	public String pattern;
	@XmlElement(required = true)
	public String patternGroups;
	@XmlElement(defaultValue = MessageSplitter.MESSAGE_PATTERN_DEFAULT)
	public String messagePattern = MessageSplitter.MESSAGE_PATTERN_DEFAULT;
	@XmlElement(required = true)
	public String timestampFormat;
	@XmlElement(defaultValue = "UTC")
	public String timezone = "UTC";
	@XmlElement(required = false, defaultValue = "false")
	public boolean multiline;

	/**
	 * Parser configuration.
	 * 
	 * @param pattern
	 * @param patternGroups
	 * @param timestampFormat
	 * @param timezone
	 * @param multiline
	 */
	public Parser(String pattern, String patternGroups, String messagePattern,
			String timestampFormat, String timezone, boolean multiline,
			String sourcePattern) {
		super();
		this.pattern = pattern;
		this.patternGroups = patternGroups;
		this.messagePattern = messagePattern;
		this.timestampFormat = timestampFormat;
		this.timezone = timezone;
		this.multiline = multiline;
		this.sourcePattern = sourcePattern;
	}

	/**
	 * Constructor.
	 */
	public Parser() {
		// required for jaxb
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Parser [pattern=");
		builder.append(pattern);
		builder.append(", patternGroups=");
		builder.append(patternGroups);
		builder.append(", messagePattern=");
		builder.append(messagePattern);
		builder.append(", timestampFormat=");
		builder.append(timestampFormat);
		builder.append(", timezone=");
		builder.append(timezone);
		builder.append(", multiline=");
		builder.append(multiline);
		builder.append("]");
		return builder.toString();
	}

}
