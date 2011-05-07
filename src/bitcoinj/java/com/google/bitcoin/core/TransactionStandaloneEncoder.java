
package com.google.bitcoin.core;

import java.math.BigInteger;
import java.util.*;

import static com.google.bitcoin.core.Utils.*;


public class TransactionStandaloneEncoder {

    NetworkParameters params;

    ArrayList<ECKey> in_keys;
    ArrayList<byte[]> in_hashes;
    ArrayList<Integer> in_indexes;

    ArrayList<Address> out_addresses;
    ArrayList<BigInteger> out_amounts;

    public TransactionStandaloneEncoder(NetworkParameters params) {
        this.params = params;
        this.in_keys = new ArrayList<ECKey>();
        this.in_hashes = new ArrayList<byte[]>();
        this.in_indexes = new ArrayList<Integer>();
        this.out_addresses = new ArrayList<Address>();
        this.out_amounts = new ArrayList<BigInteger>();
    }

    public void addInput(ECKey key, int txIndex, String txHashHex) {
        addInput(key, txIndex, Utils.hexStringToBytes(txHashHex));
    }

    public void addInput(ECKey key, int txIndex, byte[] txHash) {
        in_keys.add(key);
        in_hashes.add(txHash);
        in_indexes.add(txIndex);
    }

    public void addOutput(BigInteger amount, String address) throws AddressFormatException {
        NetworkParameters params = NetworkParameters.prodNet();
        out_addresses.add(new Address(params, address));
        out_amounts.add(amount);
    }

    public Transaction createSignedTransaction() {

        NetworkParameters params = NetworkParameters.prodNet();
        Wallet wallet = new Wallet(params);
        for (int i = 0; i < in_keys.size(); i++) {
            wallet.addKey(in_keys.get(i));
        }

        // Prereq transactions
        ArrayList<TransactionOutput> in_outputs = new ArrayList<TransactionOutput>();
        Transaction t;
        TransactionOutput output;
        Address whatever = new ECKey().toAddress(params);
        for (int i = 0; i < in_keys.size(); i++) {

            t = new Transaction(params);

            // index (via spacers)
            int index = in_indexes.get(i).intValue();
            for (int j = 0; j < index; j++) {
                t.addOutput(new TransactionOutput(params, Utils.toNanoCoins(1, 0), whatever, t));
            }
            output = new TransactionOutput(params, Utils.toNanoCoins(1, 0), in_keys.get(i).toAddress(params), t);
            t.addOutput(output);
            in_outputs.add(output);

            // hash
            t.setFakeHashForTesting(new Sha256Hash(in_hashes.get(i)));

            try {
                wallet.receive(t, null, BlockChain.NewBlockType.BEST_CHAIN);
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        Transaction sendTx = new Transaction(params);

        // Inputs
        for (int i = 0; i < in_outputs.size(); i++) {
            sendTx.addInput(in_outputs.get(i));
        }

        // Outputs
        for (int i = 0; i < out_amounts.size(); i++) {
            sendTx.addOutput(new TransactionOutput(params, out_amounts.get(i), out_addresses.get(i), sendTx));
        }

        try {
            sendTx.signInputs(Transaction.SigHash.ALL, wallet);
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        }

        return sendTx;
    }
}

