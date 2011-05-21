
package com.github.bitcoinlabs.bitcoinmobileandroid;

import java.util.ArrayList;
import java.util.List;
import java.math.BigInteger;

import com.google.bitcoin.core.Address;
import com.google.bitcoin.core.AddressFormatException;
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
    private static final String HASH = "hash";
    private static final String N = "n";
    private static final String SATOSHIS = "satoshis";

    WalletOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTableKeys(db);
        createTableOutpoints(db);
    }

    @Override
    public void onUpgrade (SQLiteDatabase db, int oldVersion, int newVersion){
        if (oldVersion <= 1) {
            createTableOutpoints(db);
        }
    }

    private void createTableKeys(SQLiteDatabase db) {
        db.execSQL(
            "CREATE TABLE keys (" +
            "id         INTEGER PRIMARY KEY AUTOINCREMENT," +
            ADDRESS + " TEXT," +
            KEY + "     BLOB);");
    }

    private void createTableOutpoints(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE outpoints (" +
                "id         INTEGER PRIMARY KEY AUTOINCREMENT," +
                HASH + " BLOB," +
                ADDRESS + " TEXT," +
                N + " INTEGER," + 
                SATOSHIS + " INTEGER" +
                "spent INTEGER DEFAULT 0);");
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

    public Address getUnusedAddress() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query("keys", new String[]{ADDRESS}, null, null, null, null, null, "1");
        Address btcAddress;
        if (cursor.getCount() > 0) {
            cursor.moveToNext();
            String addressString = cursor.getString(0);
            try {
                btcAddress = new Address(NetworkParameters.prodNet(), addressString);
            } catch (AddressFormatException e) {
                throw new RuntimeException("getUnusedAddress:" + e, e);
            }
        } else {
            btcAddress = newKey();
        }
        return btcAddress;
    }
    
    public long getBalance() {
        long satoshis = 0;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query("outpoints", new String[]{SATOSHIS}, "spent = 0", null, null, null, null, "5");
        cursor.moveToFirst();
        while (cursor.isAfterLast() == false) {
            satoshis += cursor.getLong(0);
        }
        return satoshis;
    }
}
