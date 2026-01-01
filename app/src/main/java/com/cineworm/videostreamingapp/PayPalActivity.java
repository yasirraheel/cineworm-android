package com.cineworm.videostreamingapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.braintreepayments.api.ClientTokenCallback;
import com.braintreepayments.api.ClientTokenProvider;
import com.braintreepayments.api.DropInClient;
import com.braintreepayments.api.DropInListener;
import com.braintreepayments.api.DropInRequest;
import com.braintreepayments.api.DropInResult;
import com.braintreepayments.api.PayPalCheckoutRequest;
import com.braintreepayments.api.PayPalPaymentIntent;
import com.cineworm.util.API;
import com.cineworm.util.Constant;
import com.cineworm.util.IsRTL;
import com.cineworm.util.NetworkUtils;
import com.cineworm.util.StatusBarUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import cz.msebera.android.httpclient.Header;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class PayPalActivity extends AppCompatActivity {

    String planId, planPrice, planCurrency, planGateway, planGateWayText, couponCode, couponPercentage;
    Button btnPay;
    ProgressDialog pDialog;
    DropInClient dropInClient;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        StatusBarUtil.setStatusBarGradiant(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        IsRTL.ifSupported(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(getString(R.string.payment));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        Intent intent = getIntent();
        planId = intent.getStringExtra("planId");
        planPrice = intent.getStringExtra("planPrice");
        planCurrency = intent.getStringExtra("planCurrency");
        planGateway = intent.getStringExtra("planGateway");
        planGateWayText = intent.getStringExtra("planGatewayText");
        couponCode = intent.getStringExtra("couponCode");
        couponPercentage = intent.getStringExtra("couponPercentage");

        pDialog = new ProgressDialog(this);
        btnPay = findViewById(R.id.btn_pay);
        String payString = getString(R.string.pay_via, planPrice, planCurrency, planGateWayText);
        btnPay.setText(payString);

        btnPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchDropIn();
            }
        });
        configureDropInClient();
        launchDropIn();
    }

    private void configureDropInClient() {
        dropInClient = new DropInClient(this, new TokenProvider());
        dropInClient.setListener(new DropInListener() {
            @Override
            public void onDropInSuccess(@NonNull DropInResult dropInResult) {
                String nNonce = Objects.requireNonNull(dropInResult.getPaymentMethodNonce()).getString();
                checkoutNonce(nNonce);
            }

            @Override
            public void onDropInFailure(@NonNull Exception error) {
                showError(error.getMessage());
            }
        });
    }

    private class TokenProvider implements ClientTokenProvider {
        @Override
        public void getClientToken(@NonNull ClientTokenCallback clientTokenCallback) {
            generateToken(clientTokenCallback);
        }
    }

    private void generateToken(ClientTokenCallback clientTokenCallback) {

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        params.put("data", API.toBase64(jsObj.toString()));
        client.post(Constant.BRAIN_TREE_TOKEN_URL, params, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();
                showProgressDialog();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                dismissProgressDialog();
                String result = new String(responseBody);
                try {
                    JSONObject mainJson = new JSONObject(result);
                    JSONArray jsonArray = mainJson.getJSONArray(Constant.ARRAY_NAME);
                    JSONObject objJson = jsonArray.getJSONObject(0);
                    if (objJson.getString("success").equals("1")) {
                        clientTokenCallback.onSuccess(objJson.getString("client_token"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    clientTokenCallback.onFailure(new Exception(getString(R.string.paypal_payment_error_1)));
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                dismissProgressDialog();
                clientTokenCallback.onFailure(new Exception(getString(R.string.paypal_payment_error_1)));
            }

        });
    }

    private void launchDropIn() {
        DropInRequest dropInRequest = new DropInRequest(false);
        dropInRequest.setPayPalRequest(getPaypalRequest());
        dropInClient.launchDropIn(dropInRequest);
    }

    private PayPalCheckoutRequest getPaypalRequest() {
        PayPalCheckoutRequest request = new PayPalCheckoutRequest(planPrice,false);
        request.setCurrencyCode(planCurrency);
        request.setIntent(PayPalPaymentIntent.SALE);
        return request;
    }

    private void checkoutNonce(String paymentNonce) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        jsObj.addProperty("payment_nonce", paymentNonce);
        jsObj.addProperty("payment_amount", planPrice);
        params.put("data", API.toBase64(jsObj.toString()));

        client.post(Constant.BRAIN_TREE_CHECK_OUT_URL, params, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();
                showProgressDialog();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                dismissProgressDialog();
                String result = new String(responseBody);
                try {
                    JSONObject mainJson = new JSONObject(result);
                    JSONArray jsonArray = mainJson.getJSONArray(Constant.ARRAY_NAME);
                    JSONObject objJson = jsonArray.getJSONObject(0);
                    if (objJson.getString("success").equals("1")) {
                        String paymentId = objJson.getString("paypal_payment_id"); //objJson.getString("transaction_id")
                        if (NetworkUtils.isConnected(PayPalActivity.this)) {
                            new Transaction(PayPalActivity.this)
                                    .purchasedItem(planId, paymentId, planGateway, couponCode, couponPercentage);
                        } else {
                            showError(getString(R.string.conne_msg1));
                        }
                    } else {
                        showError(objJson.getString("msg"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                dismissProgressDialog();
            }

        });
    }

    public void showProgressDialog() {
        pDialog.setMessage(PayPalActivity.this.getString(R.string.loading));
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();
    }

    public void dismissProgressDialog() {
        if (pDialog != null && pDialog.isShowing()) {
            pDialog.dismiss();
        }
    }

    private void showError(String Title) {
        new AlertDialog.Builder(PayPalActivity.this)
                .setTitle(getString(R.string.paypal_payment_error_4))
                .setMessage(Title)
                .setIcon(R.mipmap.ic_launcher)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
