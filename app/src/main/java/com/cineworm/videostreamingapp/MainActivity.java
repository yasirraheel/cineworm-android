package com.cineworm.videostreamingapp;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.cineworm.fragment.HomeFragment;
import com.cineworm.fragment.MovieFragment;
import com.cineworm.fragment.MyAccountFragment;
import com.cineworm.fragment.SettingFragment;
import com.cineworm.fragment.ShowFragment;
import com.cineworm.fragment.SportFragment;
import com.cineworm.fragment.TVFragment;
import com.cineworm.fragment.WatchListFragment;
import com.cineworm.item.ItemBottomBar;
import com.cineworm.util.BannerAds;
import com.cineworm.util.Constant;
import com.cineworm.util.GDPRChecker;
import com.cineworm.util.IsRTL;
import com.cineworm.util.NotificationTiramisu;
import com.cineworm.util.StatusBarUtil;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class MainActivity extends BaseActivity {

    Toolbar toolbar;
    private FragmentManager fragmentManager;
    boolean doubleBackToExitPressedOnce = false;
    MyApplication myApplication;
    int versionCode;
    ArrayList<ItemBottomBar> bottomBarList;
    LinearLayout llHome, llWatchlist, llAccount, llSettings, llMenu;
    RelativeLayout rootLayout;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StatusBarUtil.setStatusBarGradiant(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        IsRTL.ifSupported(this);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        rootLayout = findViewById(R.id.rootLayout);
        llHome = findViewById(R.id.llHome);
        llWatchlist = findViewById(R.id.llWatchlist);
        llAccount = findViewById(R.id.llAccount);
        llSettings = findViewById(R.id.llSetting);
        llMenu = findViewById(R.id.llMenu);
        fragmentManager = getSupportFragmentManager();
        myApplication = MyApplication.getInstance();
        bottomBarList = ItemBottomBar.listOfBottomBarItem(rootLayout);

        try {
            versionCode = getPackageManager()
                    .getPackageInfo(getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (Constant.adNetworkType != null && Constant.adNetworkType.equals(Constant.admobAd)) {
            new GDPRChecker()
                    .withContext(MainActivity.this)
                    .check();
        }

        LinearLayout mAdViewLayout = findViewById(R.id.adView);
        BannerAds.showBannerAds(this, mAdViewLayout);

        HomeFragment homeFragment = new HomeFragment();
        loadFrag(homeFragment, getString(R.string.menu_home), fragmentManager);

        if (versionCode != Constant.appUpdateVersion && Constant.isAppUpdate) {
            newUpdateDialog();
        }

        setBottomNavBar();
        selectBottomBar(0);
        NotificationTiramisu.takePermission(this);
    }

    public void loadFrag(Fragment f1, String name, FragmentManager fm) {
        for (int i = 0; i < fm.getBackStackEntryCount(); ++i) {
            fm.popBackStack();
        }
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.Container, f1, name);
        ft.commit();
        setToolbarTitle(name);
    }

    public void setToolbarTitle(String Title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(Title);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);
        final MenuItem searchMenuItem = menu.findItem(R.id.search);
        final SearchView searchView = (SearchView) searchMenuItem.getActionView();

        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            // TODO Auto-generated method stub
            if (!hasFocus) {
                searchMenuItem.collapseActionView();
                searchView.setQuery("", false);
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String arg0) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(MainActivity.this, SearchHorizontalActivity.class);
                intent.putExtra("search", arg0);
                startActivity(intent);
                searchView.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String arg0) {
                // TODO Auto-generated method stub
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        if (fragmentManager.getBackStackEntryCount() != 0) {
            String tag = fragmentManager.getFragments().get(fragmentManager.getBackStackEntryCount() - 1).getTag();
            setToolbarTitle(tag);
            super.onBackPressed();
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, getString(R.string.back_key), Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Fragment fragment = fragmentManager.findFragmentByTag(getString(R.string.menu_profile));
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void newUpdateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(getString(R.string.app_update_title));
        builder.setCancelable(false);
        builder.setMessage(Constant.appUpdateDesc);
        builder.setPositiveButton(getString(R.string.app_update_btn), (dialog, which) -> startActivity(new Intent(
                Intent.ACTION_VIEW,
                Uri.parse(Constant.appUpdateUrl))));
        if (Constant.isAppUpdateCancel) {
            builder.setNegativeButton(getString(R.string.app_cancel_btn), (dialog, which) -> {

            });
        }
        builder.setIcon(R.mipmap.ic_launcher);
        builder.show();
    }

    private void setBottomNavBar() {
        llHome.setOnClickListener(view -> {
            selectBottomBar(0);
            HomeFragment homeFragment = new HomeFragment();
            loadFrag(homeFragment, getString(R.string.menu_home), fragmentManager);
        });

        llWatchlist.setOnClickListener(view -> {
            selectBottomBar(1);
            WatchListFragment watchListFragment = new WatchListFragment();
            loadFrag(watchListFragment, getString(R.string.my_watch_list), fragmentManager);
        });

        llAccount.setOnClickListener(view -> {
            selectBottomBar(2);
            MyAccountFragment myAccountFragment = new MyAccountFragment();
            loadFrag(myAccountFragment, getString(R.string.account), fragmentManager);
        });

        llSettings.setOnClickListener(view -> {
            selectBottomBar(3);
            SettingFragment settingFragment = new SettingFragment();
            loadFrag(settingFragment, getString(R.string.menu_setting), fragmentManager);
        });

        llMenu.setOnClickListener(view -> openMenu());
    }


    public void resetBottomBar() {
        selectBottomBar(-1);
    }

    private void selectBottomBar(int posClick) {
        for (int i = 0; i < bottomBarList.size(); i++) {
            ItemBottomBar bottomBar = bottomBarList.get(i);
            if (posClick == i) {
                bottomBar.getTextView().setTextColor(getResources().getColor(R.color.bottom_hover_item));
                bottomBar.getImageView().setColorFilter(getResources().getColor(R.color.bottom_hover_item), PorterDuff.Mode.SRC_IN);
            } else {
                bottomBar.getTextView().setTextColor(getResources().getColor(R.color.bottom_text));
                bottomBar.getImageView().setColorFilter(getResources().getColor(R.color.bottom_text), PorterDuff.Mode.SRC_IN);
            }
        }
    }

    private void openMenu() {
        BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(MainActivity.this, R.style.BottomSheetDialog);
        View sheetView = getLayoutInflater().inflate(R.layout.layout_menu_sheet, rootLayout, false);
        mBottomSheetDialog.setContentView(sheetView);
        RelativeLayout rlMovie = sheetView.findViewById(R.id.rlMovie);
        RelativeLayout rlShow = sheetView.findViewById(R.id.rlShow);
        RelativeLayout rlSport = sheetView.findViewById(R.id.rlSport);
        RelativeLayout rlTV = sheetView.findViewById(R.id.rlTV);
        ImageView ivClose = sheetView.findViewById(R.id.ivMenuClose);

        rlMovie.setVisibility(Constant.isMovieMenu ? View.VISIBLE : View.GONE);
        rlShow.setVisibility(Constant.isShowMenu ? View.VISIBLE : View.GONE);
        rlSport.setVisibility(Constant.isSportMenu ? View.VISIBLE : View.GONE);
        rlTV.setVisibility(Constant.isTvMenu ? View.VISIBLE : View.GONE);

        rlMovie.setOnClickListener(view -> {
            resetBottomBar();
            MovieFragment movieFragment = new MovieFragment();
            loadFrag(movieFragment, getString(R.string.menu_movie), fragmentManager);
            mBottomSheetDialog.dismiss();
        });

        rlShow.setOnClickListener(view -> {
            resetBottomBar();
            ShowFragment showFragment = new ShowFragment();
            loadFrag(showFragment, getString(R.string.menu_tv_show), fragmentManager);
            mBottomSheetDialog.dismiss();
        });

        rlSport.setOnClickListener(view -> {
            resetBottomBar();
            SportFragment sportFragment = new SportFragment();
            loadFrag(sportFragment, getString(R.string.menu_sport), fragmentManager);
            mBottomSheetDialog.dismiss();
        });

        rlTV.setOnClickListener(view -> {
            resetBottomBar();
            TVFragment tvFragment = new TVFragment();
            loadFrag(tvFragment, getString(R.string.menu_tv), fragmentManager);
            mBottomSheetDialog.dismiss();
        });

        ivClose.setOnClickListener(view -> mBottomSheetDialog.dismiss());
        mBottomSheetDialog.show();

    }
}
