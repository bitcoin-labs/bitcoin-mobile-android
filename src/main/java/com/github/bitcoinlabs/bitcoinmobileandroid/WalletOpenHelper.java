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

import java.util.HashMap;
import java.math.BigInteger;
import com.google.bitcoin.core.Transaction;
import com.google.bitcoin.core.TransactionStandaloneEncoder;
import com.google.bitcoin.core.NetworkParameters;



public class WalletOpenHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "keys";
    private static final int DATABASE_VERSION = 2;

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
            cursor.moveToFirst();
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

    public Transaction createTransaction(long targetSatoshis, String destAddress) {
        
        long satoshisGathered = 0;
        
        ArrayList<String> in_addresses = new ArrayList<String>();
        ArrayList<byte[]> in_hashes = new ArrayList<byte[]>();
        ArrayList<Integer> in_indexes = new ArrayList<Integer>();
        ArrayList<Integer> in_ids = new ArrayList<Integer>();
        
        HashMap<String, ECKey> address_key_map = new HashMap<String, ECKey>();
        
        SQLiteDatabase db = getWritableDatabase();
        
        // Read outpoints
        Cursor cursor = db.query("outpoints", new String[]{"id", HASH, ADDRESS, N, SATOSHIS}, "spent = 0", null, null, null, null, null);
        cursor.moveToFirst();
        while ((satoshisGathered < targetSatoshis) && (cursor.isAfterLast() == false)) {
            in_ids.add(cursor.getInt(0));
            in_hashes.add(cursor.getBlob(1));
            in_addresses.add(cursor.getString(2));
            in_indexes.add(cursor.getInt(3));
            satoshisGathered += cursor.getLong(4);
        }
        if (satoshisGathered < targetSatoshis) {
            return null;
        }
        
        // Read keys
        String whereClause = "(" + ADDRESS + " in (";
        for (int i = 0; i < in_addresses.size(); i++) {
            if (i > 0) {
                whereClause += ", ";
            }
            whereClause += "'" + in_addresses.get(i) + "'";
        }
        whereClause += "))";
        cursor = db.query("keys", new String[]{ADDRESS, KEY}, whereClause, null, null, null, null, null);
        cursor.moveToFirst();
        while ((satoshisGathered < targetSatoshis) && (cursor.isAfterLast() == false)) {
            address_key_map.put(
                cursor.getString(0),
                new ECKey(new BigInteger(cursor.getBlob(1))));
        }
        
        // Create transaction
        Transaction tx;
        TransactionStandaloneEncoder tse = new TransactionStandaloneEncoder(NetworkParameters.prodNet());
        for (int i = 0; i < in_addresses.size(); i++) {
            tse.addInput(
                    address_key_map.get(in_addresses.get(i)),
                    in_indexes.get(i).intValue(),
                    in_hashes.get(i));
        }
        try {
            tse.addOutput(new BigInteger("" + targetSatoshis), destAddress);
            if (satoshisGathered < targetSatoshis) {
                BigInteger changeSatoshis = new BigInteger("" + (satoshisGathered - targetSatoshis));
                tse.addOutput(changeSatoshis, getUnusedAddress().toString());
            }
        }
        catch (AddressFormatException e) {
            // TODO: handle better
            throw new RuntimeException("Invalid address!");
        }
        tx = tse.createSignedTransaction();
        
        // Spend outpoints
        whereClause = "(id in (";
        for (int i = 0; i < in_ids.size(); i++) {
            if (i > 0) {
                whereClause += ", ";
            }
            whereClause += "'" + in_ids.get(i).intValue() + "'";
        }
        whereClause += "))";
        db.execSQL("UPDATE outpoints SET spent = 1 WHERE " + whereClause);
        
        return tx;
    }
}
