package com.cineworm.util;

import static com.cineworm.videostreamingapp.MyApplication.isInBackground;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import com.cineworm.videostreamingapp.LogoutRemoteActivity;
import com.cineworm.videostreamingapp.MyApplication;
import com.cineworm.videostreamingapp.R;
import com.onesignal.OneSignal;
import com.onesignal.notifications.INotificationReceivedEvent;
import com.onesignal.notifications.INotificationServiceExtension;

import org.json.JSONException;
import org.json.JSONObject;

@Keep
public class NotificationReceivedHandler implements INotificationServiceExtension {

    @Override
    public void onNotificationReceived(@NonNull INotificationReceivedEvent iNotificationReceivedEvent) {
        JSONObject data = iNotificationReceivedEvent.getNotification().getAdditionalData();
        Context context = iNotificationReceivedEvent.getContext();
        if (data != null) {
            try {
                if (data.has("logout_remote")) {
                    boolean isLogout = data.getString("logout_remote").equals("1");
                    if (isLogout) {
                        iNotificationReceivedEvent.preventDefault();
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, context.getString(R.string.remote_logout), Toast.LENGTH_SHORT).show();
                                MyApplication.getInstance().saveIsLogin(false);
                                MyApplication.getInstance().saveDeviceLimit(false);
                                OneSignal.getUser().addTag("user_session", "");
                                if (isInBackground) {
                                    Events.RemoteLogout fullScreen = new Events.RemoteLogout();
                                    fullScreen.setLogoutRemote(true);
                                    GlobalBus.getBus().post(fullScreen);
                                } else {
                                    Intent intent = new Intent(context, LogoutRemoteActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    context.startActivity(intent);
                                }
                            }
                        });
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}