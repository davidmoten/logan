package com.github.davidmoten.logan.ui;

import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import com.github.davidmoten.logan.Data;
import com.github.davidmoten.logan.config.Configuration;
import com.github.davidmoten.logan.watcher.Watcher;

@WebListener
public class DataLoaderServletContextListener implements ServletContextListener {

	private static Logger log = Logger
			.getLogger(DataLoaderServletContextListener.class.getName());

	@Override
	public void contextDestroyed(ServletContextEvent event) {

	}

	@Override
	public void contextInitialized(ServletContextEvent event) {
		Configuration configuration = Configuration.getConfiguration();
		Watcher w = new Watcher(new Data(), configuration);
		log.info("starting watcher");
		w.start();
		log.info("started");
	}

}
