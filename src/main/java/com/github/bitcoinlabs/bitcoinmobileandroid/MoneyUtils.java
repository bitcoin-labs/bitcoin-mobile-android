package com.github.bitcoinlabs.bitcoinmobileandroid;

import java.text.DecimalFormat;

/**
 * Created by IntelliJ IDEA.
 * User: kevin
 * Date: 4/1/11
 * Time: 2:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class MoneyUtils
{
    public static final long SATOSHIS_PER_BITCOIN = 100000000;

    public static String formatMoney(double val)
    {
        final DecimalFormat decimalFormat = new DecimalFormat("#,##0.00######");
        return decimalFormat.format(val);
    }
    
    public static String formatSatoshisAsBtcString(long satoshis) {
        final DecimalFormat decimalFormat = new DecimalFormat("#,##0.00######");
        double btcAmountDouble = satoshis * 1.0 / SATOSHIS_PER_BITCOIN;
        String btcString = decimalFormat.format(btcAmountDouble);
        return btcString;
    }
    
    public static long btcStringToSatoshis(String amount) {
        if (amount == null) {
            throw new NullPointerException("amount may not be null");
        }
        if (amount.matches("^[0-9]+$")) {
            return Long.parseLong(amount);
        }
        else if (amount.matches("^[0-9]*\\.[0-9]+$")) {
            String[] pair = amount.split("\\.");
            long left, right;
            if (pair[0].length() == 0) {
                left = 0;
            }
            else {
                left = Long.parseLong(pair[0]);
            }
            if (pair[1].length() > 8) {
                throw new RuntimeException("invalid amount string: " + amount);
            }
            else {
                right = Long.parseLong(pair[1]);
            }
            return (left * 100000000) + right * (long)Math.pow(10, 8 - pair[1].length());
        }
        else {
            throw new RuntimeException("invalid amount string: " + amount);
        }
    }

}
