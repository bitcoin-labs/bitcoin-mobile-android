package com.bitcoinlabs.android.settings;

import com.bitcoinlabs.android.R;
import com.bitcoinlabs.android.R.xml;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.widget.Toast;

public class Preferences extends PreferenceActivity implements OnSharedPreferenceChangeListener {
    private static final String TX_FEE_PREF = "txFee";
    private Preference txFeePref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.bitcoinprefs);
        txFeePref = getPreferenceScreen().findPreference(TX_FEE_PREF);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        setSummaries(sharedPreferences, TX_FEE_PREF);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (TX_FEE_PREF.equals(key)) {
            double txFee = Double.NaN;
            String txFeeString = sharedPreferences.getString(TX_FEE_PREF, "0.005");
            try {
                txFee = Double.parseDouble(txFeeString);
                if (!(txFee <= .05 && txFee >= 0)) {
                    txFee = Double.NaN;
                }
            } catch (NumberFormatException nfe) {}
            if (txFee != Double.NaN) {
                setSummaries(sharedPreferences, key);
            } else {
                Toast.makeText(Preferences.this, txFeeString + " is not a sane tx fee.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void setSummaries(SharedPreferences sharedPreferences, String key) {
        txFeePref.setSummary(sharedPreferences.getString(key, "") + " ฿TC");
    }
}
