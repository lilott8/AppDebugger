package com.cs253.appdebugger;

import com.cs253.appdebugger.benchmarking.Benchmarker;
import com.cs253.appdebugger.database.MonitorDataSource;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.app.Activity;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import android.widget.Toast;

import java.io.Serializable;
import java.util.List;

import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;
import android.widget.TextView;
import android.widget.CheckBox;
import android.content.Context;
import android.view.View.OnClickListener;
import com.cs253.appdebugger.database.Monitor;
import com.cs253.appdebugger.other.ParcelableApp;

import android.app.ActivityManager.RunningTaskInfo;
import android.app.ActivityManager;

import org.json.JSONObject;

/**
 * Created by jason on 10/22/13.
 */
public class AppDetails extends Activity implements OnClickListener {

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

        // Set up our packageManager, this will give us access to
        // query the packages and grab the necessary data
        initializePackageManager();
        initializeSelectedApplication();

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
        this.cbMonitorApp = (CheckBox) findViewById(R.id.checkboxAppDebug);
        // Initiate the onclicklistener for our checkbox
        this.cbMonitorApp.setOnClickListener(this);
        // Set our check box based on the active flag
        this.cbMonitorApp.setChecked(this.active);

        // Log.d("AppDebugger", "Uid is: " + Integer.toString(this.app.getUid()));
        // Toast.makeText(this.context, "PackageName: " + this.app.getPackageName(), Toast.LENGTH_SHORT).show();

        // Add to those views what we need
        this.tvAppVersion.setText(Integer.toString(this.app.getUid()));
        this.tvAppName.setText(this.app.getLabel());
        this.tvAppVersion.setText(Integer.toString(this.app.getVersionCode()));
        this.cbMonitorApp.setText("Turn debugging on for: " + this.app.getLabel() + "?");
        //Toast.makeText(this.appContext, this.getAppName(), Toast.LENGTH_LONG).show();
        //checkForOtherActivities();
    }

    private void initializeSelectedApplication() {
        try {
            // get our package that the user selected
            this.packageName = this.extras.getString("packageName");
            try {
                // Let's create a new app object
                this.app = new App();
                // Get the application info from our package manager
                ApplicationInfo ai = this.packageManager.getApplicationInfo(this.packageName, PackageManager.GET_META_DATA);
                // Get the package info from our package manager
                PackageInfo pi = this.packageManager.getPackageInfo(this.packageName, 0);

                this.app.setPackageName(pi.packageName);
                if(this.app.getPackageName().length() < 1) {
                    this.app.setPackageName(pi.applicationInfo.name);
                }
                //this.app.setTitle(a.loadLabel(this.packageManager).toString());
                this.app.setVersionName(pi.versionName);
                this.app.setVersionCode(pi.versionCode);
                // Toast.makeText(this.context, String.valueOf(ai.uid), Toast.LENGTH_SHORT).show();
                this.app.setUid(ai.uid);
                //this.app.setTitle(a.applicationInfo.)

                CharSequence label = ai.loadLabel(this.packageManager);
                this.app.setLabel(label != null ? label.toString() : "No label provided");

                CharSequence description = ai.loadDescription(this.packageManager);
                this.app.setDescription(description != null ? description.toString() : "No description available");

                // this.packageManager.getApplicationInfo(this.packageName, PackageManager.GET_META_DATA);
                // setAppName(this.app.packageName);
                // setAppVersion(this.app.targetSdkVersion);
            } catch(NameNotFoundException e) {
                Log.e("ERROR", "AppDebugger: "+e.toString());
            }
        } catch(NullPointerException e) {
            Log.e("ERROR", "AppDebugger: "+e.toString());
        }
    }

    protected void initializePackageManager() {
        // Load the packagemanager this has all our installed apps
        this.packageManager = getPackageManager();

        // Load our packageManager
        try {
            this.packs = this.packageManager.getInstalledPackages(0);
        } catch (Exception e) {
            Log.e("ERROR", "PackageManager could not get installed packages on line: " + e.toString());
        }
    }

    /**
     * BenchmarkApp works through 10 Steps, in which the first and last steps of the
     * process are completed here.  The steps are starting the service and killing the
     * service.
     */
    public void benchmarkApp(View v) {
        ParcelableApp pa = new ParcelableApp(this.app);
        // Initialize an intent to our service that will monitor for stats gathering
        Intent intent = new Intent(getApplicationContext(), AppBenchmarkService.class);
        // Put our app name in our intent so we have access to it in our service
        intent.putExtra("app", pa);
        // start our service
        this.context.startService(intent);
        /*
        // Toast.makeText(this.appContext, "We will be debugging: " + this.app.getLabel(), Toast.LENGTH_LONG).show();
        long txData;
        if((txData = this.benchmarker.trafficMonitor.getTxBytes()) <= 0) {
            txData = this.benchmarker.trafficMonitor.getTxBytesManual();
        }
        Log.d("AppDebugger", "Traffic transmitted at this epoch: " + Long.toString(txData));
        */
    }

    /**
     * Launch our App with no debugging...
     */
    public void launchApp(View v) {
        Toast.makeText(this.context, "This is disabled for now", Toast.LENGTH_SHORT).show();
        // Intent intent = this.packageManager.getLaunchIntentForPackage(this.app.getPackageName());
        // startActivity(intent);
    }

    /**
     * This is what requires the most work. So put cool code here!
     * @param v
     */
    public void monitorApp(View v) {
        Toast.makeText(getApplicationContext(), "Debugging: " + this.app.getLabel(), Toast.LENGTH_LONG).show();
        long data = this.benchmarker.trafficMonitor.getTxBytes();
        Toast.makeText(getApplicationContext(), "Hey, working?" + String.valueOf(data), Toast.LENGTH_SHORT).show();

        //TODO: parse logcat for specific app
        //TODO: collect network traffic for specific app
        //TODO: get application activities for specific app
    }

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

    public void checkForOtherActivities() {
        ActivityManager am = (ActivityManager)this.getSystemService(ACTIVITY_SERVICE);
        List<RunningTaskInfo> a = am.getRunningTasks(Integer.MAX_VALUE);

        for(int i=0;i<a.size();i++){

            String packageName = a.get(i).topActivity.getPackageName();
            try {
            //String appName = (String) this.packageManager.getApplicationLabel(this.packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA));
            Log.d("Executed app", "Application executed : " +a.get(i).baseActivity.toShortString()+ "\t\t ID: "+a.get(i).id+"");
            } catch (NullPointerException e) {

            }
            //Drawable d=pack.getApplicationIcon(packageName);

            Log.v("details"," "+packageName+" "+appName);

        }

    }

    public void onDestroy() {
        super.onDestroy();
        this.mds.close();
    }
}
