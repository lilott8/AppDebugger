package com.cs253.appdebugger.benchmarking;

import android.content.Context;
import android.net.TrafficStats;
import android.util.Log;

import com.cs253.appdebugger.App;
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
    String TAG = "AppDebugger";

    public NetworkMonitor(App whichApp, Context c) {
        super();
        this.app = whichApp;
        //this.trafficStats = new TrafficStats();
        this.context = c;
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
        //return TrafficStats.getTotalTxBytes();

        if((bytes = TrafficStats.getUidTxBytes(this.app.getUid())) < 1) {
            bytes = this.getTxBytesManual();
        }
        if(bytes == 0) {
            bytes = TrafficStats.getTotalTxBytes();
            //Log.d(this.TAG, "We are grabbing total phone tx bytes");
        }
        return bytes;

    }

    public long bytesTransmitted(int i) {
        switch(i) {
            case 1:
                return TrafficStats.getTotalTxBytes();
            case 2:
                return getTxBytesManual();
            case 3:
                return TrafficStats.getUidTxBytes(this.app.getUid());
        }
        return 0L;
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
        while(!Connectivity.isConnected(this.context)) {
            // empty loop to calculate the time it takes to turn
            // the NIC from a SLEEP state to an ON state
        }
    }

    /**
     * Measure how much traffic is used on an app
     * @return
     *  long of bytes used
     */
    public double measureNetworkUse() {
        // These four longs will help us determine
        // the loading of an app.  When the size
        // between nowTx and PreviousTx are less
        // than delta, our app is "done" loading
        long previousTx = 0;
        long deltaTx = 0;
        // This is our initial data sent, this will be used to calculate our total
        long initialTx = this.getTxBytes();
        // data sent for this app.
        // Get the tx size for our app
        long nowTx = this.getTxBytes();
        // just a small fail-safe, I know this technically adds to the loading time,
        // but I want to rule out some silly loop problems.
        double seconds = 10;
        //Log.d(this.TAG, "Initial Bytes:" + initialTx);
        /**
         * Measure the deltas of tx sizes
         * while (previous-now) > delta) {
         *  move now to previous
         *  get new now
         * }
         */
        /**
         * Things to account for:
         *  apps can take n-seconds before they start tx data
         *  apps can send data at irregular data over n-seconds
         *
         */
        boolean kickOut = false;
        while(((Math.abs(nowTx - previousTx) > deltaTx) || !kickOut) && seconds > 0) {
            try {
                nowTx = this.getTxBytes();
                previousTx = nowTx;
                // We must
                Thread.sleep(250);
                //Log.d(this.TAG, "There are: " + seconds + " remaining");
                seconds = seconds-.25;
            } catch (Exception e) {
                seconds = seconds-.25;
            }
            if(Math.abs(nowTx - initialTx) > 0) {
                kickOut = true;
                Log.d(this.TAG, "Our kickout has been set");
            }
            //Log.d(this.TAG, "==================================================");
            //Log.d(this.TAG, "Total bytes are: " + nowTx);
            //Log.d(this.TAG, "==================================================");
        }// while statement
        this.totalBytesSent = Math.abs(nowTx - initialTx);
        //Log.d(this.TAG, "***********************************************");
        //Log.d(this.TAG, "Total bytes sent: " + this.totalBytesSent);
        //Log.d(this.TAG, "***********************************************");
        return (double)1000*seconds;
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
            Log.d(this.TAG, "We couldn't get the nic type");
            return "nan";
        }
    }

}