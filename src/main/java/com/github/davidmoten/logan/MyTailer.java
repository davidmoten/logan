package com.github.davidmoten.logan;

import java.io.File;

import org.apache.commons.io.input.TailerListener;

public class MyTailer implements Runnable {

	private volatile boolean keepGoing = true;

	public MyTailer(File file, TailerListener listener, int bufferSize) {

	}

	@Override
	public void run() {

	}

	public void stop() {

	}

}
