package com.cs253.appdebugger;

import android.app.Application;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

import com.cs253.appdebugger.GoogleFormUploader.GoogleFormUploader;
import com.cs253.appdebugger.benchmarking.Benchmarker;
import com.cs253.appdebugger.database.Stats;
import com.cs253.appdebugger.database.StatsDataSource;
import com.cs253.appdebugger.benchmarking.Logger;
import com.cs253.appdebugger.other.HttpRequest;
import com.cs253.appdebugger.other.ParcelableApp;

import java.security.Timestamp;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.telephony.TelephonyManager;
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
    private long totalTx;
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
        // Just a way to group and consolidate various actions
        this.initializeVariables();

        // get our intent extras that we send to this class
        this.extras = intent.getExtras();
        ParcelableApp pa = this.extras.getParcelable("app");
        this.app = pa.getApp();
        this.benchmarker = new Benchmarker(this.app);
        // Toast.makeText(this.context, "The package name is: "+this.app.getPackageName(), Toast.LENGTH_SHORT).show();
/*
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                measureNetworkUse();
           }
        });
        t.start();
*/
        this.measureNetworkUse();
        /**
         * Store our results in our table
         * The totalTx will be the initial - now
         * amount of traffic sent
         */
        Stats s = this.statsDataSource.createStats(this.startTs, this.endTs, this.app.getPackageName(), totalTx);
        if (s.getId() > 0) {
            Log.d("AppDebugger", "Our insert happened successfully");
        } else {
            Log.d("AppDebugger", "Our insert failed :(");
        }
        //Toast.makeText(this.context, this.app.getLabel() + " is done loading", Toast.LENGTH_SHORT).show();
        this.postDataToForm();
        /**
         *  We don't need to kill the service, we can continually
         *  call this and pass data into this service
         */
        return START_STICKY;
    }

    private void initializeVariables() {
        // initialize our data sources so we have DB connectivity
        this.statsDataSource = new StatsDataSource(this);
        this.statsDataSource.open();
        // Our databases are now loaded
        // get our application context
        this.parentApp = getApplication();
        this.context = this.parentApp.getApplicationContext();
    }

    private void measureNetworkUse() {
        /**
         *  These three longs will help us determine
         *  the loading of an app.  When the size
         *  between nowTx and PreviousTx are less
         *  than delta, our app is "done" loading
         */
        long initialTx = 0;
        long nowTx = 0;
        long previousTx = 0;
        long deltaTx = 100;
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
            Log.d("AppDebugger", "Benchmark Intent is null!");
        }
        // just a small fail-safe, I know this technically adds to the loading time,
        // but I want to rule out some silly loop problems.
        int i = 0;
        // This is our initial data sent, this will be used to calculate our total
        // data sent for this app.
        initialTx = this.benchmarker.trafficMonitor.getTxBytes();
        /**
         * Measure the deltas of tx sizes
         * while (previous-now) > delta) {
         *  move now to previous
         *  get new now
         * }
         */
        while((Math.abs(nowTx - previousTx) > deltaTx) || i < 1000) {
            if(10000 > i) {
                previousTx = nowTx;
                nowTx = this.benchmarker.trafficMonitor.getTxBytes();
                //Log.d("AppDebugger", "now: " + Long.toString(nowTx) + " ---- previous: " + Long.toString(previousTx));
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

        this.totalTx = Math.abs(nowTx - initialTx);
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



    public void postDataToForm() {
        GoogleFormUploader uploader = new GoogleFormUploader("1wfat45AiYejUmO2yRu5Rj3kOcy-IGgVdyTKr13I5wwo");
        // Phone UID
        try{
            final TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            String deviceID = tm.getDeviceId();
            uploader.addEntry("1592233968", deviceID);
        } catch (Exception e) {
            uploader.addEntry("1592233968", "handledexception");
        }
        // PackageName
        uploader.addEntry("1419678310", this.app.getPackageName());
        // StartTs
        uploader.addEntry("955660800", Long.toString(this.startTs));
        // EndTs
        uploader.addEntry("1827012614", Long.toString(this.endTs));
        // TotalTx
        uploader.addEntry("1320577572", Long.toString(this.totalTx));
        // Android Version
        uploader.addEntry("1037276429", Build.VERSION.RELEASE);
        // Upload the data
        uploader.upload();
    }
}