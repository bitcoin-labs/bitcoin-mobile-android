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
        final DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
        return decimalFormat.format(val);
    }
    
    public static String formatSatoshisAsBtcString(long satoshis) {
        final DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
        double btcAmountDouble = satoshis * 1.0 / SATOSHIS_PER_BITCOIN;
        String btcString = decimalFormat.format(btcAmountDouble);
        return btcString;
    }
    
    public static long btcStringToSatoshis(String btcAmount) {
        if (btcAmount == null) {
            throw new NullPointerException("btcAmount may not be null");
        }
        try {
            double btcAmountDouble = Double.parseDouble(btcAmount);
            
            long satoshis = (long)(btcAmountDouble * SATOSHIS_PER_BITCOIN);
            return satoshis;
        } catch (NumberFormatException e) {
            throw new RuntimeException("btcAmount not a valid decimal: " + btcAmount, e);
        }
    }

}
