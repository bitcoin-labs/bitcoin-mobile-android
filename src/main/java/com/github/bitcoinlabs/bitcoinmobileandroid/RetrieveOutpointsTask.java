package com.github.bitcoinlabs.bitcoinmobileandroid;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import com.google.gson.Gson;

import android.database.Cursor;
import android.os.AsyncTask;

public class RetrieveOutpointsTask extends AsyncTask<WalletOpenHelper, Void, OutpointsResponse> {

    private static final String BITCOIN_EXIT_NODE_BALANCE = "";

    @Override
    protected OutpointsResponse doInBackground(WalletOpenHelper... params) {

        int TIMEOUT_MILLISEC = 10000;  // = 10 seconds
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, TIMEOUT_MILLISEC);
        HttpConnectionParams.setSoTimeout(httpParams, TIMEOUT_MILLISEC);
        HttpClient client = new DefaultHttpClient(httpParams);

//        HttpPost request = new HttpPost(BITCOIN_EXIT_NODE_BALANCE);
        HttpGet request;
        Gson gson = new Gson();

        WalletOpenHelper wallet = params[0];
        Cursor addressesCursor = wallet.getAddresses();
        //TODO figure out how to stream the cursor into a json array
        String[] btcAddresses = new String[addressesCursor.getCount()];
        int i = 0;
        while (addressesCursor.isAfterLast() == false) {
            btcAddresses[i] = addressesCursor.getString(0);
            i++;
            addressesCursor.moveToNext();
        }
//        String postMessage = gson.toJson(btcAddresses);
        HttpResponse response = null;
        OutpointsResponse outpointsResponse = null;
        try {
            request = new HttpGet(BITCOIN_EXIT_NODE_BALANCE + "?addresses=" + commaSeparate(btcAddresses));

            response = client.execute(request);
            HttpEntity responseEntity = response.getEntity();
            InputStream content = responseEntity.getContent();
            Reader reader = new InputStreamReader(content);
            outpointsResponse = gson.fromJson(reader, OutpointsResponse.class);
        } catch (Exception e) {
            outpointsResponse = new OutpointsResponse(e, null);
        }
        return outpointsResponse;
    }

    private String commaSeparate(String[] strings) {
        if (strings == null || strings.length < 1) {
            throw new IllegalArgumentException("strings array must have at least one string");
        }
        return Arrays.toString(strings).replaceAll("[\\[\\] ]", "");
    }

//    Do this with a contentprovider instead
//    @Override
//    protected void onPostExecute(OutpointsResponse outpointsResponse) {
//        if (outpointsResponse.isError()) {
//            balanceStatusView.setText(outpointsResponse.getException() + ": " + outpointsResponse.getServerError());
//        } else {
//            balanceStatusView.setText("Last checked: " + new Date(outpointsResponse.getTimeStamp()));
//            Collection<Outpoint> unspent_outpoints = outpointsResponse.getUnspent_outpoints();
//            long satoshis = 0;
//            for (Outpoint outpoint : unspent_outpoints) {
//                satoshis += outpoint.getSatoshis();
//            }
//            balanceView.setText(MoneyUtils.formatSatoshisAsBtcString(satoshis));
//            balanceUnconfirmedView.setText(MoneyUtils.formatSatoshisAsBtcString(0));
//        }
//    }
    
}
