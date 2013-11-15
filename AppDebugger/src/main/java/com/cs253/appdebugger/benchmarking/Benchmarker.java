package com.cs253.appdebugger.benchmarking;

import com.cs253.appdebugger.App;


/**
 * Created by jason on 11/6/13.
 */
public class Benchmarker {

    private String packageName;
    public Logger logger;
    public TrafficMonitor trafficMonitor;
    private long startTx;
    private long endTx;

    public Benchmarker(String packageName) {
        this.packageName = packageName;
        this.logger = new Logger("V");
        this.trafficMonitor = new TrafficMonitor(this.packageName);
    }


}