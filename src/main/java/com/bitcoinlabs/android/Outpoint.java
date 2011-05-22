package com.bitcoinlabs.android;


public class Outpoint {
    final private String address;
    final private String hash;
    final private int n;
    final private long satoshis;

    public Outpoint(String address, String hash, int n, long satoshis) {
        super();
        this.address = address;
        this.hash = hash;
        this.n = n;
        this.satoshis = satoshis;
    }

    public String getAddress() {
        return address;
    }

    public String getHash() {
        return hash;
    }

    public int getIndex() {
        return n;
    }

    public long getSatoshis() {
        return satoshis;
    }

    @Override
    public String toString() {
        return "Outpoint [address=" + address + ", hash=" + hash + ", n=" + n
                + ", satoshis=" + satoshis + "]";
    }
}
