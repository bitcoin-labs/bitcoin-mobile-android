
package com.github.bitcoinlabs.bitcoinmobileandroid;

import java.util.ArrayList;
import java.util.List;

import com.google.bitcoin.core.ECKey;
import com.google.bitcoin.core.NetworkParameters;

import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class WalletOpenHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "test2";
    private static final int DATABASE_VERSION = 2;
    
    public static final String KEY = "key";
    public static final String ADDRESS = "address58";
    

    WalletOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
            "CREATE TABLE keys (" +
            "id         INTEGER PRIMARY KEY AUTOINCREMENT," +
            ADDRESS + " TEXT," +
            KEY + "     BLOB);");
    }

    @Override
    public void onUpgrade (SQLiteDatabase db, int oldVersion, int newVersion){
        onCreate(db);
    }
    
    public String newKey() {
        ECKey key = new ECKey();
        String address = addKey(key);
        return address;
    }

    public String addKey(ECKey key) {
        SQLiteDatabase db = getWritableDatabase();
        String address58 = key.toAddress(NetworkParameters.prodNet()).toString();
        db.execSQL(
                "INSERT INTO keys ('address58', 'key') VALUES (?, ?)",
                new Object[] { address58, key.toASN1()} );
        return address58;
    }
    
    public Cursor getAddresses() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query("keys", new String[]{ADDRESS}, null, null, null, null, null, "5");
        return cursor;
    }
}
