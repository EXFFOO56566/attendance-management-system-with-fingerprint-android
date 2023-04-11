package com.example.attendacewithfingerprint.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Table Name
    static final String DATA_CONNECTION = "DATASABSENT";
    static final String DATA_LOCATION = "DATALOCATION";
    static final String DATA_ATTENDANCE = "DATAATTENDANCE";

    // Table columns DATASABSENT
    public static final String _ID = "_id";
    public static final String LINK = "link";
    public static final String KEY_API = "key";
    public static final String USER_NAME = "user_name";
    public static final String ID_NUMBER = "id_number";

    // Table columns DATALOCATION
    public static final String LAT = "lat";
    public static final String LONGT = "longt";

    // Table columns DATAATTENDANCE
    public static final String NAME = "name";
    public static final String DATE = "date";
    public static final String TIME = "time";
    public static final String TYPE = "type";
    public static final String LOCATION = "location";

    // Database Information
    private static final String DB_NAME = "DATAS_ABSENT.DB";

    // database version
    private static final int DB_VERSION = 2;

    DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + DATA_CONNECTION + "(" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                LINK + " TEXT NOT NULL, " +
                KEY_API + " TEXT NOT NULL, " +
                USER_NAME + " TEXT NOT NULL, " +
                ID_NUMBER + " TEXT NOT NULL " + ")"
        );

        db.execSQL("CREATE TABLE " + DATA_LOCATION + "(" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                LAT + " TEXT NOT NULL, " +
                LONGT + " TEXT NOT NULL " + ")"
        );

        db.execSQL("CREATE TABLE " + DATA_ATTENDANCE + "(" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                NAME + " TEXT NOT NULL, " +
                DATE + " TEXT NOT NULL, " +
                TIME + " TEXT NOT NULL, " +
                TYPE + " TEXT NOT NULL, " +
                LOCATION + " TEXT NOT NULL " + ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DATA_CONNECTION);
        db.execSQL("DROP TABLE IF EXISTS " + DATA_LOCATION);
        db.execSQL("DROP TABLE IF EXISTS " + DATA_ATTENDANCE);
        onCreate(db);
    }
}
