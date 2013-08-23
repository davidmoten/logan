package com.github.davidmoten.logan.watcher;

import com.github.davidmoten.logan.Data;
import com.github.davidmoten.logan.LogFile;

public interface FileTailer {
	void tail(LogFile logFile, Data data, boolean follow);
}
