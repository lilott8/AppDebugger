package com.cs253.appdebugger.database;

/**
 * TODO I need to add the initializePackageManager to this class :(  That way
 * we can return apps, to be used in our event parser
 */

/**
 * Created by jason on 10/25/13.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;
import com.cs253.appdebugger.App;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: Abstract the query cursors so that we don't have to repeatedly use the same code fragment
 */
public class MonitorDataSource {
    private SQLiteDatabase database;
    private MySQLiteHelper dbhelper;
    private String[] allColumns = {MySQLiteHelper._ID, MySQLiteHelper.APP_NAME,
            MySQLiteHelper.ACTIVE, MySQLiteHelper.ACTIVITY_NAMES};

    public MonitorDataSource(Context context) {
        this.dbhelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {
        this.database = this.dbhelper.getWritableDatabase();
    }

    public void close() {
        this.dbhelper.close();
    }

    public void deleteRecord(Stats stats) {
        long id = stats.getId();
        this.database.delete(MySQLiteHelper.TABLE_MONITOR, MySQLiteHelper._ID + "=" + id, null);
    }

    public List<Monitor> getAllMonitorsByColumn(String columnName) {
        List<Monitor> monitor = new ArrayList<Monitor>();

        Cursor cursor = this.database.query(MySQLiteHelper.TABLE_MONITOR, this.allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            Monitor m = cursorToMonitor(cursor);
            monitor.add(m);
            cursor.moveToNext();
        }
        cursor.close();
        return monitor;
    }

    public Monitor selectMonitorByAppName(String app_name) {
        //ContentValues values = new ContentValues();
        //Cursor cursor = this.database.query(MySQLiteHelper.TABLE_MONITOR, this.allColumns,
        //        MySQLiteHelper.APP_NAME, new String[]{app_name}, null, null, null);
        Cursor cursor = this.database.rawQuery("SELECT * FROM " + MySQLiteHelper.TABLE_MONITOR +
            " WHERE " + MySQLiteHelper.APP_NAME + " = ?", new String[]{app_name});
        cursor.moveToFirst();
        Monitor monitor = cursorToMonitor(cursor);
        cursor.close();
        return monitor;
    }

    /**
     * TODO: figure out why a 0 is being inserted into the table on row creation
     * @param app_name
     * @param active
     * @param activity_names
     * @return
     */
    public Monitor createOrUpdate(String app_name, boolean active, String activity_names) {
        Monitor checkForApp = this.selectMonitorByAppName(app_name);
        if(checkForApp.getId() < 1) {
            return this.createMonitor(app_name, active, activity_names);
        } else {
            this.activateMonitor(app_name);
            return checkForApp;
        }
    }

    public boolean activateMonitor(String app_name) {
        String[] args = {"true", app_name};
        Cursor c = this.database.rawQuery("UPDATE " + MySQLiteHelper.TABLE_MONITOR +
                " SET " + MySQLiteHelper.ACTIVE + " =? WHERE " + MySQLiteHelper.APP_NAME +
                " =? ", args);
        c.moveToFirst();
        c.close();
        return true;
    }

    public Monitor createMonitor(String app_name, boolean active, String activity_names) {
        ContentValues values = new ContentValues();
        // Get the values ready for insert
        values.put(MySQLiteHelper.APP_NAME, app_name);
        values.put(MySQLiteHelper.ACTIVE, active);
        values.put(MySQLiteHelper.ACTIVITY_NAMES, activity_names);
        // insert the record
        long insertId = this.database.insert(MySQLiteHelper.TABLE_MONITOR, null, values);
        // grab the newest record from our db
        Cursor cursor = this.database.query(MySQLiteHelper.TABLE_MONITOR, this.allColumns,
                MySQLiteHelper._ID + " = " + insertId, null, null, null, null);
        // We want the first returned value
        cursor.moveToFirst();
        // convert our cursor record to a stats object
        Monitor monitor = cursorToMonitor(cursor);
        // close the cursor/remove the cursor object
        cursor.close();
        return monitor;
    }

    public boolean deactivateMonitor(String app_name) {
        try {
            String[] args = {"false", app_name};
            Cursor c = this.database.rawQuery("UPDATE " + MySQLiteHelper.TABLE_MONITOR +
                " SET " + MySQLiteHelper.ACTIVE + "= ? WHERE " + MySQLiteHelper.APP_NAME +
                " = ? ", args);
            c.moveToFirst();
            c.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public ArrayList<App> getAllActiveApps() {
        ArrayList<App> app = new ArrayList<App>();
        Cursor c = this.database.rawQuery("SELECT * FROM " + MySQLiteHelper.TABLE_MONITOR +
            " WHERE " + MySQLiteHelper.ACTIVE + " = true", null);
        c.moveToFirst();
        while(!c.isAfterLast()) {
            App m = cursorToApp(c);
            app.add(m);
            c.moveToNext();
        }
        return app;
    }

    public App cursorToApp(Cursor cursor) {
        App app = new App();

        return app;
    }

    public Monitor cursorToMonitor(Cursor cursor) {
        Monitor monitor = new Monitor();
        if(cursor.getCount() == 0) {
            monitor.setActive(false);
        } else {
            monitor.setAppName(cursor.getString(1));
            monitor.setId(cursor.getLong(0));
            monitor.setActive(Boolean.parseBoolean(cursor.getString(2)));
            // Todo: allow for array, either parceable or serialized
            monitor.setAppActivities(cursor.getString(3));
        }
        return monitor;
    }
}
