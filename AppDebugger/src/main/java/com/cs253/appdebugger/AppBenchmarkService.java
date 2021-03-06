package com.cs253.appdebugger;

import android.app.ActivityManager;
import android.app.Application;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

import com.cs253.appdebugger.GoogleFormUploader.GoogleFormUploader;
import com.cs253.appdebugger.benchmarking.Benchmarker;
import com.cs253.appdebugger.database.Stats;
import com.cs253.appdebugger.database.StatsDataSource;
import com.cs253.appdebugger.other.AeSimpleSHA1;
import com.cs253.appdebugger.other.Commander;
import com.cs253.appdebugger.other.ParcelableApp;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by jason on 10/24/13.
 */
public class AppBenchmarkService extends Service implements View.OnTouchListener{

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
    // window manager
    private WindowManager mWindowManagerLeft;
    private WindowManager mWindowManagerRight;
    // linear layout will use to detect touch event
    private LinearLayout touchLayoutLeft;
    private LinearLayout touchLayoutRight;
    private boolean touched;


    @Override
    public void onCreate() {
        super.onCreate();
        // Just a way to group and consolidate various actions
        this.initializeVariables();
        // create the linearlayout
        /*
        this.touchLayoutLeft = new LinearLayout(this);
        this.touchLayoutRight = new LinearLayout(this);
        // set the parameters for our layout
        LinearLayout.LayoutParams lpl = new LinearLayout.LayoutParams(30, ViewGroup.LayoutParams.MATCH_PARENT);
        LinearLayout.LayoutParams lpr = new LinearLayout.LayoutParams(30, ViewGroup.LayoutParams.MATCH_PARENT);
        // Add our layout params to our layout
        this.touchLayoutLeft.setLayoutParams(lpl);
        this.touchLayoutRight.setLayoutParams(lpr);
        // Just for troubleshooting
        this.touchLayoutLeft.setBackgroundColor(Color.LTGRAY);
        this.touchLayoutLeft.setBackgroundColor(Color.LTGRAY);
        // set our on touch listener
        this.touchLayoutLeft.setOnTouchListener(this);
        this.touchLayoutRight.setOnTouchListener(this);
        // get our new windowmanager object
        this.mWindowManagerLeft = (WindowManager) getSystemService(WINDOW_SERVICE);
        this.mWindowManagerRight = (WindowManager) getSystemService(WINDOW_SERVICE);
        // Create a window manager to place our new view on
        WindowManager.LayoutParams mParamsLeft = new WindowManager.LayoutParams(30,
                // our width variable, set our type to phone, non application windows providing user interaction
                WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.TYPE_PHONE,
                // this window will never get focus, invisible to the user
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        // Create a window manager to place our new view on
        WindowManager.LayoutParams mParamsRight = new WindowManager.LayoutParams(30,
                // our width variable, set our type to phone, non application windows providing user interaction
                WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.TYPE_PHONE,
                // this window will never get focus, invisible to the user
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);

        // Set where the view will live
        mParamsLeft.gravity = Gravity.LEFT | Gravity.TOP;
        mParamsRight.gravity = Gravity.RIGHT | Gravity.TOP;
        //  attach our layout to our window manager
        this.mWindowManagerLeft.addView(this.touchLayoutLeft, mParamsLeft);
        this.mWindowManagerRight.addView(this.touchLayoutRight, mParamsRight);
        */
    }

