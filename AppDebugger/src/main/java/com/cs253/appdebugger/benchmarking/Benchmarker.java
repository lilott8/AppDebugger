package com.cs253.appdebugger.benchmarking;

import android.content.Context;
import android.content.pm.PackageManager;

import com.cs253.appdebugger.App;


/**
 * Created by jason on 11/6/13.
 */
public class Benchmarker {

    private App app;
    public Logger logger;
    public NetworkMonitor nm;
    private Context context;

    public Benchmarker() {

    }

    public Benchmarker(App whichApp, Context c) {
        this.app = whichApp;
        this.context = c;
        this.logger = new Logger("V");
        this.nm = new NetworkMonitor(this.app);
    }

    public Context getContext() {
        return this.context;
    }
}