package com.github.davidmoten.logan.watcher;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
					String source;
					if (lg.source == null
							&& configuration.parser.sourcePattern != null)
						source = extractSource(
								configuration.parser.sourcePattern,
								file.getName());
					else
						source = lg.source;
					if (source == null)
						throw new RuntimeException(
								"source not specified or could not be extracted using sourcePattern for log:"
										+ lg);
					LogFile logFile = new LogFile(file, source, 500,
							new LogParser(options), executor);
					logFile.tail(data);
					logs.add(logFile);
				}
			}
		}
		log.info("started watcher");
	}

	private String extractSource(String sourcePattern, String filename) {
		Pattern p = Pattern.compile(sourcePattern);
		Matcher m = p.matcher(filename);
		if (!m.find())
			throw new RuntimeException("could not find source in " + filename
					+ " using pattern " + sourcePattern);
		else
			return m.group();
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
