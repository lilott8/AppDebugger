package com.cs253.appdebugger.benchmarking;

import android.net.TrafficStats;
import android.util.Log;

import com.cs253.appdebugger.App;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by jason on 11/13/13.
 */
public class TrafficMonitor{

    private App app;
    private TrafficStats trafficStats;
    private long beforeStartup;
    private long afterStartup;

    public TrafficMonitor(App whichApp) {
        this.app = whichApp;
        this.trafficStats = new TrafficStats();
    }

    public long getTxBytes() {

        Log.d("AppDebugger", "The UID for " + this.app.getPackageName() + " is " + Integer.toString(this.app.getUid()));
        return this.trafficStats.getUidTxBytes(this.app.getUid());
    }

    public Long getTxBytesManual(){

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
}