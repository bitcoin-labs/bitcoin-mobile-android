
package com.github.bitcoinlabs.bitcoinmobileandroid;

import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class WalletOpenHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "test2";
    private static final int DATABASE_VERSION = 2;

    WalletOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
            "CREATE TABLE keys (" +
            "id         INTEGER PRIMARY KEY AUTOINCREMENT," +
            "address58  TEXT," +
            "key        BLOB);");
    }
    @Override
    public void onUpgrade (SQLiteDatabase db, int oldVersion, int newVersion){
        // TEMP!
        db.execSQL("DROP TABLE IF EXISTS notes");
        onCreate(db);
    }
}
