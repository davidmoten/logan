package com.github.davidmoten.logan.servlet;

import com.github.davidmoten.logan.Data;
import com.github.davidmoten.logan.config.Configuration;
import com.github.davidmoten.logan.watcher.Watcher;

public class State {

	private final Data data;

	private final Configuration configuration;

	private final Watcher watcher;

	public State(Data data, Configuration configuration, Watcher watcher) {
		this.data = data;
		this.configuration = configuration;
		this.watcher = watcher;
	}

	public Watcher getWatcher() {
		return watcher;
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
