package com.cs253.appdebugger.benchmarking;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

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
    private ArrayList<App> apps;
    private String level;
    private String singleApp;

    /**
     * Standard:
     * This allows us to log for all apps,
     * This is called from the AppDebugerService
     */
    public Logger(String level) {
        log = new StringBuilder();
        this.level = level;

        //readAllLogs();
    }

    /**
     * Overriden:
     * This allows us to log for a specific app, not all of them
     * Only call this from the AppDetails activity
     * @param appName
     */
    public Logger(String appName, String level) {
        log = new StringBuilder();
        this.singleApp = appName;
        this.level = level;

        readALog();
    }

    public String readALog() {
        return parseLogs(this.singleApp);
    }

    public String readAllLogs() {
        String s = "";
        for(App a: this.apps) {
            s = s + (a.getPackageName());
        }
        return parseLogs(s);
    }

    public String parseLogs(String filter) {
        try {
            String logCatFilter = "logcat "+filter+":"+this.level+" *:s";

            Log.d("AppDebugger", logCatFilter);

            this.mLogcatProc = Runtime.getRuntime().exec("logcat " + filter + ":" + this.level + " *:s");
            this.reader = new BufferedReader(new InputStreamReader(this.mLogcatProc.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {

                // Look for a start up command...
                Log.d("AppDebugger", "Here is a log for " + this.singleApp + ": " + line);
            }

        } catch (IOException e) {
            return null;
        }
        return this.log.toString();
    }

    public void addApp(App a) {
        this.apps.add(a);
    }


    // Asynchronous task that reads the logs for a particular app
    private class readLogsTask extends AsyncTask<Void, Void, Void> {
        Logger logger;
        @Override
        protected Void doInBackground(Void... voids) {
            this.logger = new Logger("com.facebook.katana", "V");
            //Toast.makeText(getApplicationContext(), "We are in the doinbackground", Toast.LENGTH_SHORT).show();
            try
            {
                logger.readALog();
            }
            catch (Exception e)
            {
                //Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
            return null;
        }
    }
}
