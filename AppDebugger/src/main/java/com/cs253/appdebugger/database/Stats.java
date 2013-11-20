package com.cs253.appdebugger.database;

/**
 * Created by jason on 10/25/13.
 */
public class Stats {
    private long id;
    private String app_name;
    private long app_load_time;
    private long data_sent;
    private long nic_load_time;
    private String which_nic;

    public void setId(long id) {
        this.id = id;
    }

    public void setAppLoadTime(long start) {
        this.app_load_time = start;
    }

    public void setNicLoadTime(long time) {
        this.nic_load_time = time;
    }

    public void setPackageName(String app) {
        this.app_name = app;
    }

    public void setDataSent(long data) {
        this.data_sent = data;
    }

    public void setNicType(String nic) { this.which_nic = nic; }

    public String getPackageName() {
        return this.app_name;
    }

    public long getAppLoadTime() { return this.app_load_time; }

    public long getId() {
        return this.id;
    }

    public long getNicLoadTime() { return this.nic_load_time;}

    public long getDataSent() {
        return this.data_sent;
    }

    public String getNicType() { return this.which_nic; }

}
