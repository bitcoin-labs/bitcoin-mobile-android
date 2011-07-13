package com.bitcoinlabs.android.settings;

import android.content.Context;
import android.preference.Preference;
import android.view.View;

public class ExitNode extends Preference {

    private String url;

    public ExitNode(Context context, String url) {
        super(context);
        this.url = url==null?"":url;
    }

    @Override
    protected void onBindView(View view) {
        setTitle(url);
        super.onBindView(view);
    }

    public String getUrl() {
        return url;
    }

}
