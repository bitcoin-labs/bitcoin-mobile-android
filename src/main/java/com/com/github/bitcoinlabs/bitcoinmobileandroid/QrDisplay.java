package com.github.bitcoinlabs.bitcoinmobileandroid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import java.text.DecimalFormat;

/**
 * Created by IntelliJ IDEA.
 * User: kevin
 * Date: 4/1/11
 * Time: 2:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class QrDisplay extends Activity
{
    public static final String RECEIVE_VALUE = "RECEIVE_VALUE";

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.receive_qr_display);

        final double recVal = getIntent().getDoubleExtra(RECEIVE_VALUE, 0);

        setTitle("Invoice: "+ MoneyUtils.formatMoney(recVal));
        showQrBitmap(recVal);
    }

    public static void callMe(Context c, double recVal)
    {
        final Intent intent = new Intent(c, QrDisplay.class);
        intent.putExtra(RECEIVE_VALUE, recVal);
        c.startActivity(intent);
    }

    private void showQrBitmap(double val)
    {
        //TODO: calculate and display QR code for rec value
        final ImageView qrDisplay = (ImageView) findViewById(R.id.qrDisplay);
        //Do stuff
    }
}