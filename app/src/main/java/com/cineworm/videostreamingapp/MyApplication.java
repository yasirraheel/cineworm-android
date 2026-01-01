package com.cineworm.videostreamingapp;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.multidex.MultiDex;

import com.onesignal.OneSignal;
import com.onesignal.notifications.INotificationClickEvent;
import com.onesignal.notifications.INotificationClickListener;

import org.json.JSONException;
import org.json.JSONObject;

import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;

public class MyApplication extends Application implements LifecycleObserver {

    private static MyApplication mInstance;
    public SharedPreferences preferences;
    public String prefName = "VideoStreamingApp";
    public static boolean isInBackground = true;

    public MyApplication() {
        mInstance = this;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        MultiDex.install(this);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);

        ViewPump.init(ViewPump.builder()
                .addInterceptor(new CalligraphyInterceptor(
                        new CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/custom.otf")
                                .setFontAttrId(R.attr.fontPath)
                                .build()))
                .build());

        OneSignal.initWithContext(this, getString(R.string.onesignal_app_id));
        OneSignal.getNotifications().addClickListener(new ExampleNotificationOpenedHandler());
        mInstance = this;
    }

    public static synchronized MyApplication getInstance() {
        return mInstance;
    }

    public void saveIsLogin(boolean flag) {
        preferences = this.getSharedPreferences(prefName, 0);
        Editor editor = preferences.edit();
        editor.putBoolean("IsLoggedIn", flag);
        editor.apply();
        if (!flag) {
            saveLogin("", "", "", "");
        }
    }

    public boolean getIsLogin() {
        preferences = this.getSharedPreferences(prefName, 0);
        return preferences.getBoolean("IsLoggedIn", false);
    }

    public void saveIsRemember(boolean flag) {
        preferences = this.getSharedPreferences(prefName, 0);
        Editor editor = preferences.edit();
        editor.putBoolean("IsLoggedRemember", flag);
        editor.apply();
    }

    public boolean getIsRemember() {
        preferences = this.getSharedPreferences(prefName, 0);
        return preferences.getBoolean("IsLoggedRemember", false);
    }


    public void saveRemember(String email, String password) {
        preferences = this.getSharedPreferences(prefName, 0);
        Editor editor = preferences.edit();
        editor.putString("remember_email", email);
        editor.putString("remember_password", password);
        editor.apply();
    }

    public String getRememberEmail() {
        preferences = this.getSharedPreferences(prefName, 0);
        return preferences.getString("remember_email", "");
    }

    public String getRememberPassword() {
        preferences = this.getSharedPreferences(prefName, 0);
        return preferences.getString("remember_password", "");
    }

    public void saveLogin(String user_id, String user_name, String email, String phone) {
        preferences = this.getSharedPreferences(prefName, 0);
        Editor editor = preferences.edit();
        editor.putString("user_id", user_id);
        editor.putString("user_name", user_name);
        editor.putString("email", email);
        editor.putString("phone", phone);
        editor.apply();
    }

    public String getLoginType() {
        preferences = this.getSharedPreferences(prefName, 0);
        return preferences.getString("login_type", "");
    }

    public void saveLoginType(String type) {
        preferences = this.getSharedPreferences(prefName, 0);
        Editor editor = preferences.edit();
        editor.putString("login_type", type);
        editor.apply();
    }


    public String getUserId() {
        preferences = this.getSharedPreferences(prefName, 0);
        return preferences.getString("user_id", "");
    }

    public String getUserName() {
        preferences = this.getSharedPreferences(prefName, 0);
        return preferences.getString("user_name", "");
    }

    public String getUserEmail() {
        preferences = this.getSharedPreferences(prefName, 0);
        return preferences.getString("email", "");
    }

    public String getUserPhone() {
        preferences = this.getSharedPreferences(prefName, 0);
        return preferences.getString("phone", "");
    }

    public void saveIsNotification(boolean flag) {
        preferences = this.getSharedPreferences(prefName, 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("IsNotification", flag);
        editor.apply();
    }

    public boolean getNotification() {
        preferences = this.getSharedPreferences(prefName, 0);
        return preferences.getBoolean("IsNotification", true);
    }

    public void saveIsIntroduction(boolean flag) {
        preferences = this.getSharedPreferences(prefName, 0);
        Editor editor = preferences.edit();
        editor.putBoolean("IsIntroduction", flag);
        editor.apply();
    }

    public boolean getIsIntroduction() {
        preferences = this.getSharedPreferences(prefName, 0);
        return preferences.getBoolean("IsIntroduction", false);
    }

    public void saveUserSession(String sessionName) {
        preferences = this.getSharedPreferences(prefName, 0);
        Editor editor = preferences.edit();
        editor.putString("user_session", sessionName);
        editor.apply();
    }

    public String getUserSession() {
        preferences = this.getSharedPreferences(prefName, 0);
        return preferences.getString("user_session", "");
    }

    public void saveDeviceLimit(boolean flag) {
        preferences = this.getSharedPreferences(prefName, 0);
        Editor editor = preferences.edit();
        editor.putBoolean("IsDeviceLimit", flag);
        editor.apply();
    }

    public boolean getDeviceLimitReached() {
        preferences = this.getSharedPreferences(prefName, 0);
        return preferences.getBoolean("IsDeviceLimit", false);
    }

    private class ExampleNotificationOpenedHandler implements INotificationClickListener {
        @Override
        public void onClick(INotificationClickEvent result) {
            JSONObject data = result.getNotification().getAdditionalData();
            String isExternalLink, postId, postType;
            if (data != null) {
                try {
                    isExternalLink = data.getString("external_link");
                    postId = data.getString("post_id");
                    postType = data.getString("type");
                    if (!postId.equals("null")) {
                        Class<?> aClass;
                        switch (postType) {
                            case "Movies":
                                aClass = MovieDetailsActivity.class;
                                break;
                            case "Shows":
                                aClass = ShowDetailsActivity.class;
                                break;
                            case "LiveTV":
                                aClass = TVDetailsActivity.class;
                                break;
                            default:
                                aClass = SportDetailsActivity.class;
                                break;
                        }
                        Intent intent = new Intent(MyApplication.this, aClass);
                        intent.putExtra("Id", postId);
                        intent.putExtra("isNotification", true);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    } else {
                        Intent intent;
                        if (!isExternalLink.equals("false")) {
                            intent = new Intent(Intent.ACTION_VIEW,
                                    Uri.parse(isExternalLink));
                        } else {
                            intent = new Intent(MyApplication.this, SplashActivity.class);
                        }
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                } catch (JSONException e) {
                  //  e.printStackTrace();
                    Intent intent = new Intent(MyApplication.this, SplashActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            } else {
                Intent intent = new Intent(MyApplication.this, SplashActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onMoveToForeground() {
        // app moved to foreground
        isInBackground = false;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onMoveToBackground() {
        // app moved to background
        isInBackground = true;
    }
}
