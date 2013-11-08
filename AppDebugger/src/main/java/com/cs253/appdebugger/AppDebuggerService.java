package com.cs253.appdebugger;

import android.app.Application;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;

import com.cs253.appdebugger.database.MonitorDataSource;
import com.cs253.appdebugger.database.StatsDataSource;
import com.cs253.appdebugger.debugging.Debugger;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;

/**
 * Created by jason on 10/24/13.
 */
public class AppDebuggerService extends Service {

    private StatsDataSource statsDataSource;
    private MonitorDataSource monitorDataSource;
    private Debugger eventParser;
    private ArrayList<App> apps;
    private Application parentApp;
    private Context context;

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        // initialize our data sources so we have DB connectivity
        this.statsDataSource = new StatsDataSource(this);
        this.monitorDataSource = new MonitorDataSource(this);
        this.monitorDataSource.open();
        this.statsDataSource.open();
        this.parentApp = getApplication();
        this.context = parentApp.getApplicationContext();

        //TODO get the list of apps that are monitored
        //this.apps = this.monitorDataSource.getAllActiveApps();
        //this.eventParser = new Debugger(this.apps);


        //TODO this will control our logic
        new readLogsTask().execute();
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
        @Override
        protected Void doInBackground(Void... voids) {
            //Toast.makeText(getApplicationContext(), "We are in the doinbackground", Toast.LENGTH_SHORT).show();
            try
            {

            }
            catch (Exception e)
            {
                //Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
            return null;
        }
    }

    List<App> getDebuggedApps() {
        List<App> apps = new ArrayList<App>();

        return apps;
    }
}