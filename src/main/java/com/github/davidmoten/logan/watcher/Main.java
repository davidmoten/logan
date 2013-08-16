package com.github.davidmoten.logan.watcher;

import java.io.IOException;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.github.davidmoten.logan.Data;
import com.github.davidmoten.logan.config.Configuration;

/**
 * Reads persister-configuration.xml then starts threads to read/tail log files
 * and report log lines to <i>log-database</i>.
 * 
 * @author dave
 * 
 */
public class Main {

	private static Logger log = Logger.getLogger(Main.class.getName());

	/**
	 * Main method to start the persister.
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		Configuration configuration = Configuration.getConfiguration();
		setupLogging();

		Watcher w = new Watcher(new Data(), configuration);
		log.info("starting watcher");
		w.start();
		log.info("started");
	}

	private static void setupLogging() throws IOException {
		LogManager.getLogManager().readConfiguration(
				Main.class.getResourceAsStream("/my-logging.properties"));
	}

}
