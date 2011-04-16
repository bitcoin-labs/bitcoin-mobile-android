package com.github.bitcoinlabs.bitcoinmobileandroid.db;

import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class DictionaryOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;
    private static final String DICTIONARY_TABLE_NAME = "dictionary";

    DictionaryOpenHelper(Context context) {
        super(context, "test", null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("Create table test");
    }
    @Override
    public void onUpgrade (SQLiteDatabase db, int oldVersion, int newVersion){

    }
}