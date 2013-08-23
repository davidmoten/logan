package com.github.davidmoten.logan.servlet;

import java.io.IOException;
import java.util.logging.LogManager;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import com.github.davidmoten.logan.Data;
import com.github.davidmoten.logan.DataMemory;
import com.github.davidmoten.logan.config.Configuration;
import com.github.davidmoten.logan.watcher.Watcher;

@WebListener
public class ApplicationServletContextListener implements
		ServletContextListener {

	@Override
	public void contextDestroyed(ServletContextEvent event) {

	}

	@Override
	public void contextInitialized(ServletContextEvent event) {
		setupLogging();
		Configuration configuration = Configuration.getConfiguration();
		Data data = new DataMemory(configuration.maxSize, true);
		Watcher w = new Watcher(data, configuration);
		w.start();
		State.setInstance(new State(data, configuration, w));
	}

	private static void setupLogging() {
		try {
			LogManager.getLogManager().readConfiguration(
					ApplicationServletContextListener.class
							.getResourceAsStream("/my-logging.properties"));
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
