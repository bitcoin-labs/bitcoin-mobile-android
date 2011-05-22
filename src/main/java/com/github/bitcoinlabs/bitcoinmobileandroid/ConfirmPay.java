package com.github.bitcoinlabs.bitcoinmobileandroid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.math.BigInteger;
import java.io.IOException;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.BasicHttpParams;

import com.google.bitcoin.core.TransactionStandaloneEncoder;
import com.google.bitcoin.core.NetworkParameters;
import com.google.bitcoin.core.Transaction;
import com.google.bitcoin.core.ECKey;
import com.google.bitcoin.bouncycastle.util.encoders.Base64;
import com.google.bitcoin.core.AddressFormatException;

/**
 * Created by IntelliJ IDEA.
 * User: kevin
 * Date: 4/1/11
 * Time: 2:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConfirmPay extends Activity
{
    public static final String CONFIRM_PAY_URI = "CONFIRM_PAY_URI";
    int TIMEOUT_MS = 10000;
    long FEE_SATOSHIS = 1000000;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setTitle("Send Bitcoin");
        setContentView(R.layout.pay_confirm);
        
        double val = 0;
        Uri bitcoinUri = getIntent().getData();
        assert("bitcoin".equals(bitcoinUri.getScheme()));
        
        // Hacky. Parses "bitcoin:...address..."
        bitcoinUri = Uri.parse("bitcoin://" + bitcoinUri.getEncodedSchemeSpecificPart());

        final String bitcoinAddress = bitcoinUri.getAuthority();
        String amount = bitcoinUri.getQueryParameter("amount");
        final String label = bitcoinUri.getQueryParameter("label");
        String message = bitcoinUri.getQueryParameter("message");

        TextView payAddress = (TextView)findViewById(R.id.snd_address);
        final EditText payAmount = (EditText) findViewById(R.id.snd_amount);
        final EditText payLabel = (EditText) findViewById(R.id.snd_label);
        final EditText payMessage = (EditText) findViewById(R.id.snd_message);
        payAddress.setText("Address: " + bitcoinAddress);
        payLabel.setText(label);
        payMessage.setText(message);
        
        // TODO: lossless parsing
        double amountD = 0.0;
        try {
            amountD = Double.parseDouble(amount);
        } catch (Exception e) {
        }
        final long amountSatoshis = (long)(amountD * 1e8);
        
        payAmount.setText(MoneyUtils.formatMoney(amountD));
        payAmount.setSelectAllOnFocus(true);
        findViewById(R.id.confirmButton).setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                WalletOpenHelper helper = new WalletOpenHelper(getApplicationContext());
                Transaction tx = helper.createTransaction(amountSatoshis, bitcoinAddress, FEE_SATOSHIS);
                
                if (tx == null) {
                    Toast.makeText(ConfirmPay.this, "Insufficient balance." , Toast.LENGTH_LONG).show();
                }
                else {
                    byte[] msg = tx.bitcoinSerialize();
                    String msg64 = new String(new Base64().encode(msg));
                    
                    HttpParams httpParams = new BasicHttpParams();
                    HttpConnectionParams.setConnectionTimeout(httpParams, TIMEOUT_MS);
                    HttpConnectionParams.setSoTimeout(httpParams, TIMEOUT_MS);
                    HttpClient client = new DefaultHttpClient(httpParams);
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost("http://97.107.139.194:8000/api/publish-tx.js");
                    try {
                        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
                        nameValuePairs.add(new BasicNameValuePair("tx64", msg64));
                        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                        HttpResponse response = httpclient.execute(httppost);
                        Toast.makeText(ConfirmPay.this, payAmount.getText() + "BTC paid to " + payLabel.getText() + " (" + bitcoinAddress + ")" , Toast.LENGTH_LONG).show();
                    }
                    catch (ClientProtocolException e) {
                        Toast.makeText(ConfirmPay.this, "HTTP: ClientProtocolException", Toast.LENGTH_LONG).show();
                    }
                    catch (IOException e) {
                        Toast.makeText(ConfirmPay.this, "HTTP: IOException", Toast.LENGTH_LONG).show();
                    }
                }
                finish();
            }
        });
        findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                setResult(RESULT_CANCELED);
                Toast.makeText(ConfirmPay.this, "Bitcoin payment canceled." , Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    public static void callMe(Context c, Uri bitcoinUri)
    {
        final Intent intent = new Intent(null, bitcoinUri, c, ConfirmPay.class);
        c.startActivity(intent);
    }
}