package com.github.bitcoinlabs.bitcoinmobileandroid;

public class BalanceResponse {
    final private long timeStamp;
    final private long satoshisConfirmed;
    final private long satoshisUnconfirmed;
    final private Exception exception;
    final private String serverError;
    
    public BalanceResponse(long timeStamp, long satoshisConfirmed, long satoshisUnconfirmed) {
        this.timeStamp = timeStamp;
        this.satoshisConfirmed = satoshisConfirmed;
        this.satoshisUnconfirmed = satoshisUnconfirmed;
        this.exception = null;
        this.serverError = null;
    }
    
    public BalanceResponse(Exception exception, String serverError) {
        this.exception = exception;
        this.serverError = serverError;
        this.timeStamp = System.currentTimeMillis();
        this.satoshisConfirmed = Long.MIN_VALUE;
        this.satoshisUnconfirmed = Long.MIN_VALUE;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public long getSatoshisConfirmed() {
        checkValidResponse();
        return satoshisConfirmed;
    }

    public long getSatoshisUnconfirmed() {
        checkValidResponse();
        return satoshisUnconfirmed;
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
}
