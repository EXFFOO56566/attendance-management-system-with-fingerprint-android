package com.example.attendacewithfingerprint.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;

import static com.example.attendacewithfingerprint.database.DatabaseHelper.DATA_ATTENDANCE;
import static com.example.attendacewithfingerprint.database.DatabaseHelper.DATA_CONNECTION;
import static com.example.attendacewithfingerprint.database.DatabaseHelper.DATA_LOCATION;
import static com.example.attendacewithfingerprint.database.DatabaseHelper.DATE;
import static com.example.attendacewithfingerprint.database.DatabaseHelper.LOCATION;
import static com.example.attendacewithfingerprint.database.DatabaseHelper.NAME;
import static com.example.attendacewithfingerprint.database.DatabaseHelper.TIME;
import static com.example.attendacewithfingerprint.database.DatabaseHelper.TYPE;
import static com.example.attendacewithfingerprint.database.DatabaseHelper._ID;

public class DBManager {

    private Context context;

    private SQLiteDatabase database;

    public DBManager(Context c) {
        context = c;
    }

    public void open() throws SQLException {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
    }

    //--------------------------------- TABLE CONNECTION --------------------------------------
    public Cursor getItem() {
        return database.rawQuery("SELECT * FROM " + DATA_CONNECTION + " ORDER BY " + _ID + " DESC", null);
    }

    public long insert(String link, String key, String name, String id_number) {
        ContentValues contentValue = new ContentValues();
        contentValue.put(DatabaseHelper.LINK, link);
        contentValue.put(DatabaseHelper.KEY_API, key);
        contentValue.put(DatabaseHelper.USER_NAME, name);
        contentValue.put(DatabaseHelper.ID_NUMBER, id_number);
        return database.insert(DATA_CONNECTION, null, contentValue);
    }

    public boolean update(int _id, String link, String key, String name, String id_number) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.LINK, link);
        contentValues.put(DatabaseHelper.KEY_API, key);
        contentValues.put(DatabaseHelper.USER_NAME, name);
        contentValues.put(DatabaseHelper.ID_NUMBER, id_number);
        database.update(DATA_CONNECTION, contentValues, _ID + " = " + _id, null);
        return true;
    }

    //--------------------------------- TABLE LOCATION --------------------------------------
    public Cursor getLocation() {
        return database.rawQuery("SELECT * FROM " + DATA_LOCATION, null);
    }

    public void insertLocation(Double lat, Double longt) {
        ContentValues contentValue = new ContentValues();
        contentValue.put(DatabaseHelper.LAT, lat);
        contentValue.put(DatabaseHelper.LONGT, longt);
        database.insert(DATA_LOCATION, null, contentValue);
    }

    public void deleteAllLocation() {
        SQLiteDatabase db = database;
        db.execSQL("DELETE FROM " + DATA_LOCATION); //delete all rows in a table
    }

    //--------------------------------- TABLE ATTENDANCE --------------------------------------
    public ArrayList<HashMap<String, String>> getAttendance() {
        ArrayList<HashMap<String, String>> userList = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM " + DATA_ATTENDANCE + " ORDER BY " + _ID + " desc", null);
        while (cursor.moveToNext()) {
            HashMap<String, String> detailAttendance = new HashMap<>();
            detailAttendance.put("name", cursor.getString(cursor.getColumnIndex(NAME)));
            detailAttendance.put("date", cursor.getString(cursor.getColumnIndex(DATE)));
            detailAttendance.put("time", cursor.getString(cursor.getColumnIndex(TIME)));
            detailAttendance.put("type", cursor.getString(cursor.getColumnIndex(TYPE)));
            detailAttendance.put("location", cursor.getString(cursor.getColumnIndex(LOCATION)));
            userList.add(detailAttendance);
        }
        return userList;
    }

    public void insertAttendance(String name, String date, String time, String type, String location) {
        ContentValues contentValue = new ContentValues();
        contentValue.put(DatabaseHelper.NAME, name);
        contentValue.put(DatabaseHelper.DATE, date);
        contentValue.put(DatabaseHelper.TIME, time);
        contentValue.put(DatabaseHelper.TYPE, type);
        contentValue.put(DatabaseHelper.LOCATION, location);
        database.insert(DATA_ATTENDANCE, null, contentValue);
    }

    public void deleteAllAttendance() {
        SQLiteDatabase db = database;
        db.execSQL("DELETE FROM " + DATA_ATTENDANCE); //delete all rows in a table
    }

    public boolean checkIfEmpty(String TABLE) {
        @SuppressLint("Recycle") Cursor cursor = database.rawQuery("SELECT count(*) FROM " + TABLE, null);

        if (cursor != null && cursor.getCount() != 0) {
            try {
                //if it is empty, returns true.
                cursor.moveToFirst();
                return cursor.getInt(0) == 0;
            }
            //this error usually occurs when it is empty. So i return true as well. :)
            catch (CursorIndexOutOfBoundsException e) {
                return true;
            }

        }
        return false;
    }

}
