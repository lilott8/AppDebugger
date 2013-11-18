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
            MySQLiteHelper.START_TIME, MySQLiteHelper.END_TIME, MySQLiteHelper.DATA_SENT};

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

    public Stats createStats(long start_time, long end_time, String app_name, long data_sent) {
        ContentValues values = new ContentValues();
        // Get the values ready for insert
        values.put(MySQLiteHelper.APP_NAME, app_name);
        values.put(MySQLiteHelper.START_TIME, start_time);
        values.put(MySQLiteHelper.END_TIME, end_time);
        values.put(MySQLiteHelper.DATA_SENT, data_sent);
        // Attempt to insert the record
        try {
            /**
             * Insert into stats ( app_name, start_time, end_time, data_sent) VALUES
             * (value 1, value 2, value 3, value 4);
             */
            long insertId = this.database.insert(MySQLiteHelper.TABLE_STATS, null, values);
            // grab the newest record from our db
            /**
             * Select * from stats where _ID = insert_id from above
             */
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
        Log.d("AppDebugger", "The packagename should be: "+cursor.getString(1));
        Stats stats = new Stats();
        stats.setPackageName(cursor.getString(1));
        stats.setDataSent(cursor.getLong(4));
        stats.setEndTime(cursor.getLong(3));
        stats.setId(cursor.getLong(0));
        stats.setStartTime(cursor.getLong(2));
        return stats;
    }
}