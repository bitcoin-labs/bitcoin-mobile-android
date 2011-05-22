package com.bitcoinlabs.android;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.bitcoin.core.Address;
import com.google.gson.Gson;

public class OutpointService extends IntentService {

    private NotificationManager mNM;

    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    private int NOTIFICATION = R.string.outpoint_service_started;

    public OutpointService() {
        super("OutpointService");
    }

//    /**
//     * Class for clients to access.  Because we know this service always
//     * runs in the same process as its clients, we don't need to deal with
//     * IPC.
//     */
//    public class OutpointBinder extends Binder {
//        OutpointService getService() {
//            return OutpointService.this;
//        }
//    }

    @Override
    public void onCreate() {
        super.onCreate();
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        // Display a notification about us starting.  We put an icon in the status bar.
        showNotification();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(getClass().getSimpleName()+"", "Received start id " + startId + ": " + intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Cancel the persistent notification.
        mNM.cancel(NOTIFICATION);

        WalletOpenHelper wallet = new WalletOpenHelper(getApplicationContext());
        long balance = wallet.getBalance();
        // Tell the user we stopped.
        CharSequence text = getText(R.string.outpoint_service_stopped);

        Toast.makeText(this, text + "\nNew Balance: " + MoneyUtils.formatSatoshisAsBtcString(balance) + "BTC", Toast.LENGTH_LONG).show();
    }

//    @Override
//    public IBinder onBind(Intent intent) {
//        return mBinder;
//    }
//
//    // This is the object that receives interactions from clients.  See
//    // RemoteService for a more complete example.
//    private final IBinder mBinder = new OutpointBinder();

    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.outpoint_service_started);

        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(R.drawable.icon, text,
                System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, Bitcoin.class), 0);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, getText(R.string.outpoint_service_label),
                       text, contentIntent);

        // Send the notification.
        mNM.notify(NOTIFICATION, notification);
    }

    private static final String BITCOIN_EXIT_NODE_URL = "http://97.107.139.194:8000/api/unspent-outpoints.js";

    @Override
    protected void onHandleIntent(Intent outpointQueryIntent) {
        Log.i(getClass().getSimpleName()+"", "handle:" + outpointQueryIntent);
        int TIMEOUT_MILLISEC = 10000;  // = 10 seconds
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, TIMEOUT_MILLISEC);
        HttpConnectionParams.setSoTimeout(httpParams, TIMEOUT_MILLISEC);
        HttpClient client = new DefaultHttpClient(httpParams);

        HttpGet request;
        Gson gson = new Gson();

        Context context = getApplicationContext();
        WalletOpenHelper wallet = new WalletOpenHelper(context);
        Address address = wallet.getUnusedAddress();
        String btcAddress = address.toString();
        HttpResponse response = null;
        OutpointsResponse outpointsResponse = null;
        try {
            request = new HttpGet(BITCOIN_EXIT_NODE_URL + "?addresses=" + btcAddress);
            response = client.execute(request);
            HttpEntity responseEntity = response.getEntity();
            InputStream content = responseEntity.getContent();
            Reader reader = new InputStreamReader(content);
            outpointsResponse = gson.fromJson(reader, OutpointsResponse.class);
            wallet.add(outpointsResponse);
            Log.i(getClass().getSimpleName()+"", outpointsResponse+"");
        } catch (Exception e) {
            Log.w(getClass().getSimpleName()+"", e);
            outpointsResponse = new OutpointsResponse(e, null);
        }
//        context.startActivity(new Intent(context, Bitcoin.class));
    }
}
