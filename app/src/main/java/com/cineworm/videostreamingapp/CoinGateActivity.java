package com.cineworm.videostreamingapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.cineworm.util.API;
import com.cineworm.util.Constant;
import com.cineworm.util.Events;
import com.cineworm.util.GlobalBus;
import com.cineworm.util.IsRTL;
import com.cineworm.util.PaymentWebAppInterface;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class CoinGateActivity extends AppCompatActivity {
    String planId, planPrice, planCurrency, planGateway, planGateWayText, planName, couponCode, couponPercentage;
    String cgOrderId, paymentUrl;
    MyApplication myApplication;
    WebView webView;
    ProgressBar progressBar;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coingate);
        GlobalBus.getBus().register(this);
        IsRTL.ifSupported(this);
        myApplication = MyApplication.getInstance();

        Intent intent = getIntent();
        planId = intent.getStringExtra("planId");
        planName = intent.getStringExtra("planName");
        planPrice = intent.getStringExtra("planPrice");
        planCurrency = intent.getStringExtra("planCurrency");
        planGateway = intent.getStringExtra("planGateway");
        planGateWayText = intent.getStringExtra("planGatewayText");
        couponCode = intent.getStringExtra("couponCode");
        couponPercentage = intent.getStringExtra("couponPercentage");

        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.load);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        webView.getSettings().setDefaultTextEncodingName("UTF-8");
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.addJavascriptInterface(new PaymentWebAppInterface(this), "Android");
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int progress) {
                if (progress == 100) {
                    progressBar.setVisibility(View.GONE);
                    webView.setVisibility(View.VISIBLE);
                }

            }
        });
        webView.setWebViewClient(new WebViewClient());
        getPaymentUrl();
    }

    private void getPaymentUrl() {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        jsObj.addProperty("plan_id", planId);
        jsObj.addProperty("coupon_code", couponCode);
        jsObj.addProperty("coupon_percentage", couponPercentage);
        params.put("data", API.toBase64(jsObj.toString()));
        client.post(Constant.COIN_GATE_PAYMENT_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                //    progressBar.setVisibility(View.GONE);

                String result = new String(responseBody);
                try {
                    JSONObject mainJson = new JSONObject(result);
                    JSONArray jsonArray = mainJson.getJSONArray(Constant.ARRAY_NAME);
                    JSONObject objJson;
                    if (jsonArray.length() > 0) {
                        objJson = jsonArray.getJSONObject(0);
                        if (objJson.getString("success").equals("1")) {
                            cgOrderId = objJson.getString("cg_order_id");
                            paymentUrl = objJson.getString("payment_url");
                            startPayment();
                        } else {
                            showError(getString(R.string.payment_token_error));
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable
                    error) {
                progressBar.setVisibility(View.GONE);
                showError(getString(R.string.payment_token_error));
            }
        });
    }

    private void getPaymentStatus() {
        webView.setVisibility(View.GONE);
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        jsObj.addProperty("cg_order_id", cgOrderId);
        params.put("data", API.toBase64(jsObj.toString()));
        client.post(Constant.COIN_GATE_PAYMENT_STATUS_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                //    progressBar.setVisibility(View.GONE);

                String result = new String(responseBody);
                try {
                    JSONObject mainJson = new JSONObject(result);
                    JSONArray jsonArray = mainJson.getJSONArray(Constant.ARRAY_NAME);
                    JSONObject objJson;
                    if (jsonArray.length() > 0) {
                        objJson = jsonArray.getJSONObject(0);
                        if (objJson.getString("success").equals("1")) {
                            String paymentId = objJson.getString("payment_id");
                            new Transaction(CoinGateActivity.this)
                                    .purchasedItem(planId, paymentId, planGateway, couponCode, couponPercentage);
                        } else {
                            showError(getString(R.string.payment_failed));
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable
                    error) {
                progressBar.setVisibility(View.GONE);
                showError(getString(R.string.payment_failed));
            }
        });
    }

    public void startPayment() {
        webView.loadUrl(paymentUrl);
    }

    private void showError(String Title) {
        new AlertDialog.Builder(CoinGateActivity.this)
                .setTitle(getString(R.string.coin_gate))
                .setMessage(Title)
                .setIcon(R.mipmap.ic_launcher)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .show();
    }


    @Subscribe
    public void onEvent(Events.CoinGateSuccess coinGateSuccess) {
        findUiHandler().post(new Runnable() {
            @Override
            public void run() {
                getPaymentStatus();
            }
        });

    }

    @Subscribe
    public void onEvent(Events.CoinGateFailed coinGateFailed) {
        findUiHandler().post(new Runnable() {
            @Override
            public void run() {
                showError(getString(R.string.payment_failed));
            }
        });
    }

    private Handler findUiHandler() {
        return new Handler(Looper.getMainLooper());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GlobalBus.getBus().unregister(this);
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
