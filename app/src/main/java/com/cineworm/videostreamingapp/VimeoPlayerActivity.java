package com.cineworm.videostreamingapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;

public class VimeoPlayerActivity extends BaseActivity {
    String videoId;
    WebView webView;
    ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_embedded_player);

        Intent intent = getIntent();
        videoId = intent.getStringExtra("videoId");
        webView = findViewById(R.id.video);
        progressBar = findViewById(R.id.load);
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.setFocusableInTouchMode(false);
        webView.setFocusable(false);
        webView.getSettings().setDefaultTextEncodingName("UTF-8");
        webView.getSettings().setJavaScriptEnabled(true);

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

        webView.loadUrl("http://player.vimeo.com/video/" + videoId + "?player_id=player&autoplay=1&title=0&byline=0&portrait=0&api=1&maxheight=480&maxwidth=800");

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        webView.loadUrl("");
    }
}
