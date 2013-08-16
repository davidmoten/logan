package com.github.davidmoten.logan.ui;

import java.io.IOException;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class ApplicationServletContextListener implements
		ServletContextListener {

	private static Logger log = Logger
			.getLogger(ApplicationServletContextListener.class.getName());

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		log.info("context destroyed");
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		setupLogging();
		log.info("initialized");
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
