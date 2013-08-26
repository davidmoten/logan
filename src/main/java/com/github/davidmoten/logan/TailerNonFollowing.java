package com.github.davidmoten.logan;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.io.input.TailerListener;

public class TailerNonFollowing implements Runnable {

	private volatile boolean keepGoing = true;
	private final File file;
	private final TailerListener listener;
	private final int bufferSize;

	public TailerNonFollowing(File file, TailerListener listener, int bufferSize) {
		this.file = file;
		this.listener = listener;
		this.bufferSize = bufferSize;
	}

	@Override
	public void run() {
		FileInputStream fis;
		try {
			fis = new FileInputStream(file);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis),
					bufferSize);
			String line;
			while (keepGoing && (line = br.readLine()) != null) {
				listener.handle(line);
			}
			br.close();
		} catch (FileNotFoundException e) {
			listener.handle(e);
		} catch (IOException e) {
			listener.handle(e);
		}
	}

	public void stop() {
		keepGoing = false;
	}

}
