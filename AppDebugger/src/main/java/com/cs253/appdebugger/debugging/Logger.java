package com.cs253.appdebugger.debugging;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import com.cs253.appdebugger.App;

/**
 * Created by jason on 11/7/13.
 */
public class Logger {

    private Process mLogcatProc;
    private BufferedReader reader;
    private String line;
    private final StringBuilder log;
    private String separator;
    private List<App> apps;

    public Logger(List<App> applications) {
        log = new StringBuilder();
        this.apps = applications;

        readLogs(this.apps);
    }

    public String readLogs(List<App> applications) {
        String logger = "";
        try {
            for(App a : applications) {
                Log.d("Something", a.getPackageName());
            }
            this.mLogcatProc = Runtime.getRuntime().exec("logcat -d");
            this.reader = new BufferedReader(new InputStreamReader(this.mLogcatProc.getInputStream()));
        } catch (IOException e) {
            return null;
        }
        return this.log.toString();
    }
}
