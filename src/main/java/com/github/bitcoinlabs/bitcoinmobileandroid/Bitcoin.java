package com.github.bitcoinlabs.bitcoinmobileandroid;

import java.text.DecimalFormat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
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

    private void enterValue()
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Receive");
        alert.setMessage("Receive");

        LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View entryView = vi.inflate(R.layout.receive_entry_dialog, null);

        // Set an EditText view to get user input
        final EditText input = (EditText) entryView.findViewById(R.id.receiveAmount);
        final DecimalFormat df = new DecimalFormat("0.00");

        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);

        alert.setView(entryView);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int whichButton)
            {
                String value = input.getText().toString();
//                updateAmount(value, df, absoluteValue);

                //TODO: check for bad data/show error
                final double recVal = Double.parseDouble(value);

                QrDisplay.callMe(Bitcoin.this, recVal);
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int whichButton)
            {
                dialog.dismiss();
            }
        });

        alert.show();

        new Handler().postDelayed(new Runnable()
        {
            public void run()
            {
                input.requestFocus();
//                ActivityUtils.showKeys(EditExpenseEntry.this, input);
                input.selectAll();
            }
        }, 300);
    }

}
