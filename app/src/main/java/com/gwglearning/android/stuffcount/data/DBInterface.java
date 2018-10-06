package com.gwglearning.android.stuffcount.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.gwglearning.android.stuffcount.data.InvContract.InvTable;

public class DBInterface extends SQLiteOpenHelper {

    // Database name
    private static final String DB_FILE_NAME = "books.db";

    // Database Versioning
    private static final int DB_VER = 1;


    // Constructor
    public DBInterface(Context context) {
        super(context, DB_FILE_NAME, null, DB_VER);
    }

    // Create the database!
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the database here.
        String createTable = "CREATE TABLE "
                + InvTable.TABLE_NAME + "("
                + InvTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + InvTable.COL_NAME + " TEXT NOT NULL, "
                + InvTable.COL_PRICE + " REAL NOT NULL DEFAULT 0, "
                + InvTable.COL_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
                + InvTable.COL_SUPPLIER_NAME + " TEXT NOT NULL, "
                + InvTable.COL_SUPPLIER_PHONE_1 + " TEXT NOT NULL);";

        db.execSQL(createTable); // make the DB!
    }

    // Get Readable...
    @Override
    public SQLiteDatabase getReadableDatabase() {
        return super.getReadableDatabase();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Blank method.  No upgrades are necessary.
    }
}
