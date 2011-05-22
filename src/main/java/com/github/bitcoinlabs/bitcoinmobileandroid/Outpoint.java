package com.github.bitcoinlabs.bitcoinmobileandroid;

public class Outpoint {
    private String address;
    private String hash;
    private int n;
    private long satoshis;

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

    public void setAddress(String address) {
        this.address = address;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public int getIndex() {
        return n;
    }

    public void setIndex(int index) {
        this.n = index;
    }

    public long getSatoshis() {
        return satoshis;
    }

    public void setSatoshis(long satoshis) {
        this.satoshis = satoshis;
    }

    @Override
    public String toString() {
        return "Outpoint [address=" + address + ", hash=" + hash + ", n=" + n
                + ", satoshis=" + satoshis + "]";
    }
}
