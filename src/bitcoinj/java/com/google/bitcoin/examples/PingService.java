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

package com.google.bitcoin.examples;

import com.google.bitcoin.core.*;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * PingService demonstrates basic usage of the library. It sits on the network and when it receives coins, simply
 * sends them right back to the previous owner, determined rather arbitrarily by the address of the first input.
 */
public class PingService {
    public static void main(String[] args) throws Exception {
        boolean testNet = args.length > 0 && args[0].equalsIgnoreCase("testnet");
        final NetworkParameters params = testNet ? NetworkParameters.testNet() : NetworkParameters.prodNet();
        String filePrefix = testNet ? "pingservice-testnet" : "pingservice-prodnet";

        // Try to read the wallet from storage, create a new one if not possible.
        Wallet wallet;
        final File walletFile = new File(filePrefix + ".wallet");
        try {
            wallet = Wallet.loadFromFile(walletFile);
        } catch (IOException e) {
            wallet = new Wallet(params);
            wallet.keychain.add(new ECKey());
            wallet.saveToFile(walletFile);
        }
        // Fetch the first key in the wallet (should be the only key).
        ECKey key = wallet.keychain.get(0);

        // Load the block chain, if there is one stored locally.
        System.out.println("Reading block store from disk");
        BlockStore blockStore = new DiskBlockStore(params, new File(filePrefix + ".blockchain"));

        // Connect to the localhost node.
        System.out.println("Connecting ...");
        NetworkConnection conn = new NetworkConnection(InetAddress.getLocalHost(), params,
                                                       blockStore.getChainHead().getHeight());
        BlockChain chain = new BlockChain(params, wallet, blockStore);
        final Peer peer = new Peer(params, conn, chain);
        peer.start();

        // We want to know when the balance changes.
        wallet.addEventListener(new WalletEventListener() {
            public void onCoinsReceived(Wallet w, Transaction tx, BigInteger prevBalance, BigInteger newBalance) {
                // Running on a peer thread.
                assert !newBalance.equals(BigInteger.ZERO);
                // It's impossible to pick one specific identity that you receive coins from in BitCoin as there
                // could be inputs from many addresses. So instead we just pick the first and assume they were all
                // owned by the same person.
                try {
                    TransactionInput input = tx.getInputs().get(0);
                    Address from = input.getFromAddress();
                    BigInteger value = tx.getValueSentToMe(w);
                    System.out.println("Received " + Utils.bitcoinValueToFriendlyString(value) + " from " + from.toString());
                    // Now send the coins back!
                    Transaction sendTx = w.sendCoins(peer, from, value);
                    assert sendTx != null;  // We should never try to send more coins than we have!
                    System.out.println("Sent coins back! Transaction hash is " + sendTx.getHashAsString());
                    w.saveToFile(walletFile);
                } catch (ScriptException e) {
                    // If we didn't understand the scriptSig, just crash.
                    e.printStackTrace();
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        });

        CountDownLatch progress = peer.startBlockChainDownload();
        long max = progress.getCount();  // Racy but no big deal.
        if (max > 0) {
            System.out.println("Downloading block chain. " + (max > 1000 ? "This may take a while." : ""));
            long current = max;
            while (current > 0) {
                double pct = 100.0 - (100.0 * (current / (double)max));
                System.out.println(String.format("Chain download %d%% done", (int)pct));
                progress.await(1, TimeUnit.SECONDS);
                current = progress.getCount();
            }
        }
        System.out.println("Send coins to: " + key.toAddress(params).toString());
        System.out.println("Waiting for coins to arrive. Press Ctrl-C to quit.");
        // The peer thread keeps us alive until something kills the process.
    }
}
