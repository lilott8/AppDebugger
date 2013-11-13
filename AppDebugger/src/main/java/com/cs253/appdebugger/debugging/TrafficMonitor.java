package com.cs253.appdebugger.debugging;

import android.net.TrafficStats;

import com.cs253.appdebugger.App;

/**
 * Created by jason on 11/13/13.
 */
public class TrafficMonitor{

    private App app;
    private TrafficStats trafficStats;
    private long beforeStartup;
    private long afterStartup;

    public TrafficMonitor(App whichApp) {
        this.app = whichApp;
        this.trafficStats = new TrafficStats();
    }

    public long getTxBytes() {
        return this.trafficStats.getUidTxBytes(this.app.getUid());
    }
}