package com.github.davidmoten.logan.watcher;

import com.github.davidmoten.logan.Data;
import com.github.davidmoten.logan.LogFile;

public class FileTailerStandard implements FileTailer {

	public enum Singleton {

		INSTANCE;

		private final FileTailerStandard instance = new FileTailerStandard();

		public FileTailerStandard instance() {
			return instance;
		}

	}

	@Override
	public void tail(LogFile logFile, Data data, boolean follow) {
		logFile.tail(data, follow);
	}

}
