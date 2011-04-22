package com.github.bitcoinlabs.bitcoinmobileandroid;

import java.util.ArrayList;
import java.math.BigInteger;
import java.io.*;

import com.google.bitcoin.core.ECKey;
import com.google.bitcoin.core.Utils;
import com.google.bitcoin.core.VarInt;

public class RawTransactionMessage {

    ArrayList<ECKey> in_keys;
    ArrayList<byte[]> in_hashes;
    ArrayList<Integer> in_indexes;

    ArrayList<String> out_addresses;
    ArrayList<BigInteger> out_amounts;

    public RawTransactionMessage() {
        in_keys = new ArrayList<ECKey>();
        in_hashes = new ArrayList<byte[]>();
        in_indexes = new ArrayList<Integer>();
        out_addresses = new ArrayList<String>();
        out_amounts = new ArrayList<BigInteger>();
    }

    public void addInput(ECKey key, byte[] txHash, int txIndex) {
        in_keys.add(key);
        in_hashes.add(txHash);
        in_indexes.add(txIndex);
    }

    public void addOutput(String address, BigInteger amount) {
        out_addresses.add(address);
        out_amounts.add(amount);
    }

    public byte[] bitcoinSerialize() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            bitcoinSerializeToStream(stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return stream.toByteArray();
    }

    public void bitcoinSerializeToStream(OutputStream stream) throws IOException {

        // version
        Utils.uint32ToByteStreamLE(1, stream);

        // inputs
        stream.write(new VarInt(in_keys.size()).encode());
        for (int i = 0; i < in_keys.size(); i++) {

            // hash
            stream.write(in_hashes.get(i));// Note: core.TransactionOutPoint uses Utils.reverseBytes

            // index
            Utils.uint32ToByteStreamLE(in_indexes.get(i), stream);

            // signature
            // TODO
            // stream.write(new VarInt(scriptBytes.length).encode());
            // stream.write(scriptBytes);

            // sequence
            Utils.uint32ToByteStreamLE(0, stream);
        }

        // outputs
        stream.write(new VarInt(out_addresses.size()).encode());
        for (int i = 0; i < out_addresses.size(); i++) {

            // amount
            Utils.uint64ToByteStreamLE(out_amounts.get(i), stream);

            // script
            // TODO
            //stream.write(new VarInt(scriptBytes.length).encode());
            //stream.write(scriptBytes);
        }

        // lockTime
        Utils.uint32ToByteStreamLE(0, stream);
    }
}
