package com.github.bitcoinlabs.bitcoinmobileandroid;

import java.util.Hashtable;
import java.util.LinkedList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore.Audio;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.google.bitcoin.core.Address;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class Receive extends Activity {
    public static final String RECEIVE_VALUE = "RECEIVE_VALUE";

    private static final int WHITE = 0xFFFFFFFF;
    private static final int BLACK = 0xFF000000;

    QRCodeWriter qrCodeWriter = new QRCodeWriter();

    protected Address btcAddress;

    private EditText amount;

    private EditText label;

    private EditText message;

    private TextView btcAddressView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Receive Bitcoin");
        setContentView(R.layout.receive);
        btcAddressView = (TextView)findViewById(R.id.rcv_btcAddress);
        amount = (EditText)findViewById(R.id.rcv_amount);
        label = (EditText)findViewById(R.id.rcv_label);
        message = (EditText)findViewById(R.id.rcv_message);
        final SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        String receiveLabelValue = preferences.getString("rcv_label", null);
        label.setText(receiveLabelValue);
        amount.setSelectAllOnFocus(true);
        btcAddressView.setText("Address:\n" + "Generating new key...");
        new GenerateKeyTask().execute();
        OnEditorActionListener onEditorActionListener = new OnEditorActionListener() {
            
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                if (v == label) {
                    Editor edit = preferences.edit().
                    putString("rcv_label", label.getText().toString());
                    edit.commit();
                }
                updateQRCode();
                return true;
            }
        };
        amount.setOnEditorActionListener(onEditorActionListener);
        label.setOnEditorActionListener(onEditorActionListener);
        message.setOnEditorActionListener(onEditorActionListener);
        OnFocusChangeListener onFocusChangeListener = new OnFocusChangeListener() {
            
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    updateQRCode();
                }
            }
        };
        amount.setOnFocusChangeListener(onFocusChangeListener);
        label.setOnFocusChangeListener(onFocusChangeListener);
        message.setOnEditorActionListener(onEditorActionListener);
        updateQRCode();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.rcv_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.balance:
            finish();
            return true;
        case R.id.dtmf:
            new DTMFTask().execute(btcAddress);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void updateQRCode() {
        if (btcAddress != null) {
            btcAddressView.setText("Address:\n" + btcAddress);
            try {
                Double receiveAmount = null;
                try {
                    receiveAmount = Double.parseDouble(amount.getText().toString());
                } catch (NumberFormatException e) {/*let receiveAmount be null*/}
                showQrBitmap(btcAddress.toString(), receiveAmount, label.getText().toString(), message.getText().toString());
            } catch (WriterException e) {
                e.printStackTrace();
            }
        }
    }

    public static void callMe(Context c) {
        final Intent intent = new Intent(c, Receive.class);
        c.startActivity(intent);
    }

    private void showQrBitmap(String btcAddress, Double amount, String label, String message) throws WriterException {
        Builder builder = new Uri.Builder();
        builder.scheme("bitcoin");
        builder.authority(btcAddress);
        if (amount != null) {
            builder.appendQueryParameter("amount", amount+"");
        }
        if (label != null && label.length() > 0) {
            builder.appendQueryParameter("label", label);
        }
        if (message != null && message.length() > 0) {
            builder.appendQueryParameter("message", message);
        }
        String btcUri = builder.build().toString().replaceAll("//", "");
        Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>(2);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        
        BitMatrix result = qrCodeWriter.encode(btcUri, BarcodeFormat.QR_CODE, 0 , 0, hints);
        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        // All are 0, or black, by default
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        BitmapDrawable drawable = new BitmapDrawable(bitmap);
        drawable.setFilterBitmap(false);
        final ImageView qrDisplay = (ImageView) findViewById(R.id.qrDisplay);
        qrDisplay.setImageDrawable(drawable);
    }

    private void playTones(Address address) {
        ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME);
        for (byte b : address.asBytes()) {
            toneGenerator.startTone(b >> 4, 45);
            try {Thread.sleep(50);} catch (InterruptedException e) {}
            toneGenerator.startTone(b & 0x0F, 45);
            try {Thread.sleep(50);} catch (InterruptedException e) {}
        }
        toneGenerator.stopTone();
    }

    private class GenerateKeyTask extends AsyncTask<Void, Void, Address> {

        @Override
        protected Address doInBackground(Void... ignored) {
            WalletOpenHelper wallet = new WalletOpenHelper(getApplicationContext());
            Address btcAddress = wallet.newKey();
            return btcAddress;
        }

        @Override
        protected void onPostExecute(Address generatedBtcAddress) {
            Receive.this.btcAddress = generatedBtcAddress;
            updateQRCode();
        }
    }

    private class DTMFTask extends AsyncTask<Address, Void, Void> {
        @Override
        protected Void doInBackground(Address... btcAddresses) {
            Address btcAddress = btcAddresses[0];
            playTones(btcAddress);
            return null;
        }
    }
}