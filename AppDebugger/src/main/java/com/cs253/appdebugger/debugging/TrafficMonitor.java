package com.cs253.appdebugger.debugging;

import android.net.TrafficStats;
import android.util.Log;

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

        Log.d("AppDebugger", "The UID for " + this.app.getPackageName() + " is " + Integer.toString(this.app.getUid()));
        return this.trafficStats.getUidTxBytes(this.app.getUid());
    }
}