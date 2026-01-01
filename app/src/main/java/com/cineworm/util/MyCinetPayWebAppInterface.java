package com.cineworm.util;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.cinetpay.androidsdk.CinetPayWebAppInterface;
import com.cineworm.videostreamingapp.R;
import com.cineworm.videostreamingapp.Transaction;

import org.json.JSONException;
import org.json.JSONObject;

public class MyCinetPayWebAppInterface extends CinetPayWebAppInterface {
    String planId, couponCode, couponPercentage;
    String gatewayName;
    Context context;

    public MyCinetPayWebAppInterface(Context c,
                                     String apikey,
                                     String site_id,
                                     String transaction_id,
                                     int amount,
                                     String currency,
                                     String description,
                                     String gatewayName,
                                     String planId,
                                     String couponCode,
                                     String couponPercentage) {
        super(c, apikey, site_id, transaction_id, amount, currency, description);
        this.planId = planId;
        this.gatewayName = gatewayName;
        this.couponCode = couponCode;
        this.couponPercentage = couponPercentage;
        this.context = c;
    }

    @Override
    @JavascriptInterface
    public void onResponse(String response) {
        Log.d("MyCinetPayWebApp", response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.has("status")) {
                String statusSuccess = jsonObject.getString("status");
                String transId = jsonObject.getString("operator_id");
                if (statusSuccess.equals("ACCEPTED")) {
                    new Transaction((Activity) context)
                            .purchasedItem(planId, transId, gatewayName, couponCode, couponPercentage);
                } else {
                    Toast.makeText(context, context.getString(R.string.paypal_payment_error_1), Toast.LENGTH_SHORT).show();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    @JavascriptInterface
    public void onError(String response) {
        Log.d("onerror", response);
    }
}
