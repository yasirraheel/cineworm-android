package com.cineworm.videostreamingapp;

import android.content.Intent;
import android.os.Bundle;

import com.cinetpay.androidsdk.CinetPayActivity;
import com.cineworm.util.MyCinetPayWebAppInterface;

public class MyCinetPayActivity extends CinetPayActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String api_key = intent.getStringExtra(KEY_API_KEY);
        String site_id = intent.getStringExtra(KEY_SITE_ID);
        String transaction_id = intent.getStringExtra(KEY_TRANSACTION_ID);
        int amount = intent.getIntExtra(KEY_AMOUNT, 0);
        String currency = intent.getStringExtra(KEY_CURRENCY);
        String description = intent.getStringExtra(KEY_DESCRIPTION);
        String channels = intent.getStringExtra(KEY_CHANNELS);
        String customer_name = intent.getStringExtra(KEY_CUSTOMER_NAME);
        String customer_surname = intent.getStringExtra(KEY_CUSTOMER_SURNAME);
        String customer_email = intent.getStringExtra(KEY_CUSTOMER_EMAIL);
        String customer_address = intent.getStringExtra(KEY_CUSTOMER_ADDRESS);
        String customer_phone_number = intent.getStringExtra(KEY_CUSTOMER_PHONE_NUMBER);
        String customer_city = intent.getStringExtra(KEY_CUSTOMER_CITY);
        String customer_country = intent.getStringExtra(KEY_CUSTOMER_COUNTRY);
        String customer_zip_code = intent.getStringExtra(KEY_CUSTOMER_ZIP_CODE);
        String gateway_name = intent.getStringExtra("planGateway");
        String plan_id = intent.getStringExtra("planId");
        String coupon_code = intent.getStringExtra("couponCode");
        String coupon_percentage = intent.getStringExtra("couponPercentage");

        mWebView
                .addJavascriptInterface(
                        new MyCinetPayWebAppInterface(
                                this,
                                api_key,
                                site_id,
                                transaction_id,
                                amount,
                                currency,
                                description,
                                gateway_name,
                                plan_id,
                                coupon_code,
                                coupon_percentage
                        )
                                .setChannels(channels)
                                .setCustomerName(customer_name)
                                .setCustomerSurname(customer_surname)
                                .setCustomerEmail(customer_email)
                                .setCustomerAddress(customer_address)
                                .setCustomerPhoneNumber(customer_phone_number)
                                .setCustomerCity(customer_city)
                                .setCustomerCountry(customer_country)
                                .setCustomerZipCode(customer_zip_code)
                        /*.setCustomerState("A_STATE")*/,
                        "Android");
    }
}
