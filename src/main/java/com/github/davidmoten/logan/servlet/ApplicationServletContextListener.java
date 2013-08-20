package com.github.davidmoten.logan.servlet;

import java.io.IOException;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import com.github.davidmoten.logan.Data;
import com.github.davidmoten.logan.config.Configuration;
import com.github.davidmoten.logan.watcher.Watcher;

@WebListener
public class ApplicationServletContextListener implements
		ServletContextListener {

	private static Logger log = Logger
			.getLogger(ApplicationServletContextListener.class.getName());

	@Override
	public void contextDestroyed(ServletContextEvent event) {

	}

	@Override
	public void contextInitialized(ServletContextEvent event) {
		setupLogging();
		Configuration configuration = Configuration.getConfiguration();
		Data data = Data.instance();
		data.setMaxSize(configuration.maxSize);
		State.setInstance(new State(data, configuration));
		Watcher w = new Watcher(Data.instance(), configuration);
		log.info("starting watcher");
		w.start();
		log.info("started");
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
