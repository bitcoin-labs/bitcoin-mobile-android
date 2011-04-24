package com.github.bitcoinlabs.bitcoinmobileandroid;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class Bitcoin extends Activity
{
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
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
}
