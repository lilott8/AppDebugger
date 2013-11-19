package com.cs253.appdebugger;

import android.app.Application;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

import com.cs253.appdebugger.GoogleFormUploader.GoogleFormUploader;
import com.cs253.appdebugger.benchmarking.Benchmarker;
import com.cs253.appdebugger.benchmarking.NetworkMonitor;
import com.cs253.appdebugger.database.Stats;
import com.cs253.appdebugger.database.StatsDataSource;
import com.cs253.appdebugger.benchmarking.Logger;
import com.cs253.appdebugger.other.ParcelableApp;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.Arrays;

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
    private App app;
    private long appLoadTime;
    private long nicLoadTime;
    /**
     *
     * @param intent
     * @param flags
     * @param startID
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        long[] times = new long[4];
        // Just a way to group and consolidate various actions
        this.initializeVariables();

        // get our intent extras that we send to this class
        this.extras = intent.getExtras();
        ParcelableApp pa = this.extras.getParcelable("app");
        this.app = pa.getApp();
        this.benchmarker = new Benchmarker(this.app, this);
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
        // get our nic state change time
        this.nicLoadTime = System.currentTimeMillis();
        this.benchmarker.nm.measureNetworkState();
        this.nicLoadTime = System.currentTimeMillis() - this.nicLoadTime;

        // get our network load time!
        this.appLoadTime = System.currentTimeMillis();
        this.benchmarker.nm.measureNetworkUse();
        this.appLoadTime = System.currentTimeMillis() - this.appLoadTime;

        Log.d("AppDebugger", "It took " + this.app.getPackageName() + " " +
                Long.toString(this.appLoadTime) + " milliseconds to load");

        // Store our results in our table
        // The totalTx will be the initial - now
        // amount of traffic sent
        Stats s = this.statsDataSource.createStats(this.appLoadTime, this.app.getPackageName(),
                this.benchmarker.nm.getTotalBytesSent(), this.nicLoadTime);

        if (s.getId() > 0) {
            Log.d("AppDebugger", "Our insert happened successfully");
        } else {
            Log.e("AppDebugger", "Our insert failed :(");
        }
        //Toast.makeText(this.context, this.app.getLabel() + " is done loading", Toast.LENGTH_SHORT).show();
        this.postDataToForm();

        return START_STICKY;
    }

    /**
     *  Because there is no constructor here, I made this
     *  That way we can simplify the creation of variables
     *  and then allow for more logic based instructions
     *  to reside in the "onCreate()" method
     */
    private void initializeVariables() {
        // initialize our data sources so we have DB connectivity
        this.statsDataSource = new StatsDataSource(this);
        this.statsDataSource.open();
        // Our databases are now loaded
        // get our application context
        this.parentApp = getApplication();
        this.context = this.parentApp.getApplicationContext();
    }

    public void measureNetworkState() {

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
        // Close our data source when the service is ended
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
        // AppLoadTime
        uploader.addEntry("955660800", Long.toString(this.appLoadTime));
        // NicLoadTime
        uploader.addEntry("1981683208", Long.toString(this.nicLoadTime));
        // TotalDataSent
        uploader.addEntry("1320577572", Long.toString(this.benchmarker.nm.getTotalBytesSent()));
        // Android Version
        uploader.addEntry("1037276429", Build.VERSION.RELEASE);
        // Upload the data
        uploader.upload();
    }
}