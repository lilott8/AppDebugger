package com.cs253.appdebugger.debugging;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.cs253.appdebugger.App;
import com.cs253.appdebugger.debugging.TrafficMonitor;
import android.util.Log;


/**
 * Created by jason on 11/6/13.
 */
public class Debugger {

    private App app;
    public Logger logger;
    public TrafficMonitor trafficMonitor;
    private long startTx;
    private long endTx;

    public Debugger(App whichApp) {
        this.app = whichApp;
        this.logger = new Logger("V");
        this.trafficMonitor = new TrafficMonitor(this.app);
    }

    public long getTxBytes() {
        return this.trafficMonitor.getTxBytes();
    }

    public long totalBytes() {
        return 0;
    }


}