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
                int i = 10000;
                while ((line = br.readLine()) != null) {
                    // don't do the volatile read every line
                    // to give better perf (probably unnoticeable)
                    // as we are doing IO
                    if (--i==0) {
                        i = 10000;
                        if (!keepGoing) {
                            break;
                        }
                    }
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
