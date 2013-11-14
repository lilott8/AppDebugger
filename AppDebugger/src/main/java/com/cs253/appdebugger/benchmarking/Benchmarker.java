package com.cs253.appdebugger.benchmarking;

import com.cs253.appdebugger.App;


/**
 * Created by jason on 11/6/13.
 */
public class Benchmarker {

    private App app;
    public Logger logger;
    public TrafficMonitor trafficMonitor;
    private long startTx;
    private long endTx;

    public Benchmarker(App whichApp) {
        this.app = whichApp;
        this.logger = new Logger("V");
        this.trafficMonitor = new TrafficMonitor(this.app);
    }


}