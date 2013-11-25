package com.cs253.appdebugger.benchmarking;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.util.Log;

import com.cs253.appdebugger.App;
import com.cs253.appdebugger.NetworkConnectivityListener;
import com.cs253.appdebugger.other.Connectivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by jason on 11/13/13.
 */
public class NetworkMonitor extends Benchmarker{

    private App app;
    private TrafficStats trafficStats;
    private long totalBytesSent;
    private Context context;

    public NetworkMonitor(App whichApp, Context c) {
        super();
        this.app = whichApp;
        this.trafficStats = new TrafficStats();
        this.context = c;
        try {} catch(NullPointerException e) {}
    }

    /**
     * This method gets the total bytes transmitted by an app given a uid,
     * if the API does not return a valid value, then we brute force the
     * retrieval of the network statistics by going out to disk
     * @return
     *  a long that is the bytes that have been transmitted per app
     */
    public long getTxBytes() {
        long bytes = 0;

        if((bytes = this.trafficStats.getUidTxBytes(this.app.getUid())) < 1) {
            bytes = this.getTxBytesManual();
        }
        return bytes;
    }

    private Long getTxBytesManual(){

        int localUid = this.app.getUid();

        File dir = new File("/proc/uid_stat/");
        String[] children = dir.list();
        if(!Arrays.asList(children).contains(String.valueOf(localUid))){
            return 0L;
        }
        File uidFileDir = new File("/proc/uid_stat/"+String.valueOf(localUid));
        File uidActualFileReceived = new File(uidFileDir,"tcp_rcv");
        File uidActualFileSent = new File(uidFileDir,"tcp_snd");

        String textReceived = "0";
        String textSent = "0";

        try {
            BufferedReader brReceived = new BufferedReader(new FileReader(uidActualFileReceived));
            BufferedReader brSent = new BufferedReader(new FileReader(uidActualFileSent));
            String receivedLine;
            String sentLine;

            if ((receivedLine = brReceived.readLine()) != null) {
                textReceived = receivedLine;
            }
            if ((sentLine = brSent.readLine()) != null) {
                textSent = sentLine;
            }

        }
        catch (IOException e) {

        }
        return Long.valueOf(textReceived).longValue() + Long.valueOf(textReceived).longValue();

    }

    /*************************************************************************************/

    /**
     * Measures the time it takes from a NIC to go
     * from standby/powered down to up and functional
     * @return
     *  time for state to move from down to up
     */
    public long monitorNetworkState() {
        return 0;
    }

    /**
     *  TODO: get this working!
     * @return
     *  long of something
     */
    public void measureNetworkState() {
        int i=0;
        while(!Connectivity.isConnected(this.context) || i < 100) {
            i++;
        }
        Log.d("AppDebugger", "leaving measureNetworkState");
    }

    /**
     * Measure how much traffic is used on an app
     * @return
     *  long of bytes used
     */
    public void measureNetworkUse() {
        // These four longs will help us determine
        // the loading of an app.  When the size
        // between nowTx and PreviousTx are less
        // than delta, our app is "done" loading
        long initialTx = 0;
        long nowTx = 0;
        long previousTx = 0;
        long deltaTx = 1;
        // This is our initial data sent, this will be used to calculate our total
        // data sent for this app.
        initialTx = this.getTxBytes();
        // Get the tx size for our app
        nowTx = this.getTxBytes();
        // just a small fail-safe, I know this technically adds to the loading time,
        // but I want to rule out some silly loop problems.
        int i = 0;
        /**
         * Measure the deltas of tx sizes
         * while (previous-now) > delta) {
         *  move now to previous
         *  get new now
         * }
         */
        while((Math.abs(nowTx - previousTx) > deltaTx)) {
            //if(10000 > i) {
            previousTx = nowTx;
            nowTx = this.getTxBytes();
            //Log.d("AppDebugger", "now: " + Long.toString(nowTx) + " ---- previous: " + Long.toString(previousTx));
            i++;
            //} else {
            //    break;
            //}
            this.totalBytesSent = Math.abs(nowTx - initialTx);
        }
    }

    public long getTotalBytesSent(){
        return this.totalBytesSent;
    }

    public String getWhichNic() {
        try {
            if(Connectivity.isConnectedMobile(this.context)) {
                return "MOBILE";
            } else {
                return "WIFI";
            }
        } catch(NullPointerException e) {
            Log.d("AppDebugger", "We couldn't get the nic type");
            return "nan";
        }
    }

}