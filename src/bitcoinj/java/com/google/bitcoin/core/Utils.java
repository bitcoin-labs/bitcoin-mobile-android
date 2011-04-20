/**
 * Copyright 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.bitcoin.core;

import com.google.bitcoin.bouncycastle.crypto.digests.RIPEMD160Digest;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * A collection of various utility methods that are helpful for working with the BitCoin protocol.
 * To enable debug logging from the library, run with -Dbitcoinj.logging=true on your command line.
 */
@SuppressWarnings({"SameParameterValue"})
public class Utils {
    // TODO: Replace this nanocoins business with something better.

    /**
     * How many "nanocoins" there are in a BitCoin.
     *
     * A nanocoin is the smallest unit that can be transferred using BitCoin.
     * The term nanocoin is very misleading, though, because there are only 100 million
     * of them in a coin (whereas one would expect 1 billion.
     */
    public static final BigInteger COIN = new BigInteger("100000000", 10);

    /**
     * How many "nanocoins" there are in 0.01 BitCoins.
     *
     * A nanocoin is the smallest unit that can be transferred using BitCoin.
     * The term nanocoin is very misleading, though, because there are only 100 million
     * of them in a coin (whereas one would expect 1 billion).
     */
    public static final BigInteger CENT = new BigInteger("1000000", 10);

    private static final boolean logging;

    static {
        logging = Boolean.parseBoolean(System.getProperty("bitcoinj.logging", "false"));
    }

    /** Convert an amount expressed in the way humans are used to into nanocoins. */
    public static BigInteger toNanoCoins(int coins, int cents) {
        assert cents < 100;
        BigInteger bi = BigInteger.valueOf(coins).multiply(COIN);
        bi = bi.add(BigInteger.valueOf(cents).multiply(CENT));
        return bi;
    }

    /**
     * Convert an amount expressed in the way humans are used to into nanocoins.<p>
     *
     * This takes string in a format understood by {@link BigDecimal#BigDecimal(String)},
     * for example "0", "1", "0.10", "1.23E3", "1234.5E-5".
     * 
     * @throws ArithmeticException if you try to specify fractional nanocoins
     **/
    public static BigInteger toNanoCoins(String coins){
        return new BigDecimal(coins).movePointRight(8).toBigIntegerExact();
    }

    public static void uint32ToByteArrayBE(long val, byte[] out, int offset) {
        out[offset + 0] = (byte) (0xFF & (val >> 24));
        out[offset + 1] = (byte) (0xFF & (val >> 16));
        out[offset + 2] = (byte) (0xFF & (val >>  8));
        out[offset + 3] = (byte) (0xFF & (val >>  0));      
    }

    public static void uint32ToByteArrayLE(long val, byte[] out, int offset) {
        out[offset + 0] = (byte) (0xFF & (val >>  0));
        out[offset + 1] = (byte) (0xFF & (val >>  8));
        out[offset + 2] = (byte) (0xFF & (val >> 16));
        out[offset + 3] = (byte) (0xFF & (val >> 24));      
    }
    
    public static void uint32ToByteStreamLE(long val,  OutputStream stream) throws IOException {
        stream.write((int)(0xFF & (val >>  0)));
        stream.write((int)(0xFF & (val >>  8)));
        stream.write((int)(0xFF & (val >> 16)));
        stream.write((int)(0xFF & (val >> 24)));
    }
    
    public static void uint64ToByteStreamLE( BigInteger val,  OutputStream stream) throws IOException {
        byte[] bytes = val.toByteArray();
        if (bytes.length > 8) { 
            throw new RuntimeException("Input too large to encode into a uint64");
        }
        bytes = reverseBytes(bytes);
        stream.write(bytes);
        if (bytes.length < 8) {
            for (int i = 0; i < 8 - bytes.length; i++)
                stream.write(0);
        }
    }

    /**
     * See {@link Utils#doubleDigest(byte[],int,int)}.
     */
    public static byte[] doubleDigest(byte[] input) {
        return doubleDigest(input, 0, input.length);
    }

