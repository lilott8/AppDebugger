package com.cs253.appdebugger.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by jason on 10/25/13.
 */
public class MySQLiteHelper extends SQLiteOpenHelper {

    // Shared fields
    public static final String _ID = "_id";
    public static final String APP_NAME = "app_name";
    // Stats columns
    public static final String TABLE_STATS = "stats";
    public static final String START_TIME = "start_time";
    public static final String END_TIME = "end_time";
    public static final String DATA_SENT = "data_sent";
    // Monitor columns
    public static final String TABLE_MONITOR = "monitor";
    public static final String ACTIVE = "active";
    public static final String ACTIVITY_NAMES = "activities";
    // Database "stuff"
    public static final String DATABASE_NAME = "appDebugger.db";
    private static final int DATABASE_VERSION = 1;

    private static final String STATS_CREATE = "create table " + TABLE_STATS + " (" +
            _ID + " integer primary key autoincrement, " +
            APP_NAME + " varchar(255) unique not null, " +
            START_TIME + " unsigned bigint, " +
            END_TIME + " unsigned bigint, " +
            DATA_SENT + " unsigned bigint)";

    private static final String MONITOR_CREATE = "create table " + TABLE_MONITOR + " (" +
            _ID + " integer primary key autoincrement, " +
            APP_NAME + " varchar(255) not null, " +
            ACTIVE + " boolean false, " +
            ACTIVITY_NAMES + " text null );";
    private Context context;

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        Toast.makeText(this.context, "Creating database tables now...", Toast.LENGTH_LONG).show();
        database.execSQL(STATS_CREATE);
        database.execSQL(MONITOR_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        Log.w(MySQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_MONITOR);
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_STATS);
        onCreate(database);
    }
}
