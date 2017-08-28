package com.example.lonejourneyman.buoynow.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by lonejourneyman on 8/24/17.
 */

public class BuoyDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "buoylist.db";
    private static final int DATABASE_VERSION = 1;

    public BuoyDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_BUOYSLIST_TABLE = "CREATE TABLE " +
                BuoyContract.BuoyEntry.TABLE_NAME + " (" +
                BuoyContract.BuoyEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                BuoyContract.BuoyEntry.COLUMN_DESCRIPTION + " TEXT NOT NULL, " +
                BuoyContract.BuoyEntry.COLUMN_DETAILS + " TEXT NOT NULL, " +
                BuoyContract.BuoyEntry.COLUMN_LONG + " DOUBLE NOT NULL, " +
                BuoyContract.BuoyEntry.COLUMN_LAT + " DOUBLE NOT NULL, " +
                BuoyContract.BuoyEntry.COLUMN_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                "); ";
        sqLiteDatabase.execSQL(SQL_CREATE_BUOYSLIST_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + BuoyContract.BuoyEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}