    /**
     *
     * @param intent
     * @param flags
     * @param startID
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        try{
            // get our intent extras that we send to this class
            this.extras = intent.getExtras();
            ParcelableApp pa = this.extras.getParcelable("app");
            this.app = pa.getApp();
            // This is our benchmarker.  It allows us to do various benchmarking
            // functions, read logs, monitor network traffic, etc
            this.benchmarker = new Benchmarker(this.app, this);
            // We want to thread this so we don't get in trouble from android
            // and so we don't affect the results!
            Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                runMeasurement();
           }
        });
            // Start our thread
            t.start();
        } catch (Exception e) {
            Log.e("AppDebugger", "Our parcelable app is null");
        }
        // This will only allow for the service to be restarted explicitly
        return START_NOT_STICKY;
        // This will restart the service with the previous intent
        // return START_REDELIVER_INTENT;
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
        try {
            this.context = this.parentApp.getApplicationContext();
        } catch (NullPointerException e) {
            Log.e("AppDebugger", "Our application context is null");
        }
        // this.touched = false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        //TODO for communication return IBinder implementation
        return null;
    }

    public void onDestroy() {
        if(this.mWindowManagerLeft != null) {
            if (this.touchLayoutLeft != null) {
                this.mWindowManagerLeft.removeView(this.touchLayoutLeft);
            }
        }
        if(this.mWindowManagerRight != null) {
            if(this.touchLayoutRight != null) {
                this.mWindowManagerRight.removeView(this.touchLayoutRight);
            }
        }
        // Close our data source when the service is ended
        this.statsDataSource.close();
        super.onDestroy();
    }



    public void postDataToForm() {
        GoogleFormUploader uploader = new GoogleFormUploader("1wfat45AiYejUmO2yRu5Rj3kOcy-IGgVdyTKr13I5wwo");
        // Phone UID
        try{
            final TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            // SHA1 our deviceID for safety purposes
            String deviceID = AeSimpleSHA1.SHA1(tm.getDeviceId());
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
        // WhichNic?
        uploader.addEntry("1393503322", this.benchmarker.nm.getWhichNic());
        // Android Version
        uploader.addEntry("1037276429", Build.VERSION.RELEASE);
        // Make and Build of the model
        uploader.addEntry("1320755101", Build.MANUFACTURER + "-" + Build.PRODUCT);
        // Upload the data
        uploader.upload();
    }

    /**
     * This is a threaded function call,
     * It should never be invoked outside of a thread,
     * Otherwise Android will yell at you about doing
     * much work on the main thread.
     */
    public void runMeasurement() {
        Intent benchmarkIntent = getPackageManager().getLaunchIntentForPackage(this.app.getPackageName());
        if(benchmarkIntent != null) {
            startActivity(benchmarkIntent);
        } else {
            Log.e("AppDebugger", "Benchmark Intent is null!");
            return;
        }
        // This has to be here because our PID isn't set until
        // the intent is completed
        this.getPid();
        //this.getAddressesAndPorts();
        this.measureTheNic();

        // get our network load time!
        // while time is not out and our traffic is 0 then do this
        this.appLoadTime = System.currentTimeMillis();
        double seconds = this.benchmarker.nm.measureNetworkUse();
        if(seconds == 0 && this.benchmarker.nm.getTotalBytesSent() == 0){
            this.appLoadTime = Math.abs(System.currentTimeMillis() - this.appLoadTime) - 10000;
        } else {
            this.appLoadTime = Math.abs((System.currentTimeMillis() - this.appLoadTime) - (long)seconds);
        }

        Log.d("AppDebugger", "It took " + this.app.getPackageName() + " " +
                Long.toString(this.appLoadTime) + " milliseconds to load");

        // Store our results in our table
        // The totalTx will be the initial - now
        // amount of traffic sent
        Stats s = this.statsDataSource.createStats(this.appLoadTime, this.app.getPackageName(),
                this.benchmarker.nm.getTotalBytesSent(), this.nicLoadTime, this.benchmarker.nm.getWhichNic());

        //Toast.makeText(this.context, this.app.getLabel() + " is done loading", Toast.LENGTH_SHORT).show();
        this.postDataToForm();
    }

    public void measureTheNic() {
        // get our nic state change time
        this.nicLoadTime = System.currentTimeMillis();
        this.benchmarker.nm.measureNetworkState();
        this.nicLoadTime = System.currentTimeMillis() - this.nicLoadTime;
        //Log.d("AppDebugger", "It took " + Long.toString(this.nicLoadTime) +
        //        " milliseconds to turn on a nic");
    }
    // https://github.com/kpbird/android-global-touchevent
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        this.touched = true;
        if(event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_UP) {
            Log.i("AppDebugger", "Action :" + event.getAction() + "\t X :" + event.getRawX() + "\t Y :"+ event.getRawY());
        }
        return true;
    }

    public void getAddressesAndPorts() {
        Commander c = new Commander();
        //String result = c.executeCommand(new String[]{"top"});
        String result = c.executeCommand(this.app.getPid());
        //Log.d("AppDebugger", "result: " + result);
    }

    public void getPid() {
        ActivityManager manager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> services = manager.getRunningAppProcesses();

        try {
            int i = 0;
            Log.d("AppDebugger", "Size of services: " + services.size());
            while(services.get(i).uid != this.app.getUid() && i<services.size()-1) {
                i++;
            }//while
            Log.d("AppDebugger", "i: " + i);
            try {
                this.app.setPid(services.get(i).pid);
                Log.d("AppDebugger", "Pid: " + this.app.getPid());
            } catch(IndexOutOfBoundsException e) {
                Log.d("AppDebugger", "Our index is out of bounds!");
            }//indexoutofbounds catch
        } catch (NullPointerException e) {
            Log.d("AppDebugger", "NullPointer for our service size???");
        }//nullpointerexception catch
        Log.d("AppDebugger", "The pid for " + this.app.getTitle() + " is " + this.app.getPid());
    }
}