package com.github.bitcoinlabs.bitcoinmobileandroid;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

public class Bitcoin extends Activity
{
    public static final String BITCOIN_EXIT_NODE_BALANCE = "http://97.107.139.194:8000/api/unspent-outpoints.js";
    private TextView balanceStatusView;
    private TextView balanceView;
    private TextView balanceUnconfirmedView;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        balanceStatusView = (TextView)findViewById(R.id.balance_status);
        balanceView = (TextView)findViewById(R.id.balance);
        balanceUnconfirmedView = (TextView)findViewById(R.id.balance_unconfirmed);

        findViewById(R.id.recButton).setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                Receive.callMe(Bitcoin.this);
            }
        });
        findViewById(R.id.scanButton).setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                callScan();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.refresh:
            refreshBalance();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void refreshBalance() {
        balanceStatusView.setText("Refreshing...");
        new RefreshOutpointsTask().execute();
    }

    private void callScan()
    {
        Intent intent = new Intent("com.google.zxing.client.android.SCAN");
        intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
        try
        {
            startActivityForResult(intent, 0);
        }
        catch (Exception e)
        {
            Toast.makeText(this, "Install the barcode scanner!!!", Toast.LENGTH_LONG).show();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        if (requestCode == 0)
        {
            if (resultCode == RESULT_OK)
            {
                String contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                Uri bitcoinUri = Uri.parse(contents);

                ConfirmPay.callMe(this, bitcoinUri);
            }
            else if (resultCode == RESULT_CANCELED)
            {
                // Handle cancel
            }
        }
    }

    private class RefreshOutpointsTask extends AsyncTask<Void, Void, OutpointsResponse> {

        @Override
        protected OutpointsResponse doInBackground(Void... params) {

            int TIMEOUT_MILLISEC = 10000;  // = 10 seconds
            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, TIMEOUT_MILLISEC);
            HttpConnectionParams.setSoTimeout(httpParams, TIMEOUT_MILLISEC);
            HttpClient client = new DefaultHttpClient(httpParams);

//            HttpPost request = new HttpPost(BITCOIN_EXIT_NODE_BALANCE);
            HttpGet request;
            Gson gson = new Gson();

            WalletOpenHelper wallet = new WalletOpenHelper(getApplicationContext());
            Cursor addressesCursor = wallet.getAddresses();
            //TODO figure out how to stream the cursor into a json array
            String[] btcAddresses = new String[addressesCursor.getCount()];
            int i = 0;
            while (addressesCursor.isAfterLast() == false) {
                btcAddresses[i] = addressesCursor.getString(0);
                i++;
                addressesCursor.moveToNext();
            }
//            String postMessage = gson.toJson(btcAddresses);
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

        @Override
        protected void onPostExecute(OutpointsResponse outpointsResponse) {
            if (outpointsResponse.isError()) {
                balanceStatusView.setText(outpointsResponse.getException() + ": " + outpointsResponse.getServerError());
            } else {
                balanceStatusView.setText("Last checked: " + new Date(outpointsResponse.getTimeStamp()));
                Collection<Outpoint> unspent_outpoints = outpointsResponse.getUnspent_outpoints();
                long satoshis = 0;
                for (Outpoint outpoint : unspent_outpoints) {
                    satoshis += outpoint.getSatoshis();
                }
                balanceView.setText(MoneyUtils.formatSatoshisAsBtcString(satoshis));
                balanceUnconfirmedView.setText(MoneyUtils.formatSatoshisAsBtcString(0));
            }
        }
    }
}
