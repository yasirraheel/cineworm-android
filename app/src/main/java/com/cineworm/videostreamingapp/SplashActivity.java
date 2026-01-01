package com.cineworm.videostreamingapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.applovin.sdk.AppLovinMediationProvider;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkInitializationConfiguration;
import com.cineworm.util.API;
import com.cineworm.util.Constant;
import com.cineworm.util.IsRTL;
import com.cineworm.util.NetworkUtils;
import com.cineworm.util.StatusBarUtil;
import com.facebook.ads.AudienceNetworkAds;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.StartAppSDK;
import com.wortise.ads.WortiseSdk;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import cz.msebera.android.httpclient.Header;


public class SplashActivity extends AppCompatActivity {

    MyApplication myApplication;
    private boolean mIsBackButtonPressed;
    private static final int SPLASH_DURATION = 2000;
    boolean isLoginDisable = false;
    
    private ImageView loaderRing;
    private View dot1, dot2, dot3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StatusBarUtil.setStatusBarBlack(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        IsRTL.ifSupported(this);
        
        // Initialize loader views
        loaderRing = findViewById(R.id.loaderRing);
        dot1 = findViewById(R.id.dot1);
        dot2 = findViewById(R.id.dot2);
        dot3 = findViewById(R.id.dot3);
        
        // Start animations
        startLoaderAnimations();
        
        myApplication = MyApplication.getInstance();
        if (NetworkUtils.isConnected(SplashActivity.this)) {
            checkLicense();
        } else {
            Toast.makeText(SplashActivity.this, getString(R.string.conne_msg1), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void startLoaderAnimations() {
        // Rotate the ring
        Animation rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate_loader);
        loaderRing.startAnimation(rotateAnimation);
        
        // Animate dots with delays
        Animation fadeAnimation1 = AnimationUtils.loadAnimation(this, R.anim.fade_pulse);
        Animation fadeAnimation2 = AnimationUtils.loadAnimation(this, R.anim.fade_pulse);
        Animation fadeAnimation3 = AnimationUtils.loadAnimation(this, R.anim.fade_pulse);
        
        fadeAnimation2.setStartOffset(200);
        fadeAnimation3.setStartOffset(400);
        
        dot1.startAnimation(fadeAnimation1);
        dot2.startAnimation(fadeAnimation2);
        dot3.startAnimation(fadeAnimation3);
    }

    private void splashScreen() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!mIsBackButtonPressed) {
                    if (myApplication.getIsIntroduction()) {
                        if (myApplication.getIsLogin() && myApplication.getDeviceLimitReached()) {
                            Intent intent = new Intent(SplashActivity.this, DeviceActivity.class);
                            startActivity(intent);
                            finish();
                        } else if (isLoginDisable && myApplication.getIsLogin()) {
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        } else if (!isLoginDisable && myApplication.getIsLogin()) {
                            myApplication.saveIsLogin(false);
                            Toast.makeText(SplashActivity.this, getString(R.string.user_disable), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                            intent.putExtra("isLogout", true);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        } else {
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        }

                    } else {
                        Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                }
            }

        }, SPLASH_DURATION);
    }

    @Override
    public void onBackPressed() {
        // set the flag to true so the next activity won't start up
        mIsBackButtonPressed = true;
        super.onBackPressed();
    }

    private void checkLicense() {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        if (myApplication.getIsLogin()) {
            jsObj.addProperty("user_id", myApplication.getUserId());
        } else {
            jsObj.addProperty("user_id", "");
        }
        params.put("data", API.toBase64(jsObj.toString()));
        client.post(Constant.APP_DETAIL_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String result = new String(responseBody);
                try {
                    JSONObject mainJson = new JSONObject(result);
                    isLoginDisable = mainJson.getBoolean("user_status");
                    boolean isAdStatus = mainJson.getBoolean("user_ad_status");
                    JSONArray jsonArray = mainJson.getJSONArray(Constant.ARRAY_NAME);
                    JSONObject objJson = jsonArray.getJSONObject(0);
                    if (objJson.has(Constant.STATUS)) {
                        Toast.makeText(SplashActivity.this, getString(R.string.something_went), Toast.LENGTH_SHORT).show();
                    } else {
                        String packageName = objJson.getString("app_package_name");
                        Constant.isAppUpdate = objJson.getBoolean("app_update_hide_show");
                        Constant.isAppUpdateCancel = objJson.getBoolean("app_update_cancel_option");
                        Constant.appUpdateVersion = objJson.getInt("app_update_version_code");
                        Constant.appUpdateUrl = objJson.getString("app_update_link");
                        Constant.appUpdateDesc = objJson.getString("app_update_desc");
                        Constant.isMovieMenu = objJson.getBoolean("menu_movies");
                        Constant.isShowMenu = objJson.getBoolean("menu_shows");
                        Constant.isTvMenu = objJson.getBoolean("menu_livetv");
                        Constant.isSportMenu = objJson.getBoolean("menu_sports");

                        JSONArray jsonAdArray = objJson.getJSONArray("ads_list");
                        JSONObject objJsonAd = jsonAdArray.getJSONObject(0);
                        Constant.adNetworkType = objJsonAd.getString("ad_id");
                        JSONObject objJsonAdInfo = objJsonAd.getJSONObject("ads_info");
                        Constant.appIdOrPublisherId = objJsonAdInfo.getString("publisher_id");
                        if (isAdStatus) {
                            Constant.isBanner = objJsonAdInfo.getString("banner_on_off").equals("1");
                            Constant.isInterstitial = objJsonAdInfo.getString("interstitial_on_off").equals("1");
                            Constant.isPlayerAd = objJson.getString("ad_on_off").equals("on");
                        } else {
                            Constant.isBanner = false;
                            Constant.isInterstitial = false;
                            Constant.isPlayerAd = false;
                        }
                        Constant.bannerId = objJsonAdInfo.getString("banner_id");
                        Constant.interstitialId = objJsonAdInfo.getString("interstitial_id");
                        Constant.interstitialAdCount = objJsonAdInfo.getInt("interstitial_clicks");
                        Constant.playerAdVastUrl = objJson.getString("ad_url");
                        initializeAds();

                        if (packageName.isEmpty() || !packageName.equals(getPackageName())) {
                            invalidDialog();
                        } else {
                            splashScreen();
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            }

        });
    }

    private void invalidDialog() {
        new AlertDialog.Builder(SplashActivity.this).setTitle(getString(R.string.invalid_license)).setMessage(getString(R.string.license_msg)).setCancelable(false).setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        }).setIcon(R.mipmap.ic_launcher).show();
    }

    private void initializeAds() {
        if (Constant.isBanner || Constant.isInterstitial) {
            switch (Constant.adNetworkType) {
                case Constant.admobAd:
                    MobileAds.initialize(this, new OnInitializationCompleteListener() {
                        @Override
                        public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {

                        }
                    });
                    break;
                case Constant.startAppAd:
                    StartAppSDK.init(this, Constant.appIdOrPublisherId, false);
                    StartAppAd.disableSplash();
                    break;
                case Constant.facebookAd:
                    AudienceNetworkAds.initialize(this);
                    break;
                case Constant.appLovinMaxAd:
                    AppLovinSdkInitializationConfiguration initConfig = AppLovinSdkInitializationConfiguration.builder(getString(R.string.applovin_sdk_key), this).setMediationProvider(AppLovinMediationProvider.MAX).setTestDeviceAdvertisingIds(Arrays.asList("9f08fe13-c55d-400b-994c-400ec52cf80f")).build();
                    AppLovinSdk.getInstance(SplashActivity.this).initialize(initConfig, appLovinSdkConfiguration -> {

                    });
                    break;
                case Constant.wortiseAd:
                    WortiseSdk.initialize(SplashActivity.this, Constant.appIdOrPublisherId);
                    break;
            }
        }
    }
}
