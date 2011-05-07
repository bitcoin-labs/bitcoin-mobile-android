
package com.github.bitcoinlabs.bitcoinmobileandroid;

import java.util.ArrayList;
import java.util.List;
import java.math.BigInteger;

import com.google.bitcoin.core.Address;
import com.google.bitcoin.core.ECKey;
import com.google.bitcoin.core.NetworkParameters;

import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class WalletOpenHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "keys";
    private static final int DATABASE_VERSION = 1;

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

    public Address newKey() {
        ECKey key = new ECKey();
        Address address = addKey(key);
        return address;
    }

    public Address addKey(ECKey key) {
        SQLiteDatabase db = getWritableDatabase();
        Address address = key.toAddress(NetworkParameters.prodNet());
        db.execSQL(
                "INSERT INTO keys ('address58', 'key') VALUES (?, ?)",
                new Object[] { address.toString(), key.getPrivKey().toByteArray()} );
        db.close();
        return address;
    }

    public ECKey getKey(String address58) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query("keys", new String[]{KEY}, "address58 = ?", new String[]{address58}, null, null, null, "1");
        if (cursor.getCount() == 0) {
            return null;
        }
        else {
            return new ECKey(new BigInteger(cursor.getBlob(0)));
        }
    }

    public Cursor getAddresses() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query("keys", new String[]{ADDRESS}, null, null, null, null, null, "5");
        return cursor;
    }
}
