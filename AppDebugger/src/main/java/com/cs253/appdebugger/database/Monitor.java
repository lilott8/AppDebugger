package com.cs253.appdebugger.database;

/**
 * Created by jason on 10/25/13.
 */
public class Monitor {

    private long id;
    private String app_name;
    private boolean active;
    // todo: allow for array, whether parcable, serializeable or otherwise
    private String app_activities;

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAppName() {
        return this.app_name;
    }

    public void setAppName(String app) {
        this.app_name = app;
    }

    public boolean getActive() {
        return this.active;
    }

    public void setActive(boolean a) {
        this.active = a;
    }

    // todo: allow for array, whether parcable, serializeable or otherwise
    public String getAppActivities() {
        return this.app_activities;
    }
    // todo: allow for array, whether parcable, serializeable or otherwise
    public void setAppActivities(String activities) {
        this.app_activities = activities;
    }
}
