package com.cs253.appdebugger;

import com.cs253.appdebugger.benchmarking.Benchmarker;
import com.cs253.appdebugger.database.MonitorDataSource;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import java.util.List;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.CheckBox;
import android.content.Context;
import android.view.View.OnClickListener;
import com.cs253.appdebugger.database.Monitor;
import com.cs253.appdebugger.other.ParcelableApp;
//import com.jjoe64.graphview.GraphView;
//import com.jjoe64.graphview.GraphViewSeries;
//import com.jjoe64.graphview.LineGraphView;

import android.app.ActivityManager.RunningTaskInfo;
import android.app.ActivityManager;

/**
 * Created by jason on 10/22/13.
 */
public class AppDetails extends Activity {

    App app;
    String packageName;
    List<PackageInfo> packs;
    PackageManager packageManager;
    Monitor monitor;
    String appName;
    int appVersion;
    boolean active;
    TextView tvAppName;
    TextView tvAppVersion;
    TextView tvAppUid;
    Context context;
    CheckBox cbMonitorApp;
    Bundle extras;
    MonitorDataSource mds;
    Benchmarker benchmarker;

    protected void onCreate(Bundle savedInstance) {
        // call our parent
        super.onCreate(savedInstance);

        // get our intent extras that we send to this class
        this.extras = getIntent().getExtras();
        // Set our application context, should we need it elsewhere in this class
        this.context = getApplicationContext();

        /** Set up our database connections **/
        this.mds = new MonitorDataSource(this);
        this.mds.open();

        // Grab our app from our intent
        ParcelableApp pa = this.extras.getParcelable("app");
        this.app = pa.getApp();

        // Initialize our monitor, so we know what the status of this app is
        this.monitor = this.mds.selectMonitorByAppName(this.app.getPackageName());
        this.active = this.monitor.getActive();
        this.appName = this.monitor.getAppName();

        // Draw our app details activity
        setContentView(R.layout.activity_appdetails);

        // Set some of our activity's views
        this.tvAppName = (TextView) findViewById(R.id.textViewAppName);
        this.tvAppUid = (TextView) findViewById(R.id.textViewAppUid);
        this.tvAppVersion = (TextView) findViewById(R.id.textViewAppVersion);

        /**https://github.com/jjoe64/GraphView**/
        // init example series data
        /*
        GraphViewSeries exampleSeries = new GraphViewSeries(new GraphView.GraphViewData[] {
                new GraphView.GraphViewData(1, 2.0d)
                , new GraphView.GraphViewData(2, 1.5d)
                , new GraphView.GraphViewData(3, 2.5d)
                , new GraphView.GraphViewData(4, 1.0d)
        });

        GraphView graphView = new LineGraphView(
                this // context
                , "GraphViewDemo" // heading
        );
        graphView.addSeries(exampleSeries); // data

        LinearLayout layout = (LinearLayout) findViewById(R.id.grapher);
        layout.addView(graphView);
*/

        /** Deprecated for now **/
        /*
        this.cbMonitorApp = (CheckBox) findViewById(R.id.checkboxAppDebug);
        // Initiate the onclicklistener for our checkbox
        this.cbMonitorApp.setOnClickListener(this);
        // Set our check box based on the active flag
        this.cbMonitorApp.setChecked(this.active);
        */
        /** end deprecation **/

        // Add to those views what we need
        this.tvAppVersion.setText(Integer.toString(this.app.getUid()));
        this.tvAppName.setText(this.app.getLabel());
        this.tvAppVersion.setText(Integer.toString(this.app.getVersionCode()));
        // this.cbMonitorApp.setText("Turn debugging on for: " + this.app.getLabel() + "?");
        //checkForOtherActivities();
    }

    /**
     * Deprecated for now
     *
     * We must implement OnClickListener if we want to use this!
     */
    /*
    public void onClick(View v) {
        CheckBox checkBox = (CheckBox) v;
        Monitor m = this.mds.selectMonitorByAppName(this.getPackageName());
        // Check to see if it was already checked
        // if it is, we want to stop monitoring the app
        // if it isn't we want to start monitoring the app
        if(checkBox.isChecked()) {
            this.mds.createOrUpdate(this.app.getPackageName(), false, "");
        } else {
            if(this.mds.deactivateMonitor(this.app.getPackageName())) {
                Toast.makeText(this.context, "App has been successfully removed from debugging", Toast.LENGTH_LONG).show();
            }
        }
    }
*/

    public void onDestroy() {
        super.onDestroy();
        this.mds.close();
    }
}
