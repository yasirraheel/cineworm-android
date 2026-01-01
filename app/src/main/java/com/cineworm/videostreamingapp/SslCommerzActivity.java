package com.cineworm.videostreamingapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.cineworm.util.NetworkUtils;
import com.sslwireless.sslcommerzlibrary.model.initializer.SSLCAdditionalInitializer;
import com.sslwireless.sslcommerzlibrary.model.initializer.SSLCommerzInitialization;
import com.sslwireless.sslcommerzlibrary.model.response.SSLCTransactionInfoModel;
import com.sslwireless.sslcommerzlibrary.model.util.SSLCSdkType;
import com.sslwireless.sslcommerzlibrary.view.singleton.IntegrateSSLCommerz;
import com.sslwireless.sslcommerzlibrary.viewmodel.listener.SSLCTransactionResponseListener;

import java.util.Locale;
import java.util.Random;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class SslCommerzActivity extends AppCompatActivity implements SSLCTransactionResponseListener {

    String planId, planPrice, planCurrency, planGateway, planGateWayText, sslStoreId, sslStorePassword, couponCode, couponPercentage;
    Button btnPay;
    MyApplication myApplication;
    ProgressDialog pDialog;
    boolean isSandbox = false;
    SSLCommerzInitialization sslCommerzInitialization;
    SSLCAdditionalInitializer additionalInitialization;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
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
        sslStoreId = intent.getStringExtra("sslStoreId");
        sslStorePassword = intent.getStringExtra("sslStorePassword");
        couponCode = intent.getStringExtra("couponCode");
        couponPercentage = intent.getStringExtra("couponPercentage");
        isSandbox = intent.getBooleanExtra("isSandbox", false);

        btnPay = findViewById(R.id.btn_pay);
        String payString = getString(R.string.pay_via, planPrice, planCurrency, planGateWayText);
        btnPay.setText(payString);

        initPaymentGateway();

        btnPay.setOnClickListener(view -> initPaymentGateway());
    }

    private void initPaymentGateway() {
        double amount = Double.parseDouble(planPrice);
        sslCommerzInitialization = new SSLCommerzInitialization(sslStoreId, sslStorePassword, amount, planCurrency,
                getTransactionId(), getString(R.string.payment_company_name), isSandbox ? SSLCSdkType.TESTBOX : SSLCSdkType.LIVE);
        additionalInitialization = new SSLCAdditionalInitializer();
        additionalInitialization.setValueA("");

        IntegrateSSLCommerz.getInstance(this).addSSLCommerzInitialization(sslCommerzInitialization).buildApiCall(this);
    }

    public String getTransactionId() {
        Random rnd = new Random();
        int number = rnd.nextInt(999999);
        String orderId = String.format(Locale.getDefault(), "%06d", number);
        return "Ssl" + myApplication.getUserId() + orderId;
    }

    @Override
    public void transactionSuccess(SSLCTransactionInfoModel sslcTransactionInfoModel) {
        if (NetworkUtils.isConnected(SslCommerzActivity.this)) {
            new Transaction(SslCommerzActivity.this)
                    .purchasedItem(planId, sslcTransactionInfoModel.getTranId(), planGateway, couponCode, couponPercentage);
        } else {
            showError(getString(R.string.conne_msg1));
        }
    }

    @Override
    public void transactionFail(String s) {
        showError(s);
    }

    @Override
    public void closed(String s) {
        showError(s);
    }

    private void showError(String Title) {
        new AlertDialog.Builder(SslCommerzActivity.this)
                .setTitle(getString(R.string.ssl_commerz))
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
}
