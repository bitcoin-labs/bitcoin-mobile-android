package com.github.bitcoinlabs.bitcoinmobileandroid;

import java.util.Hashtable;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

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

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.receive);
        final Spinner btcAddressSpinner = (Spinner)findViewById(R.id.rcv_btcAddress);
        final EditText amount = (EditText)findViewById(R.id.rcv_amount);
        final EditText label = (EditText)findViewById(R.id.rcv_label);
        final EditText message = (EditText)findViewById(R.id.rcv_message);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.btcAddresses, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        btcAddressSpinner.setAdapter(adapter);

        setTitle("Receive Bitcoin");
        btcAddressSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> arg0, View arg1,
                    int arg2, long arg3) {
                updateQRCode(btcAddressSpinner, amount, label, message);
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        OnEditorActionListener onEditorActionListener = new OnEditorActionListener() {
            
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                updateQRCode(btcAddressSpinner, amount, label, message);
                return true;
            }
        };
        amount.setOnEditorActionListener(onEditorActionListener);
        label.setOnEditorActionListener(onEditorActionListener);
        message.setOnEditorActionListener(onEditorActionListener);
        updateQRCode(btcAddressSpinner, amount, label, message);
    }

    private void updateQRCode(Spinner btcAddressSpinner, EditText amount, EditText label, EditText message) {
        try {
            Double receiveAmount = null;
            try {
                receiveAmount = Double.parseDouble(amount.getText().toString());
            } catch (NumberFormatException e) {/*let receiveAmount be null*/}
            showQrBitmap(btcAddressSpinner.getSelectedItem()+"", receiveAmount, label.getText().toString(), message.getText().toString());
        } catch (WriterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void callMe(Context c) {
        final Intent intent = new Intent(c, Receive.class);
        c.startActivity(intent);
    }

    private void showQrBitmap(String btcAddress, Double amount, String label, String message) throws WriterException {
        Builder builder = new Uri.Builder();
        btcAddress = "185fv9g323aTbGK2BC9H4x3NGtMbFNKusF";
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
}