package com.github.davidmoten.logan;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.io.input.TailerListener2;

public class TailerNonFollowing implements Runnable {

    private volatile boolean keepGoing = true;
    private final File file;
    private final TailerListener2 listener;
    private final int bufferSize;

    public TailerNonFollowing(File file, TailerListener2 listener, int bufferSize) {
        this.file = file;
        this.listener = listener;
        this.bufferSize = bufferSize;
    }

    @Override
    public void run() {
        try {
            try (BufferedReader br = //
                    new BufferedReader( //
                            new InputStreamReader( //
                                    new FileInputStream(file)),
                            bufferSize)) {
                String line;
                while (keepGoing && (line = br.readLine()) != null) {
                    listener.handle(line);
                }
            }
        } catch (IOException e) {
            listener.handle(e);
        }
    }

    public void stop() {
        keepGoing = false;
    }

}
