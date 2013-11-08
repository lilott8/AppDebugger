package com.cs253.appdebugger.debugging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by jason on 11/7/13.
 */
public class Logger {

    private Process mLogcatProc;
    private BufferedReader reader;
    private String line;
    private final StringBuilder log;
    private String separator;

    public Logger() {
/*
        this.mLogcatProc = null;
        this.reader = null;
        try {
        this.mLogcatProc = Runtime.getRuntime().exec(new String[]{"logcat", "-d"});
        } catch (IOException e) {

        }
        this.reader = new BufferedReader(new InputStreamReader(this.mLogcatProc.getInputStream()));

        this.log = new StringBuilder();
        this.separator = System.getProperty("line.separator");
    }

    public String readLine() throws IOException {

        while ((this.line = this.reader.readLine()) != null)
        {
            log.append(line);
            log.append(separator);
        }
        return log.toString();
        //Toast.makeText(getApplicationContext(),w, Toast.LENGTH_LONG).show();
        */
    }
}
