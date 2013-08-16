package com.github.davidmoten.logan.config;


import javax.xml.bind.annotation.XmlElement;

public class Connection {
	@XmlElement(required = true)
	public String url = "remote:localhost/logs";
	@XmlElement(required = true)
	public String username = "admin";
	@XmlElement(required = true)
	public String password = "admin";

}