    /**
     * Calculates the SHA-256 hash of the given byte range, and then hashes the resulting hash again. This is
     * standard procedure in BitCoin. The resulting hash is in big endian form.
     */
    public static byte[] doubleDigest(byte[] input, int offset, int length) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(input, offset, length);
            byte[] first = digest.digest();
            return digest.digest(first);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);  // Cannot happen.
        }
    }

    /**
     * Calculates SHA256(SHA256(byte range 1 + byte range 2)).
     */
    public static byte[] doubleDigestTwoBuffers(byte[] input1, int offset1, int length1,
                                                byte[] input2, int offset2, int length2) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(input1, offset1, length1);
            digest.update(input2, offset2, length2);
            byte[] first = digest.digest();
            return digest.digest(first);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);  // Cannot happen.
        }
    }

    /** Work around lack of unsigned types in Java. */
    public static boolean isLessThanUnsigned(long n1, long n2) { 
        return (n1 < n2) ^ ((n1 < 0) != (n2 < 0));
    }

    /** Returns the given byte array hex encoded. */
    public static String bytesToHexString(byte[] bytes) {
        StringBuffer buf = new StringBuffer(bytes.length * 2);
        for (byte b : bytes) {
            String s = Integer.toString(0xFF & b, 16);
            if (s.length() < 2)
                buf.append('0');
            buf.append(s);
        }
        return buf.toString();
    }
    

    /** Returns a copy of the given byte array in reverse order. */
    public static byte[] reverseBytes(byte[] bytes) {
        // We could use the XOR trick here but it's easier to understand if we don't. If we find this is really a
        // performance issue the matter can be revisited.
        byte[] buf = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++)
            buf[i] = bytes[bytes.length - 1 - i];
        return buf;
    }

    public static long readUint32(byte[] bytes, int offset) {
        return ((bytes[offset++] & 0xFFL) <<  0) |
               ((bytes[offset++] & 0xFFL) <<  8) |
               ((bytes[offset++] & 0xFFL) << 16) |
               ((bytes[offset] & 0xFFL) << 24);
    }
    
    public static long readUint32BE(byte[] bytes, int offset) {
        return ((bytes[offset + 0] & 0xFFL) << 24) |
               ((bytes[offset + 1] & 0xFFL) << 16) |
               ((bytes[offset + 2] & 0xFFL) <<  8) |
               ((bytes[offset + 3] & 0xFFL) <<  0);
    }

    static void LOG(String msg) {
        // Set this to true to see debug prints from the library.
        if (logging) {
            System.out.print("BitCoin: ");
            System.out.println(msg);
        }
    }

    /**
     * Calculates RIPEMD160(SHA256(input)). This is used in Address calculations.
     */
    public static byte[] sha256hash160(byte[] input) {
        try {
            byte[] sha256 = MessageDigest.getInstance("SHA-256").digest(input);
            RIPEMD160Digest digest = new RIPEMD160Digest();
            digest.update(sha256, 0, sha256.length);
            byte[] out = new byte[20];
            digest.doFinal(out, 0);
            return out;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);  // Cannot happen.
        }
    }

    /** Returns the given value in nanocoins as a 0.12 type string. */
    public static String bitcoinValueToFriendlyString(BigInteger value) {
        BigInteger coins = value.divide(COIN);
        BigInteger cents = value.remainder(COIN);
        return String.format("%d.%02d", coins.intValue(), cents.intValue() / 1000000);
    }
    
    /**
     * MPI encoded numbers are produced by the OpenSSL BN_bn2mpi function. They consist of
     * a 4 byte big endian length field, followed by the stated number of bytes representing
     * the number in big endian format.
     */
    static BigInteger decodeMPI(byte[] mpi) {
        int length = (int) readUint32BE(mpi, 0);
        byte[] buf = new byte[length];
        System.arraycopy(mpi, 4, buf, 0, length);
        return new BigInteger(buf);
    }

    // The representation of nBits uses another home-brew encoding, as a way to represent a large
    // hash value in only 32 bits.
    static BigInteger decodeCompactBits(long compact) {
        int size = ((int)(compact >> 24)) & 0xFF;
        byte[] bytes = new byte[4 + size];
        bytes[3] = (byte) size;
        if (size >= 1) bytes[4] = (byte) ((compact >> 16) & 0xFF);
        if (size >= 2) bytes[5] = (byte) ((compact >>  8) & 0xFF);
        if (size >= 3) bytes[6] = (byte) ((compact >>  0) & 0xFF);
        return decodeMPI(bytes);
    }
}
