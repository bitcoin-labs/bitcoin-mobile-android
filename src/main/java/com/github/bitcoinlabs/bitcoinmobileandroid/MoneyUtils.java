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
    public static String formatMoney(double val)
    {
        final DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
        return decimalFormat.format(val);
    }

}
