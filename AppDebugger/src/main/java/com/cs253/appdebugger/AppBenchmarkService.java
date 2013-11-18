package com.cs253.appdebugger;

import android.app.Application;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;

import com.cs253.appdebugger.benchmarking.Benchmarker;
import com.cs253.appdebugger.database.StatsDataSource;
import com.cs253.appdebugger.benchmarking.Logger;
import com.cs253.appdebugger.other.ParcelableApp;

import java.security.Timestamp;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by jason on 10/24/13.
 */
public class AppBenchmarkService extends Service {

    // Our database connections
    private StatsDataSource statsDataSource;
    // Probably not needed, this is supposed to be a wrapper for all benchmarking
    // But this may provide too much overhead for what its worth and complicate
    // things more than they need to be.
    private Benchmarker benchmarker;
    // Only used for Toast messages
    private Application parentApp;
    // Maintain ties to our context so we can use Toast for debugging
    private Context context;
    private Bundle extras;
    // Which app we are monitoring
    private String packageName;
    private long startTs;
    private long endTs;
    private App app;
    private boolean serviceStarted;

    /**
     *
     * @param intent
     * @param flags
     * @param startID
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        /**
         *  These three longs will help us determine
         *  the loading of an app.  When the size
         *  between nowTx and PreviousTx are less
         *  than delta, our app is "done" loading
         */
        long nowTx = 0;
        long previousTx = 0;
        long deltaTx = 1000;
        // initialize our data sources so we have DB connectivity
        this.statsDataSource = new StatsDataSource(this);
        this.statsDataSource.open();
        // Our databases are now loaded
        // get our application context
        this.parentApp = getApplication();
        this.context = this.parentApp.getApplicationContext();
        // get our intent extras that we send to this class
        this.extras = intent.getExtras();
        ParcelableApp pa = this.extras.getParcelable("app");
        this.app = pa.getApp();
        this.benchmarker = new Benchmarker(this.app);
        Toast.makeText(this.context, "The package name is: "+this.app.getPackageName(), Toast.LENGTH_SHORT).show();
        /**
         * Get the tx size for our app
         */
        nowTx = this.benchmarker.trafficMonitor.getTxBytes();
        /**
         * Grab the timestamp of before we load our system
         */
        this.startTs = System.currentTimeMillis();
        /**
         * Start the app
         */
        Intent benchmarkIntent = getPackageManager().getLaunchIntentForPackage(this.app.getPackageName());
        if(benchmarkIntent != null) {
            startActivity(benchmarkIntent);
        } else {
            try {
                Log.d("AppDebugger", "We cannot open this intent because it's null???? " + benchmarkIntent.toString());
            } catch (Exception e) {
                Log.d("AppDebugger", "the benchmarkIntent is null :(");
            }
        }
        /**
         * Measure the deltas of tx sizes
         * while (previous-now) > delta) {
         *  move now to previous
         *  get new now
         * }
         */
        // just a small fail-safe, I know this technically adds to the loading time,
        // but I want to rule out some silly loop problems.
        int i = 0;
            while(Math.abs(nowTx - previousTx) > deltaTx) {
                if(10000 > i) {
                    previousTx = nowTx;
                    nowTx = this.benchmarker.trafficMonitor.getTxBytes();
                    Log.d("AppDebugger", "now: " + Long.toString(nowTx) + " ---- previous: " + Long.toString(previousTx));
                    i++;
                } else {
                    break;
                }
            }
        /**
         * grab a new timestamp, our app is "done loading"
         */
        this.endTs = System.currentTimeMillis();
        Long total = this.endTs - this.startTs;
        Log.d("AppDebugger", "It took " + this.app.getPackageName() + " " + Long.toString(total) + " milliseconds to load");
        /**
         * Store our results in our table
         * The nowTx will always house our last
         * amount of traffic sent
         */
        //this.statsDataSource.createStats(startTs, endTs, this.app.getPackageName(), nowTx);
        Toast.makeText(this.context, this.app.getLabel() + " is done loading", Toast.LENGTH_SHORT).show();
        /**
         *  We don't need to kill the service, we can continually
         *  call this and pass data into this service
         */
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        //TODO for communication return IBinder implementation
        return null;
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

    public void onDestroy() {
        this.statsDataSource.close();
    }
}