package com.github.davidmoten.logan;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;

/**
 * Starts a thread using a given {@link ExecutorService} to load all logs from a
 * {@link File} and then monitor the file for new lines and load them too as
 * they arrive.
 * 
 */
public class LogFile {

    private static Logger log = Logger.getLogger(LogFile.class.getName());

    private final File file;
    private final long checkIntervalMs;
    private Runnable tailer;
    private final LogParser parser;
    private final ExecutorService executor;
    private final String source;

    public LogFile(File file, String source, long checkIntervalMs, LogParser parser,
            ExecutorService executor) {
        this.file = file;
        this.source = source;
        this.checkIntervalMs = checkIntervalMs;
        this.parser = parser;
        this.executor = executor;
        createFileIfDoesntExist(file);
    }

    @VisibleForTesting
    static void createFileIfDoesntExist(File file) {
        if (!file.exists())
            try {
                if (!file.createNewFile())
                    throw new RuntimeException("could not create file: " + file);
            } catch (IOException e) {
                throw new RuntimeException("could not create file: " + file, e);
            }
    }

    private static int BUFFER_SIZE = 2 * 4096;

    /**
     * Starts a thread that tails a file from the start and reports extracted
     * info from the lines to the database.
     * 
     * @param data
     *            data
     * @param follow
     *            follow
     */
    public void tail(Data data, boolean follow) {

        TailerListener listener = createListener(data);

        if (follow)
            // tail from the start of the file and watch for future changes
            tailer = new Tailer(file, listener, checkIntervalMs, false, BUFFER_SIZE);

        else
            tailer = new TailerNonFollowing(file, listener, BUFFER_SIZE);

        // start in separate thread
        log.info("starting tailer thread");
        executor.execute(tailer);
    }

    public static class SampleResult {

        private final LinkedHashMap<LogEntry, List<String>> entries;
        private final List<String> unparsedLines;

        public SampleResult(LinkedHashMap<LogEntry, List<String>> entries,
                List<String> unparsedLines) {
            this.entries = entries;
            this.unparsedLines = unparsedLines;
        }

        public List<String> getUnparsedLines() {
            return unparsedLines;
        }

        public LinkedHashMap<LogEntry, List<String>> getEntries() {
            return entries;
        }

    }

    public SampleResult sample(int numLines) {

        try {
            FileInputStream fis = new FileInputStream(file);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));

            String line;
            List<String> lines = Lists.newArrayList();
            LinkedHashMap<LogEntry, List<String>> entries = new LinkedHashMap<LogEntry, List<String>>();
            int lineCount = 0;
            while (lineCount < numLines && (line = br.readLine()) != null) {
                LogEntry entry = parser.parse(source, line);
                lines.add(line);
                if (entry != null) {
                    entries.put(entry, lines);
                    lines = Lists.newArrayList();
                }
                lineCount++;
            }
            br.close();
            return new SampleResult(entries, lines);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Stops the tailer (and thus its thread).
     */
    public void stop() {
        if (tailer != null)
            if (tailer instanceof Tailer)
                ((Tailer) tailer).stop();
            else if (tailer instanceof TailerNonFollowing)
                ((TailerNonFollowing) tailer).stop();
    }

    private TailerListener createListener(final Data data) {
        return new TailerListener() {
            private final Data db = data;

            @Override
            public void fileNotFound() {
                log.warning("file not found");
            }

            @Override
            public void fileRotated() {
                log.info("file rotated");
            }

            @Override
            public synchronized void handle(String line) {
                log.fine(new StringBuilder().append(source).append(":").append(line).toString());
                try {
                    LogEntry entry = parser.parse(source, line);
                    if (entry != null) {
                        db.add(entry);
                        log.fine("added");
                    }
                } catch (Throwable e) {
                    log.log(Level.WARNING, e.getMessage(), e);
                }
            }

            @Override
            public void handle(Exception e) {
                log.log(Level.WARNING, "handle exception " + e.getMessage(), e);
            }

            @Override
            public void init(Tailer tailer) {
                log.info("init");
            }
        };
    }

    public File getFile() {
        return file;
    }
}
