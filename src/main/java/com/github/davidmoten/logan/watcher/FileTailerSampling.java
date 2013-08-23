package com.github.davidmoten.logan.watcher;

import java.util.Map;

import com.github.davidmoten.logan.Data;
import com.github.davidmoten.logan.LogFile;
import com.github.davidmoten.logan.LogFile.SampleResult;
import com.google.common.collect.Maps;

public class FileTailerSampling implements FileTailer {

	public enum Singleton {

		INSTANCE;

		private final FileTailerSampling instance = new FileTailerSampling();

		public FileTailerSampling instance() {
			return instance;
		}

	}

	private final Map<LogFile, SampleResult> samples = Maps.newHashMap();

	@Override
	public void tail(LogFile logFile, Data data, boolean follow) {
		samples.put(logFile, logFile.sample(20));
	}

	public Map<LogFile, SampleResult> getSamples() {
		return samples;
	}

}
