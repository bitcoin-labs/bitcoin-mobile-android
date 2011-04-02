package com.github.bitcoinlabs.bitcoinmobileandroid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
    public static final String CONFIRM_PAY_VAL = "CONFIRM_PAY_VAL";

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pay_confirm);

        final double val = getIntent().getDoubleExtra(CONFIRM_PAY_VAL, 0);

        final TextView payAmount = (TextView) findViewById(R.id.payAmount);

        payAmount.setText(MoneyUtils.formatMoney(val));
        findViewById(R.id.confirmButton).setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                Toast.makeText(ConfirmPay.this, "You did it!", Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    public static void callMe(Context c, double val)
    {
        final Intent intent = new Intent(c, ConfirmPay.class);
        intent.putExtra(CONFIRM_PAY_VAL, val);
        c.startActivity(intent);
    }
}