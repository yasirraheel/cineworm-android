package com.cineworm.videostreamingapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.webkit.WebView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.cineworm.util.IsRTL;
import com.cineworm.util.StatusBarUtil;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class BankTransferActivity extends BaseActivity {

    private WebView webView;
    private String bankTransferInfo;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        StatusBarUtil.setStatusBarGradiant(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.accept_activity);
        webView = findViewById(R.id.webView);
        IsRTL.ifSupported(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        webView.setBackgroundColor(Color.TRANSPARENT);
        Intent intent = getIntent();
        String bankTransferTitle = intent.getStringExtra("btTitle");
        bankTransferInfo = intent.getStringExtra("btInfo");
        setTitle(bankTransferTitle);
        setResult();
    }

    private void setResult() {
        String mimeType = "text/html";
        String encoding = "utf-8";
        String htmlText = bankTransferInfo;
        boolean isRTL = Boolean.parseBoolean(getResources().getString(R.string.isRTL));
        String direction = isRTL ? "rtl" : "ltr";
        String text = "<html dir=" + direction + "><head>"
                + "<style type=\"text/css\">@font-face {font-family: MyFont;src: url(\"file:///android_asset/fonts/custom.otf\")}body{font-family: MyFont;color: #ffffff;text-align:justify;line-height:1.2}"
                + "</style></head>"
                + "<body>"
                + htmlText
                + "</body></html>";

        webView.loadDataWithBaseURL(null, text, mimeType, encoding, null);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
