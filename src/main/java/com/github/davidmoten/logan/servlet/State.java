package com.github.davidmoten.logan.servlet;

import com.github.davidmoten.logan.Data;
import com.github.davidmoten.logan.config.Configuration;

public class State {

	private final Data data;

	private final Configuration configuration;

	public State(Data data, Configuration configuration) {
		this.data = data;
		this.configuration = configuration;
	}

	public Data getData() {
		return data;
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	private static State instance;

	public static State instance() {
		return instance;
	}

	public static void setInstance(State instance) {
		State.instance = instance;
	}

}
