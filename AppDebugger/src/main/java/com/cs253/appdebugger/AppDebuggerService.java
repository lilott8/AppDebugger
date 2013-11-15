package com.cs253.appdebugger;

import android.app.Application;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;

import com.cs253.appdebugger.benchmarking.Benchmarker;
import com.cs253.appdebugger.database.MonitorDataSource;
import com.cs253.appdebugger.database.StatsDataSource;
import com.cs253.appdebugger.benchmarking.Logger;

import java.security.Timestamp;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by jason on 10/24/13.
 */
public class AppDebuggerService extends Service {

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

    /**
     *
     * @param intent
     * @param flags
     * @param startID
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        // initialize our data sources so we have DB connectivity
        this.statsDataSource = new StatsDataSource(this);
        this.statsDataSource.open();
        // Our databases are now loaded
        // get our application context
        this.parentApp = getApplication();
        this.context = this.parentApp.getApplicationContext();
        // get our intent extras that we send to this class
        this.extras = intent.getExtras();
        this.packageName = this.extras.getString("app");
        this.benchmarker = new Benchmarker();

        /**
         * Grab the timestamp of before we load our system
         */
        this.startTs = System.currentTimeMillis();
        /**
         * Get the tx size for our app
         */

        /**
         * Start the app
         */
        Intent benchmarkIntent = getPackageManager().getLaunchIntentForPackage(this.packageName);
        startActivity(benchmarkIntent);
        /**
         * Measure the deltas of tx sizes
         * while (previous-now) > delta) {
         *  move now to previous
         *  get new now
         * }
         */

        /**
         * grab a new timestamp, our app is "done loading"
         */
        this.endTs = System.currentTimeMillis();
        /**
         * Store our results in our table
         */

        /**
         * Kill the service
         */


        //TODO this will control our logic
        // new readLogsTask().execute();
        //Toast.makeText(getApplicationContext(), "service started...", Toast.LENGTH_LONG).show();
        return Service.START_NOT_STICKY;
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