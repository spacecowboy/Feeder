package com.nononsenseapps.feeder.util;

import android.support.annotation.NonNull;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static junit.framework.Assert.assertEquals;

public class FileLogTest {
    @Test
    public void shouldLogEntry() throws Exception {
        FileLog log = getFileLog();

        log.d("Testing an entry");

        assertEquals( "Testing an entry\n", log.getLog() );
    }

    @Test
    public void shouldNotExceedNLines() throws Exception {
        FileLog log = getFileLog();

        for (int i = 0; i < 20; i++) {
            log.d("Testing an entry");
        }

        assertEquals( 10, log.getLog().split("\n").length );
    }

    @Test
    public void persistsFile() throws Exception {
        File tempFile = getTempFile();

        FileLog log = new FileLog(tempFile, 10);

        log.d("an entry");
        log.d("another");

        log = new FileLog(tempFile, 10);

        assertEquals("an entry\n" +
                "another\n", log.getLog());
    }

    @NonNull
    private FileLog getFileLog() throws IOException {
        return new FileLog(getTempFile(), 10);
    }

    @NonNull
    private File getTempFile() throws IOException {
        File tempFile = File.createTempFile("feeder", "log");
        tempFile.deleteOnExit();
        return tempFile;
    }
}
