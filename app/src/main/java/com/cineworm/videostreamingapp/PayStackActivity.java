package com.cineworm.videostreamingapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.cineworm.util.API;
import com.cineworm.util.Constant;
import com.cineworm.util.NetworkUtils;
import com.cineworm.util.StatusBarUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.paystack.android.core.Paystack;
import com.paystack.android.ui.paymentsheet.PaymentSheet;
import com.paystack.android.ui.paymentsheet.PaymentSheetResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class PayStackActivity extends AppCompatActivity {

    String planId, planPrice, planCurrency, planGateway, planGateWayText, payStackPublicKey, couponCode, couponPercentage, paymentId, accessCode;
    Button btnPay;
    MyApplication myApplication;
    ProgressDialog pDialog;
    PaymentSheet paymentSheet;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        StatusBarUtil.setStatusBarGradiant(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(getString(R.string.payment));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        pDialog = new ProgressDialog(this);
        myApplication = MyApplication.getInstance();

        Intent intent = getIntent();
        planId = intent.getStringExtra("planId");
        planPrice = intent.getStringExtra("planPrice");
        planCurrency = intent.getStringExtra("planCurrency");
        planGateway = intent.getStringExtra("planGateway");
        planGateWayText = intent.getStringExtra("planGatewayText");
        payStackPublicKey = intent.getStringExtra("payStackPublicKey");
        couponCode = intent.getStringExtra("couponCode");
        couponPercentage = intent.getStringExtra("couponPercentage");


        btnPay = findViewById(R.id.btn_pay);
        String payString = getString(R.string.pay_via, planPrice, planCurrency, planGateWayText);
        btnPay.setText(payString);

        btnPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (NetworkUtils.isConnected(PayStackActivity.this)) {
                    getToken();
                } else {
                    Toast.makeText(PayStackActivity.this, getString(R.string.conne_msg1), Toast.LENGTH_SHORT).show();
                }
            }
        });
        getToken();
        initPaymentGateway();
        paymentSheet = new PaymentSheet(PayStackActivity.this, this::onPaymentSheetResult);
    }

    private void initPaymentGateway() {
        Paystack.builder()
                .setPublicKey(payStackPublicKey)
                // .setLoggingEnabled(true)
                .build();
    }

    private void onPaymentSheetResult(final PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Cancelled) {
            showError(getString(R.string.paypal_payment_error_2));
        } else if (paymentSheetResult instanceof PaymentSheetResult.Failed) {
            showError(getString(R.string.paypal_payment_error_1));
        } else if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            if (NetworkUtils.isConnected(PayStackActivity.this)) {
                new Transaction(PayStackActivity.this)
                        .purchasedItem(planId, paymentId, planGateway, couponCode, couponPercentage);
            } else {
                showError(getString(R.string.conne_msg1));
            }
        }
    }

    private void startPayment() {
        paymentSheet.launch(accessCode);
    }

    private void getToken() {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        jsObj.addProperty("amount", planPrice);
        jsObj.addProperty("email", myApplication.getUserEmail());
        params.put("data", API.toBase64(jsObj.toString()));
        client.post(Constant.PAYSTACK_TOKEN_URL, params, new AsyncHttpResponseHandler() {
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
                    JSONObject objJson;
                    if (jsonArray.length() > 0) {
                        objJson = jsonArray.getJSONObject(0);
                        paymentId = objJson.getString("reference");
                        accessCode = objJson.getString("access_code");
                        if (paymentId.isEmpty() && accessCode.isEmpty()) {
                            showError(getString(R.string.payment_token_error));
                        } else {
                            startPayment();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    showError(getString(R.string.payment_token_error));
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable
                    error) {
                dismissProgressDialog();
                showError(getString(R.string.stripe_token_error));
            }
        });
    }


    public void showProgressDialog() {
        pDialog.setMessage(getString(R.string.loading));
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(true);
        pDialog.show();
    }

    public void dismissProgressDialog() {
        if (pDialog != null && pDialog.isShowing())
            pDialog.dismiss();
    }

    private void showError(String Title) {
        new AlertDialog.Builder(PayStackActivity.this)
                .setTitle(getString(R.string.pay_stack_error_1))
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
