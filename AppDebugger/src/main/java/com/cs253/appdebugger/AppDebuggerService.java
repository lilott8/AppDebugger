package com.cs253.appdebugger;

import android.app.Application;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;

import com.cs253.appdebugger.database.MonitorDataSource;
import com.cs253.appdebugger.database.StatsDataSource;
import com.cs253.appdebugger.benchmarking.Debugger;
import com.cs253.appdebugger.benchmarking.Logger;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

/**
 * Created by jason on 10/24/13.
 */
public class AppDebuggerService extends Service {

    // Our database connections
    private StatsDataSource statsDataSource;
    private MonitorDataSource monitorDataSource;
    // Probably not needed, this is supposed to be a wrapper for all benchmarking
    // But this may provide too much overhead for what its worth and complicate
    // things more than they need to be.
    private Debugger eventParser;
    // Only used for Toast messages
    private Application parentApp;
    private Context context;
    // Logger for parsing our logcat
    private Logger logger;

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
        this.monitorDataSource = new MonitorDataSource(this);
        this.monitorDataSource.open();
        this.statsDataSource.open();
        // Our databases are now loaded
        // get our application context
        this.parentApp = getApplication();
        this.context = parentApp.getApplicationContext();
        // get a new logger
        this.logger = new Logger("com.facebook.katana", "V");

        //TODO get the list of apps that are monitored
        //this.apps = this.monitorDataSource.getAllActiveApps();
        //this.eventParser = new Debugger(this.apps);

        this.logger.readALog();
        //while(this.logger.readALog()) {

        //}

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
        this.monitorDataSource.close();
    }
    /**
     * Get the list of all apps that are to be monitored from our monitor DB
     * @return
     */
    private List<App> getDebuggedApps() {
        List<App> apps = new ArrayList<App>();

        return apps;
    }
}