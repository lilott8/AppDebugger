package com.cs253.appdebugger.other;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;

/**
 * Created by jason on 12/3/13.
 */
public class Commander {

    public String executeCommand(int pid) {
        String line;
        StringBuilder sb = new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec("netstat | grep ESTABLISHED");
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("AppDebugger", "Uh-oh!");
        }
        return null;
    }
}
