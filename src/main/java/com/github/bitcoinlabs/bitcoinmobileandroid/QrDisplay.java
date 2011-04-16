package com.github.bitcoinlabs.bitcoinmobileandroid;

import java.util.Hashtable;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import com.google.bitcoin.core.ECKey;
import com.google.bitcoin.core.Address;
import com.google.bitcoin.core.NetworkParameters;


/**
 * Created by IntelliJ IDEA. User: kevin Date: 4/1/11 Time: 2:05 PM To change
 * this template use File | Settings | File Templates.
 */
public class QrDisplay extends Activity {
    public static final String RECEIVE_VALUE = "RECEIVE_VALUE";

    private static final int WHITE = 0xFFFFFFFF;
    private static final int BLACK = 0xFF000000;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.receive_qr_display);

        final double recVal = getIntent().getDoubleExtra(RECEIVE_VALUE, 0);

        setTitle("Invoice: " + MoneyUtils.formatMoney(recVal));
        try {
            showQrBitmap(recVal);
        } catch (WriterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void callMe(Context c, double recVal) {
        final Intent intent = new Intent(c, QrDisplay.class);
        intent.putExtra(RECEIVE_VALUE, recVal);
        c.startActivity(intent);
    }

    private void showQrBitmap(double val) throws WriterException {
        
        ECKey k = new ECKey();
        String address = k.toAddress(NetworkParameters.prodNet()).toString();
        
        BarcodeFormat format = BarcodeFormat.QR_CODE;
        int dimension = 0;
        String contents = "bitcoin:"+address+"?amount="+val;
        Hashtable<EncodeHintType, Object> hints = null;
        String encoding = guessAppropriateEncoding(contents);
        if (encoding != null) {
            hints = new Hashtable<EncodeHintType, Object>(2);
            hints.put(EncodeHintType.CHARACTER_SET, encoding);
        }
        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix result = writer.encode(contents, format, dimension , dimension, hints);
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

    private static String guessAppropriateEncoding(CharSequence contents) {
        // Very crude at the moment
        for (int i = 0; i < contents.length(); i++) {
          if (contents.charAt(i) > 0xFF) {
            return "UTF-8";
          }
        }
        return null;
      }
}