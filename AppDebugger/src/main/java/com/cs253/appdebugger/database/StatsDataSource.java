package com.cs253.appdebugger.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by jason on 10/25/13.
 */

/**
 * TODO: Abstract the query cursors so that we don't have to repeatedly use the same code fragment
 */
public class StatsDataSource {

    private SQLiteDatabase database;
    private MySQLiteHelper dbhelper;
    private String[] allColumns = {MySQLiteHelper._ID, MySQLiteHelper.APP_NAME,
            MySQLiteHelper.APP_LOAD_TIME, MySQLiteHelper.DATA_SENT, MySQLiteHelper.NIC_TYPE,
            MySQLiteHelper.NIC_LOAD_TIME};

    private String[] nicColumns = {MySQLiteHelper._ID, MySQLiteHelper.NIC_LOAD_TIME,
            MySQLiteHelper.NIC_TYPE, MySQLiteHelper.APP_NAME};
    private String[] appColumns = {MySQLiteHelper._ID, MySQLiteHelper.APP_NAME,
            MySQLiteHelper.APP_LOAD_TIME};

    public StatsDataSource(Context context) {
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
        this.database.delete(MySQLiteHelper.TABLE_STATS, MySQLiteHelper._ID + "=" + id, null);
    }

    public List<Stats> getAllStatsByColumn(String columnName) {
        List<Stats> stats = new ArrayList<Stats>();

        Cursor cursor = this.database.query(MySQLiteHelper.TABLE_STATS, this.allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            Stats stat = cursorToStats(cursor);
            stats.add(stat);
            cursor.moveToNext();
        }
        cursor.close();
        return stats;
    }

    public Stats createStats(long app_load_time, String app_name, long data_sent, long nic_load_time, String nic) {
        ContentValues values = new ContentValues();
        // Get the values ready for insert
        values.put(MySQLiteHelper.APP_NAME, app_name);
        values.put(MySQLiteHelper.NIC_TYPE, nic);
        values.put(MySQLiteHelper.APP_LOAD_TIME, app_load_time);
        values.put(MySQLiteHelper.NIC_LOAD_TIME, nic_load_time);
        values.put(MySQLiteHelper.DATA_SENT, data_sent);
        // Attempt to insert the record
        try {
            Log.d("AppDebugger", "Attempting to insert into stats");
            // Insert into stats ( app_name, start_time, end_time, data_sent) VALUES
            // (value 1, value 2, value 3, value 4, value 5);
            long insertId = this.database.insert(MySQLiteHelper.TABLE_STATS, null, values);
            Log.d("AppDebugger", "Insert ID: " + Long.toString(insertId));
            // grab the newest record from our db
            // Select * from stats where _ID = insert_id from above
            Cursor cursor = this.database.query(MySQLiteHelper.TABLE_STATS, this.allColumns,
                    MySQLiteHelper._ID + " = " + insertId, null, null, null, null);
            // We want the first returned value
            cursor.moveToFirst();
            // convert our cursor record to a stats object
            Stats stats = cursorToStats(cursor);
            // close the cursor/remove the cursor object
            cursor.close();
            return stats;
        } catch (SQLException e) {
            Log.d("AppDebugger", "We couldn't insert: " + e.toString());
            return null;
        }
    }


    public Stats cursorToStats(Cursor cursor) {
        Stats stats = new Stats();
        stats.setId(cursor.getLong(0));
        stats.setPackageName(cursor.getString(1));
        stats.setNicType(cursor.getString(2));
        stats.setAppLoadTime(cursor.getLong(3));
        stats.setNicLoadTime(cursor.getLong(4));
        stats.setDataSent(cursor.getLong(5));
         return stats;
    }
}