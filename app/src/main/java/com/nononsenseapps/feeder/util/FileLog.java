package com.nononsenseapps.feeder.util;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.Queue;

public class FileLog {
    private static final int DEFAULT_MAX_LINES = 1000;
    private static FileLog instance;
    private final Queue<String> memLog;

    public static synchronized FileLog instance(Context context) {
        if (instance == null) {
            instance = new FileLog(new File(context.getFilesDir(), "feeder.log"), DEFAULT_MAX_LINES);
        }
        return instance;
    }

    private final File logFile;
    private final int maxLines;

    FileLog(File logFile, int maxLines) {
        this.logFile = logFile;
        this.maxLines = maxLines;
        memLog = readLog();
    }

    private Queue<String> readLog() {
        Queue<String> log = new ArrayDeque<>();
        if (logFile.exists()) {
            try (FileReader fr = new FileReader(logFile); BufferedReader br = new BufferedReader(fr)) {
                String line;
                while ((line = br.readLine()) != null) {
                    log.add(line);
                }
            } catch (Exception ignored) {
            }
        }
        ensureSize(log);
        return log;
    }

    private void ensureSize(Queue<String> log) {
        while (log.size() > maxLines) {
            log.remove();
        }
    }

    public synchronized String getLog() {
        ensureSize(memLog);
        StringBuilder sb = new StringBuilder();
        for (String l : memLog) {
            sb.append(l).append("\n");
        }
        return sb.toString();
    }

    public synchronized void d(String line) {
        memLog.add(line);
        ensureSize(memLog);
        persist();
    }

    public static void d(Context context, String line) {
        instance(context).d(line);
    }

    private void persist() {
        ensureSize(memLog);
        try (PrintWriter pw = new PrintWriter(logFile)) {
            memLog.forEach(pw::println);
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }
}
