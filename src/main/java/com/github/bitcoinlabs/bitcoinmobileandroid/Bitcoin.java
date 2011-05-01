package com.github.bitcoinlabs.bitcoinmobileandroid;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
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
    public static final String BITCOIN_EXIT_NODE_BALANCE = "";
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
        new RefreshBalanceTask().execute();
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

    private class RefreshBalanceTask extends AsyncTask<Void, Void, BalanceResponse> {

        @Override
        protected BalanceResponse doInBackground(Void... params) {

            int TIMEOUT_MILLISEC = 10000;  // = 10 seconds
            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, TIMEOUT_MILLISEC);
            HttpConnectionParams.setSoTimeout(httpParams, TIMEOUT_MILLISEC);
            HttpClient client = new DefaultHttpClient(httpParams);

            HttpPost request = new HttpPost(BITCOIN_EXIT_NODE_BALANCE);
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
            String postMessage = gson.toJson(btcAddresses);
            HttpResponse response = null;
            BalanceResponse balanceResponse = null;
            try {
                request.setEntity(new ByteArrayEntity(postMessage.toString().getBytes("UTF8")));
                response = client.execute(request);
                HttpEntity responseEntity = response.getEntity();
                InputStream content = responseEntity.getContent();
                Reader reader = new InputStreamReader(content);
                balanceResponse = gson.fromJson(reader, BalanceResponse.class);
                if (balanceResponse == null) {
                    balanceResponse = new BalanceResponse(System.currentTimeMillis(), (long)(1.23 * MoneyUtils.SATOSHIS_PER_BITCOIN), (long)(3.21 * MoneyUtils.SATOSHIS_PER_BITCOIN));
                }
            } catch (Exception e) {
                balanceResponse = new BalanceResponse(e, null);
            }
            return balanceResponse;
        }

        @Override
        protected void onPostExecute(BalanceResponse balanceResponse) {
            if (balanceResponse.isError()) {
                balanceStatusView.setText(balanceResponse.getException() + ": " + balanceResponse.getServerError());
            } else {
                balanceStatusView.setText("Last checked: " + new Date(balanceResponse.getTimeStamp()));
                balanceView.setText(MoneyUtils.formatSatoshisAsBtcString(balanceResponse.getSatoshisConfirmed()));
                balanceUnconfirmedView.setText(MoneyUtils.formatSatoshisAsBtcString(balanceResponse.getSatoshisUnconfirmed()));
            }
        }
    }
}
