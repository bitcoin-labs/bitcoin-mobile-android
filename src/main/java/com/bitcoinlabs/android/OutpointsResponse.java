package com.bitcoinlabs.android;

import java.util.Collection;

public class OutpointsResponse {
    final private long timeStamp;
    final private Exception exception;
    final private String serverError;
    final private Collection<Outpoint> unspent_outpoints;
    
    public OutpointsResponse(long timeStamp, Collection<Outpoint> unspent_outpoints) {
        this.timeStamp = timeStamp;
        this.unspent_outpoints = unspent_outpoints;
        this.exception = null;
        this.serverError = null;
    }
    
    public OutpointsResponse(Exception exception, String serverError) {
        this.exception = exception;
        this.serverError = serverError;
        this.unspent_outpoints = null;
        this.timeStamp = System.currentTimeMillis();
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    private void checkValidResponse() {
        if (isError()) {
            throw new IllegalStateException("Cannot retrieve balance from failed response");
        }
    }

    public boolean isError() {
        return getException() != null || getServerError() != null;
    }

    public Exception getException() {
        return exception;
    }

    public String getServerError() {
        return serverError;
    }

    public Collection<Outpoint> getUnspent_outpoints() {
        return unspent_outpoints;
    }

    @Override
    public String toString() {
        return "OutpointsResponse [timeStamp=" + timeStamp + ", exception="
                + exception + ", serverError=" + serverError
                + ", unspent_outpoints=" + unspent_outpoints + "]";
    }
    
}
