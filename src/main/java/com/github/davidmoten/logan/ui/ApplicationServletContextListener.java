package com.github.davidmoten.logan.ui;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import com.github.davidmoten.logan.LogFormatter;

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
		Handler ch = new ConsoleHandler();
		ch.setFormatter(new LogFormatter());
		Logger.getGlobal().addHandler(ch);
		log.info("initialized");
	}
}
