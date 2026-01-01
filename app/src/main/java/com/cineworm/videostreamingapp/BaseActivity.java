package com.cineworm.videostreamingapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.cineworm.util.Events;
import com.cineworm.util.GlobalBus;

import org.greenrobot.eventbus.Subscribe;

public class BaseActivity extends AppCompatActivity {
    boolean isLogoutRemote = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GlobalBus.getBus().register(this);
    }

    @Subscribe
    public void getRemoteLogout(Events.RemoteLogout remoteLogout) {
        isLogoutRemote = remoteLogout.isLogoutRemote();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isLogoutRemote) {
            Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            ActivityCompat.finishAffinity(BaseActivity.this);
            isLogoutRemote = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GlobalBus.getBus().unregister(this);
    }
}
