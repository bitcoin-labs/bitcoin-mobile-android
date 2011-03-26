package com.github.bitcoinlabs.bitcoinmobileandroid;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        Button pay_button = (Button) findViewById(R.id.pay_button);
        Button rec_button = (Button) findViewById(R.id.rec_button);
    }
}