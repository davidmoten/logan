package com.github.davidmoten.logan.watcher;
import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.github.davidmoten.logan.Data;
import com.github.davidmoten.logan.LogFile;
import com.github.davidmoten.logan.LogParser;
import com.github.davidmoten.logan.LogParserOptions;
import com.github.davidmoten.logan.config.Configuration;
import com.github.davidmoten.logan.config.Group;
import com.github.davidmoten.logan.config.Log;
import com.google.common.collect.Lists;

/**
 * Watches (tails) groups of files configured by persister configuration and
 * reports lines to the <i>log-database</i>.
 * 
 * @author dave
 * 
 */
public class Watcher {

	private static final int TERMINATION_TIMEOUT_MS = 30000;

	private static final Logger log = Logger.getLogger(Watcher.class.getName());

	private final ExecutorService executor;

	private final List<LogFile> logs = Lists.newArrayList();

	private final Configuration configuration;

	private final Data data;

	/**
	 * Constructor.
	 * 
	 * @param factory
	 * @param configuration
	 */
	public Watcher(Data data, Configuration configuration) {
		this.data = data;
		this.configuration = configuration;
		executor = Executors.newFixedThreadPool(20);
	}

	/**
	 * Starts tailing threads for each configured matched file.
	 */
	public void start() {
		log.info("starting watcher");
		for (Group group : configuration.group) {
			log.info("starting group " + group);
			for (Log lg : group.log) {
				for (File file : Util
						.getFilesFromPathWithRegexFilename(lg.path)) {
					log.info("starting tail on " + file);
					LogParserOptions options = LogParserOptions.load(
							configuration.parser, group);
					LogFile logFile = new LogFile(file, lg.source, 500,
							new LogParser(options), executor);
					logFile.tail(data);
					logs.add(logFile);
				}
			}
		}
		log.info("started watcher");
	}

	/**
	 * Stops each thread watching a file and shuts down the executor that
	 * started the threads.
	 */
	public void stop() {
		log.info("stopping watcher");
		for (LogFile lg : logs) {
			lg.stop();
		}
		executor.shutdownNow();
		try {
			executor.awaitTermination(TERMINATION_TIMEOUT_MS,
					TimeUnit.MILLISECONDS);
			log.info("stopped watcher");
		} catch (InterruptedException e) {
			throw new RuntimeException("failed to stop running threads", e);
		}
	}

}
