package com.cineworm.videostreamingapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.FragmentManager;

import com.cineworm.fragment.ExoPlayerFragment;
import com.cineworm.item.ItemPlayer;

public class TrailerExoPlayerActivity extends BaseActivity {
    String channelUrl;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exo_player_trailer);
        WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
        FragmentManager fragmentManager = getSupportFragmentManager();
        Intent intent = getIntent();
        channelUrl = intent.getStringExtra("streamUrl");

        ExoPlayerFragment exoPlayerFragment = ExoPlayerFragment.newInstance(getPlayerData());
        fragmentManager.beginTransaction().replace(R.id.playerSection, exoPlayerFragment).commitAllowingStateLoss();

    }

    private ItemPlayer getPlayerData() {
        ItemPlayer itemPlayer = new ItemPlayer();
        itemPlayer.setDefaultUrl(channelUrl);
        itemPlayer.setTrailer(true);
        itemPlayer.setQuality(false);
        itemPlayer.setSubTitle(false);
        return itemPlayer;
    }
}
