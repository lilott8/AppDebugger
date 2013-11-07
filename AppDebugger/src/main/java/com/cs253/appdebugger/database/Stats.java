package com.cs253.appdebugger.database;

/**
 * Created by jason on 10/25/13.
 */
public class Stats {
    private long id;
    private String app_name;
    private long start_time;
    private long end_time;
    private long data_sent;

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getStartTime() {
        return this.start_time;
    }

    public long getEndTime() {
        return this.end_time;
    }

    public long getTotalTime() {
        return this.end_time - this.start_time;
    }

    public void setStartTime(long start) {
        this.start_time = start;
    }

    public void setEndTime(long end) {
        this.end_time = end;
    }

    public String getAppName() {
        return this.app_name;
    }

    public void setAppName(String app) {
        this.app_name = app;
    }

    public void setDataSent(long data) {
        this.data_sent = data;
    }

    public long getDataSent() {
        return this.data_sent;
    }

}
