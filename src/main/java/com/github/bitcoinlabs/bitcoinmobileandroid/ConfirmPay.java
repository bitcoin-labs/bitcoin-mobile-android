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

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pay_confirm);
        
        double val = 0;
        Uri bitcoinUri = getIntent().getData();
        assert("bitcoin".equals(bitcoinUri.getScheme()));
        //hackity hackity
        bitcoinUri = Uri.parse("bitcoin://" + bitcoinUri.getEncodedSchemeSpecificPart());

        final String bitcoinAddress = bitcoinUri.getAuthority();
        String amount = bitcoinUri.getQueryParameter("amount");
        final String label = bitcoinUri.getQueryParameter("label");
        String message = bitcoinUri.getQueryParameter("message");

        final EditText payAmount = (EditText) findViewById(R.id.payAmount);
        
        final TextView payDetails = (TextView) findViewById(R.id.payDetails);
        payDetails.setText("Address: " + bitcoinAddress + "\n" + 
        "Amount: " + amount + "\n" + 
        "Label: " + label + "\n" + 
        "Message: " + message);
        
        double amountD = 0.0;
        try {
            amountD = Double.parseDouble(amount);
        } catch (Exception e) {
        }
        payAmount.setText(MoneyUtils.formatMoney(amountD));
        findViewById(R.id.confirmButton).setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                Toast.makeText(ConfirmPay.this, payAmount.getText() + "BTC paid to " + label + " (" + bitcoinAddress + ")" , Toast.LENGTH_LONG).show();
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