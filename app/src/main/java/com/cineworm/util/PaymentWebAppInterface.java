package com.cineworm.util;

import android.content.Context;
import android.webkit.JavascriptInterface;

public class PaymentWebAppInterface {
    Context mContext;

    public PaymentWebAppInterface(Context c) {
        mContext = c;
    }

    @JavascriptInterface
    public void callSuccess() {
        GlobalBus.getBus().post(new Events.CoinGateSuccess());
    }

    @JavascriptInterface
    public void callFailed() {
        GlobalBus.getBus().post(new Events.CoinGateFailed());
    }
}
