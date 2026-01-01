package com.cineworm.videostreamingapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.FragmentManager;
import androidx.mediarouter.app.MediaRouteButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cineworm.adapter.ActorDirectorAdapter;
import com.cineworm.adapter.EpisodeAdapter;
import com.cineworm.adapter.HomeShowAdapter;
import com.cineworm.adapter.InfoAdapter;
import com.cineworm.adapter.SeasonRvAdapter;
import com.cineworm.cast.Casty;
import com.cineworm.cast.MediaData;
import com.cineworm.fragment.ChromecastScreenFragment;
import com.cineworm.fragment.EmbeddedImageFragment;
import com.cineworm.fragment.ExoPlayerFragment;
import com.cineworm.fragment.PlayRippleFragment;
import com.cineworm.fragment.PremiumContentFragment;
import com.cineworm.item.ItemActor;
import com.cineworm.item.ItemEpisode;
import com.cineworm.item.ItemPlayer;
import com.cineworm.item.ItemSeason;
import com.cineworm.item.ItemShow;
import com.cineworm.item.ItemSubTitle;
import com.cineworm.util.API;
import com.cineworm.util.BannerAds;
import com.cineworm.util.Constant;
import com.cineworm.util.EpisodeNextPrevListener;
import com.cineworm.util.Events;
import com.cineworm.util.GlobalBus;
import com.cineworm.util.IsRTL;
import com.cineworm.util.LinearLayoutPagerManager;
import com.cineworm.util.NetworkUtils;
import com.cineworm.util.PlayerUtil;
import com.cineworm.util.RvOnClickListener;
import com.cineworm.util.ShareUtils;
import com.cineworm.util.StatusBarUtil;
import com.cineworm.util.WatchListClickListener;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class ShowDetailsActivity extends BaseActivity {

    ProgressBar mProgressBar, mProgressBarEpisode;
    LinearLayout lyt_not_found;
    RelativeLayout lytParent;
    WebView webView;
    TextView textTitle, textNoEpisode, textNoSeason;
    RecyclerView rvInfo, rvRelated, rvEpisode, rvActor, rvDirector, rvSeason;
    ItemShow itemShow;
    ArrayList<ItemShow> mListItemRelated;
    ArrayList<ItemSeason> mListSeason;
    ArrayList<ItemEpisode> mListItemEpisode;
    ArrayList<ItemActor> mListItemActor, mListItemDirector;
    ArrayList<String> mListInfo, showGenreList;
    HomeShowAdapter homeShowAdapter;
    ActorDirectorAdapter actorAdapter, directorAdapter;
    String Id;
    LinearLayout lytRelated, lytActor, lytDirector, lytShare;
    MyApplication myApplication;
    NestedScrollView nestedScrollView;
    Toolbar toolbar;
    EpisodeAdapter episodeAdapter;
    private FragmentManager fragmentManager;
    private int playerHeight;
    FrameLayout frameLayout;
    boolean isFullScreen = false;
    boolean isFromNotification = false, isUpcoming = false, isEpisodeRedirect = false;
    LinearLayout mAdViewLayout;
    boolean isPurchased = false;
    private int selectedEpisode = 0, selectedSeason = -1;
    private Casty casty;
    private MaterialButton btnWatchList, btnDownload, btnTrailer;
    SeasonRvAdapter seasonAdapter;
    RelativeLayout lytEpisode;
    InfoAdapter infoAdapter;
    ImageView imgFacebook, imgTwitter, imgWhatsApp;
    MediaRouteButton mediaRouteButton;
    WindowInsetsControllerCompat windowInsetsController;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        StatusBarUtil.setStatusBarBlack(this);
        super.onCreate(savedInstanceState);
    //    getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_show_details);
        windowInsetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        IsRTL.ifSupported(this);
        //    GlobalBus.getBus().register(this);
        mAdViewLayout = findViewById(R.id.adView);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        myApplication = MyApplication.getInstance();
        fragmentManager = getSupportFragmentManager();
        Intent intent = getIntent();
        Id = intent.getStringExtra("Id");
        if (intent.hasExtra("isNotification")) {
            isFromNotification = true;
        }
        if (intent.hasExtra("episodeRedirect")) {
            isEpisodeRedirect = true;
        }
        
        try {
            casty = Casty.create(this).withMiniController();
            mediaRouteButton = findViewById(R.id.media_route_button);
            casty.setUpMediaRouteButton(mediaRouteButton);
        } catch (Exception e) {
            Log.e("ShowDetails", "Failed to initialize Cast: " + e.getMessage());
            e.printStackTrace();
            mediaRouteButton = findViewById(R.id.media_route_button);
            if (mediaRouteButton != null) {
                mediaRouteButton.setVisibility(View.GONE);
            }
        }

        frameLayout = findViewById(R.id.playerSection);
        int columnWidth = NetworkUtils.getScreenWidth(this);
        frameLayout.setLayoutParams(new RelativeLayout.LayoutParams(columnWidth, (columnWidth / 2) + 100));
        playerHeight = frameLayout.getLayoutParams().height;

        BannerAds.showBannerAds(this, mAdViewLayout);

        mListItemRelated = new ArrayList<>();
        mListSeason = new ArrayList<>();
        mListItemEpisode = new ArrayList<>();
        itemShow = new ItemShow();
        mListItemActor = new ArrayList<>();
        mListItemDirector = new ArrayList<>();
        mListInfo = new ArrayList<>();
        showGenreList = new ArrayList<>();
        lytActor = findViewById(R.id.lytActors);
        lytDirector = findViewById(R.id.lytDirector);
        rvActor = findViewById(R.id.rv_actor);
        rvDirector = findViewById(R.id.rv_director);
        lytRelated = findViewById(R.id.lytRelated);
        mProgressBar = findViewById(R.id.progressBar1);
        lyt_not_found = findViewById(R.id.lyt_not_found);
        lytParent = findViewById(R.id.lytParent);
        nestedScrollView = findViewById(R.id.nestedScrollView);
        webView = findViewById(R.id.webView);
        textTitle = findViewById(R.id.textTitle);
        rvRelated = findViewById(R.id.rv_related);
        rvEpisode = findViewById(R.id.rv_episode);
        rvInfo = findViewById(R.id.rv_info);
        rvSeason = findViewById(R.id.rv_season);
        textNoSeason = findViewById(R.id.textNoSeason);
        mProgressBarEpisode = findViewById(R.id.progressBar);
        textNoEpisode = findViewById(R.id.textNoEpisode);
        btnWatchList = findViewById(R.id.btnWatchList);
        btnDownload = findViewById(R.id.btnDownload);
        btnTrailer = findViewById(R.id.btnTrailer);
        lytEpisode = findViewById(R.id.lytEpisode);
        lytShare = findViewById(R.id.lytShare);
        imgFacebook = findViewById(R.id.imgFacebook);
        imgTwitter = findViewById(R.id.imgTwitter);
        imgWhatsApp = findViewById(R.id.imgWhatsApp);

        rvRelated.setHasFixedSize(true);
        rvRelated.setLayoutManager(new LinearLayoutPagerManager(ShowDetailsActivity.this, LinearLayoutManager.HORIZONTAL, false, 2));
        rvRelated.setFocusable(false);
        rvRelated.setNestedScrollingEnabled(false);

        rvEpisode.setHasFixedSize(true);
        rvEpisode.setLayoutManager(new LinearLayoutPagerManager(ShowDetailsActivity.this, LinearLayoutManager.HORIZONTAL, false, 2));
        rvEpisode.setFocusable(false);
        rvEpisode.setNestedScrollingEnabled(false);

        setRecyclerViewProperty(rvActor);
        setRecyclerViewProperty(rvDirector);
        setRecyclerViewProperty(rvInfo);
        setRecyclerViewProperty(rvSeason);

        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.getSettings().setJavaScriptEnabled(true);
        if (NetworkUtils.isConnected(ShowDetailsActivity.this)) {
            getDetails();
        } else {
            showToast(getString(R.string.conne_msg1));
        }

    }

    private void setRecyclerViewProperty(RecyclerView recyclerView) {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(ShowDetailsActivity.this, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setFocusable(false);
        recyclerView.setNestedScrollingEnabled(false);
    }

    private void getDetails() {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        jsObj.addProperty("show_id", Id);
        params.put("data", API.toBase64(jsObj.toString()));
        client.post(Constant.SHOW_DETAILS_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                mProgressBar.setVisibility(View.VISIBLE);
                lytParent.setVisibility(View.GONE);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                mProgressBar.setVisibility(View.GONE);
                lytParent.setVisibility(View.VISIBLE);

                String result = new String(responseBody);
                try {
                    JSONObject mainJson = new JSONObject(result);
                    JSONObject objJson = mainJson.getJSONObject(Constant.ARRAY_NAME);
                    if (objJson.length() > 0) {
                        if (objJson.has(Constant.STATUS)) {
                            lyt_not_found.setVisibility(View.VISIBLE);
                        } else {
                            isUpcoming = objJson.getBoolean(Constant.UPCOMING_STATUS);
                            itemShow.setShowId(objJson.getString(Constant.SHOW_ID));
                            itemShow.setShowName(objJson.getString(Constant.SHOW_NAME));
                            itemShow.setShowDescription(objJson.getString(Constant.SHOW_DESC));
                            itemShow.setShowImage(objJson.getString(Constant.SHOW_POSTER));
                            itemShow.setShowLanguage(objJson.getString(Constant.SHOW_LANGUAGE));
                            itemShow.setShowRating(objJson.getString(Constant.IMDB_RATING));
                            itemShow.setShowContentRating(objJson.getString(Constant.MOVIE_CONTENT_RATING));

                            JSONArray jsonArrayChild = objJson.getJSONArray(Constant.RELATED_SHOW_ARRAY_NAME);
                            if (jsonArrayChild.length() != 0) {
                                for (int j = 0; j < jsonArrayChild.length(); j++) {
                                    JSONObject objChild = jsonArrayChild.getJSONObject(j);
                                    ItemShow item = new ItemShow();
                                    item.setShowId(objChild.getString(Constant.SHOW_ID));
                                    item.setShowName(objChild.getString(Constant.SHOW_TITLE));
                                    item.setShowImage(objChild.getString(Constant.SHOW_POSTER));
                                    item.setPremium(objChild.getString(Constant.SHOW_ACCESS).equals("Paid"));
                                    mListItemRelated.add(item);
                                }
                            }


                            JSONArray jsonArrayGenre = objJson.getJSONArray(Constant.GENRE_LIST);
                            if (jsonArrayGenre.length() != 0) {
                                for (int k = 0; k < jsonArrayGenre.length(); k++) {
                                    JSONObject objChild = jsonArrayGenre.getJSONObject(k);
                                    showGenreList.add(objChild.getString(Constant.GENRE_NAME));
                                }
                            }

                            JSONArray jsonArraySeason = objJson.getJSONArray(Constant.SEASON_ARRAY_NAME);
                            if (jsonArraySeason.length() != 0) {
                                for (int j = 0; j < jsonArraySeason.length(); j++) {
                                    JSONObject objSeason = jsonArraySeason.getJSONObject(j);
                                    ItemSeason item = new ItemSeason();
                                    item.setSeasonId(objSeason.getString(Constant.SEASON_ID));
                                    item.setSeasonName(objSeason.getString(Constant.SEASON_NAME));
                                    item.setSeasonPoster(objSeason.getString(Constant.SEASON_IMAGE));
                                    item.setSeasonTrailer(objSeason.getString(Constant.SEASON_TRAILER).equals("null") ? "" : objSeason.getString(Constant.SEASON_TRAILER));
                                    mListSeason.add(item);
                                }
                            }

                            JSONArray jsonArrayActor = objJson.getJSONArray(Constant.ACTOR_ARRAY);
                            if (jsonArrayActor.length() != 0) {
                                for (int j = 0; j < jsonArrayActor.length(); j++) {
                                    JSONObject objChild = jsonArrayActor.getJSONObject(j);
                                    ItemActor item = new ItemActor();
                                    item.setActorId(objChild.getString(Constant.ACTOR_ID));
                                    item.setActorName(objChild.getString(Constant.ACTOR_NAME));
                                    item.setActorImage(objChild.getString(Constant.ACTOR_IMAGE));
                                    mListItemActor.add(item);
                                }
                            }

                            JSONArray jsonArrayDirector = objJson.getJSONArray(Constant.DIRECTOR_ARRAY);
                            if (jsonArrayDirector.length() != 0) {
                                for (int j = 0; j < jsonArrayDirector.length(); j++) {
                                    JSONObject objChild = jsonArrayDirector.getJSONObject(j);
                                    ItemActor item = new ItemActor();
                                    item.setActorId(objChild.getString(Constant.ACTOR_ID));
                                    item.setActorName(objChild.getString(Constant.ACTOR_NAME));
                                    item.setActorImage(objChild.getString(Constant.ACTOR_IMAGE));
                                    mListItemDirector.add(item);
                                }
                            }

                        }
                        displayData();

                    } else {
                        mProgressBar.setVisibility(View.GONE);
                        lytParent.setVisibility(View.GONE);
                        lyt_not_found.setVisibility(View.VISIBLE);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    mProgressBar.setVisibility(View.GONE);
                    lytParent.setVisibility(View.GONE);
                    lyt_not_found.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                mProgressBar.setVisibility(View.GONE);
                lytParent.setVisibility(View.GONE);
                lyt_not_found.setVisibility(View.VISIBLE);
            }
        });
    }

    private void displayData() {
        casty.setOnConnectChangeListener(new Casty.OnConnectChangeListener() {
            @Override
            public void onConnected() {
                initCastPlayer(selectedEpisode);
            }

            @Override
            public void onDisconnected() {
                if (!mListItemEpisode.isEmpty()) {
                    playEpisode(selectedEpisode);
                } else {
                    setImageIfSeasonAndEpisodeNone(itemShow.getShowImage());
                }

            }
        });

        if (!mListSeason.isEmpty()) {
            seasonAdapter = new SeasonRvAdapter(ShowDetailsActivity.this, mListSeason);
            rvSeason.setAdapter(seasonAdapter);
            changeSeason(isEpisodeRedirect ? getEpSeasonPos() : 0);
            seasonAdapter.setOnItemClickListener(new RvOnClickListener() {
                @Override
                public void onItemClick(int position) {
                    if (isEpisodeRedirect) {
                        isEpisodeRedirect = false;
                    }
                    changeSeason(position);
                }
            });
        } else {
            setShowInfoText();
            setImageIfSeasonAndEpisodeNone(itemShow.getShowImage());
            lytEpisode.setVisibility(View.GONE);
            textNoSeason.setVisibility(View.VISIBLE);
            rvSeason.setVisibility(View.GONE);
            btnWatchList.setVisibility(View.GONE);
            btnDownload.setVisibility(View.GONE);
            btnTrailer.setVisibility(View.GONE);
            lytShare.setVisibility(View.GONE);
        }

        if (!mListItemRelated.isEmpty()) {
            homeShowAdapter = new HomeShowAdapter(ShowDetailsActivity.this, mListItemRelated);
            rvRelated.setAdapter(homeShowAdapter);

            homeShowAdapter.setOnItemClickListener(new RvOnClickListener() {
                @Override
                public void onItemClick(int position) {
                    String showId = mListItemRelated.get(position).getShowId();
                    Intent intent = new Intent(ShowDetailsActivity.this, ShowDetailsActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("Id", showId);
                    startActivity(intent);
                }
            });

        } else {
            lytRelated.setVisibility(View.GONE);
        }

        if (!mListItemActor.isEmpty()) {
            actorAdapter = new ActorDirectorAdapter(ShowDetailsActivity.this, mListItemActor);
            rvActor.setAdapter(actorAdapter);

            actorAdapter.setOnItemClickListener(new RvOnClickListener() {
                @Override
                public void onItemClick(int position) {
                    String adId = mListItemActor.get(position).getActorId();
                    String adName = mListItemActor.get(position).getActorName();
                    Intent intent = new Intent(ShowDetailsActivity.this, ActorDirectorDetailActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("adId", adId);
                    intent.putExtra("adName", adName);
                    intent.putExtra("isActor", true);
                    startActivity(intent);
                }
            });

        } else {
            lytActor.setVisibility(View.GONE);
        }

        if (!mListItemDirector.isEmpty()) {
            directorAdapter = new ActorDirectorAdapter(ShowDetailsActivity.this, mListItemDirector);
            rvDirector.setAdapter(directorAdapter);

            directorAdapter.setOnItemClickListener(new RvOnClickListener() {
                @Override
                public void onItemClick(int position) {
                    String adId = mListItemDirector.get(position).getActorId();
                    String adName = mListItemDirector.get(position).getActorName();
                    Intent intent = new Intent(ShowDetailsActivity.this, ActorDirectorDetailActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("adId", adId);
                    intent.putExtra("adName", adName);
                    intent.putExtra("isActor", false);
                    startActivity(intent);
                }
            });

        } else {
            lytDirector.setVisibility(View.GONE);
        }

        btnWatchList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickOnWatchList(selectedEpisode);
            }
        });

        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ItemEpisode itemEpisode = mListItemEpisode.get(selectedEpisode);
                if (itemEpisode.getDownloadUrl().isEmpty()) {
                    Toast.makeText(ShowDetailsActivity.this, getString(R.string.download_not_found), Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(itemEpisode.getDownloadUrl())));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        Toast.makeText(ShowDetailsActivity.this, getString(R.string.invalid_download), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        imgFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                episodeShare(1);
            }
        });

        imgTwitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                episodeShare(2);
            }
        });

        imgWhatsApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                episodeShare(3);
            }
        });
    }

    private void setShowInfoText() {
        setTitle("");
        mListInfo.clear();
        textTitle.setText(itemShow.getShowName());
        String mimeType = "text/html";
        String encoding = "utf-8";
        String htmlText = itemShow.getShowDescription();

        String text = "<html><head>" + "<style type=\"text/css\">@font-face {font-family: MyFont;src: url(\"file:///android_asset/fonts/custom.otf\")}body{font-family: MyFont;color: #9c9c9c;font-size:14px;margin-left:0px;line-height:1.3}" + "</style></head>" + "<body>" + htmlText + "</body></html>";

        webView.loadDataWithBaseURL(null, text, mimeType, encoding, null);

        mListInfo.add(itemShow.getShowLanguage());
        if (!itemShow.getShowRating().isEmpty() && !itemShow.getShowRating().equals("0")) {
            mListInfo.add(getString(R.string.imdb, itemShow.getShowRating()));
        }
        if (!itemShow.getShowContentRating().isEmpty()) {
            mListInfo.add(getString(R.string.content_rating, itemShow.getShowContentRating()));
        }
        mListInfo.addAll(showGenreList);
        infoAdapter = new InfoAdapter(mListInfo);
        rvInfo.setAdapter(infoAdapter);
    }

    private void setEpisodeInfoText(ItemEpisode itemEpisode) {
        mListInfo.clear();
        textTitle.setText(itemEpisode.getEpisodeName());
        String mimeType = "text/html";
        String encoding = "utf-8";
        String htmlText = itemEpisode.getEpisodeDescription();

        String text = "<html><head>" + "<style type=\"text/css\">@font-face {font-family: MyFont;src: url(\"file:///android_asset/fonts/custom.otf\")}body{font-family: MyFont;color: #9c9c9c;font-size:14px;margin-left:0px;line-height:1.3}" + "</style></head>" + "<body>" + htmlText + "</body></html>";

        webView.loadDataWithBaseURL(null, text, mimeType, encoding, null);

        mListInfo.add(itemEpisode.getEpisodeDate());
        if (!itemEpisode.getEpisodeDuration().isEmpty() && !itemEpisode.getEpisodeDuration().equals("null")) {
            mListInfo.add(itemEpisode.getEpisodeDuration());
        }
        if (!itemEpisode.getEpisodeRating().isEmpty() && !itemEpisode.getEpisodeRating().equals("0")) {
            mListInfo.add(getString(R.string.imdb, itemEpisode.getEpisodeRating()));
        }
        mListInfo.add(itemShow.getShowLanguage());
        mListInfo.addAll(showGenreList);
        infoAdapter = new InfoAdapter(mListInfo);
        rvInfo.setAdapter(infoAdapter);
        btnWatchList.setVisibility(View.VISIBLE);
        btnWatchList.setIconResource(itemEpisode.isWatchList() ? R.drawable.ic_watch_list_remove : R.drawable.ic_watch_list_add);
        // btnWatchList.setText(itemEpisode.isWatchList() ? getString(R.string.remove_from_watch_list) : getString(R.string.add_to_watch_list));

        if (itemEpisode.isDownload()) {
            if (itemEpisode.isPremium()) {
                if (isPurchased) {
                    btnDownload.setVisibility(View.VISIBLE);
                } else {
                    btnDownload.setVisibility(View.GONE);
                }
            } else {
                btnDownload.setVisibility(View.VISIBLE);
            }
        } else {
            btnDownload.setVisibility(View.GONE);
        }
        lytShare.setVisibility(View.VISIBLE);
    }

    private void episodeShare(int type) {
        ItemEpisode itemEpisode = mListItemEpisode.get(selectedEpisode);
        if (type == 1) {
            ShareUtils.shareFacebook(ShowDetailsActivity.this, itemEpisode.getEpisodeName(), itemEpisode.getEpisodeShareLink());
        } else if (type == 2) {
            ShareUtils.shareTwitter(ShowDetailsActivity.this, itemEpisode.getEpisodeName(), itemEpisode.getEpisodeShareLink(), "", "");
        } else if (type == 3) {
            ShareUtils.shareWhatsapp(ShowDetailsActivity.this, itemEpisode.getEpisodeName(), itemEpisode.getEpisodeShareLink());
        }
    }

    public void showToast(String msg) {
        Toast.makeText(ShowDetailsActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

    private void changeSeason(int seasonId) {
        seasonAdapter.select(seasonId);
        ItemSeason itemSeason = mListSeason.get(seasonId);
        if (selectedSeason != seasonId) {
            selectedEpisode = 0;
            selectedSeason = seasonId;
            mListItemEpisode.clear();
            if (NetworkUtils.isConnected(ShowDetailsActivity.this)) {
                textNoEpisode.setVisibility(View.GONE);
                getEpisode(itemSeason.getSeasonId());
            } else {
                showToast(getString(R.string.conne_msg1));
            }
            setShowInfoText(); // when change season set info of show
            btnTrailer.setVisibility(View.VISIBLE);
            initTrailerPlayer(itemSeason.getSeasonTrailer());
            //when season changes by default select comes so
            btnWatchList.setVisibility(View.GONE);
            btnDownload.setVisibility(View.GONE);
            lytShare.setVisibility(View.GONE);
        }
    }

    private void getEpisode(String seasonId) {

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        jsObj.addProperty("season_id", seasonId);
        jsObj.addProperty("user_id", myApplication.getIsLogin() ? myApplication.getUserId() : "");
        params.put("data", API.toBase64(jsObj.toString()));
        client.post(Constant.EPISODE_LIST_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                mProgressBarEpisode.setVisibility(View.VISIBLE);
                rvEpisode.setVisibility(View.GONE);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                mProgressBarEpisode.setVisibility(View.GONE);
                rvEpisode.setVisibility(View.VISIBLE);

                String result = new String(responseBody);
                try {
                    JSONObject mainJson = new JSONObject(result);
                    isPurchased = mainJson.getBoolean(Constant.USER_PLAN_STATUS);
                    JSONArray jsonArray = mainJson.getJSONArray(Constant.ARRAY_NAME);
                    if (jsonArray.length() > 0) {
                        textNoEpisode.setVisibility(View.GONE);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject objJson = jsonArray.getJSONObject(i);
                            ItemEpisode itemEpisode = new ItemEpisode();
                            itemEpisode.setEpisodeId(objJson.getString(Constant.EPISODE_ID));
                            itemEpisode.setEpisodeName(objJson.getString(Constant.EPISODE_TITLE));
                            itemEpisode.setEpisodeImage(objJson.getString(Constant.EPISODE_IMAGE));
                            itemEpisode.setEpisodeUrl(objJson.getString(Constant.EPISODE_URL));
                            itemEpisode.setEpisodeType(objJson.getString(Constant.EPISODE_TYPE));
                            itemEpisode.setEpisodeDate(objJson.getString(Constant.EPISODE_DATE));
                            itemEpisode.setEpisodeDuration(objJson.getString(Constant.EPISODE_DURATION));
                            itemEpisode.setPremium(objJson.getString(Constant.EPISODE_ACCESS).equals("Paid"));
                            itemEpisode.setDownload(objJson.getBoolean(Constant.DOWNLOAD_ENABLE));
                            itemEpisode.setDownloadUrl(objJson.getString(Constant.DOWNLOAD_URL));
                            itemEpisode.setEpisodeDescription(objJson.getString(Constant.EPISODE_DESC));
                            itemEpisode.setEpisodeShareLink(objJson.getString(Constant.MOVIE_SHARE_LINK));
                            itemEpisode.setEpisodeView(objJson.getString(Constant.MOVIE_VIEW));
                            itemEpisode.setEpisodeRating(objJson.getString(Constant.IMDB_RATING));

                            itemEpisode.setQuality(objJson.getBoolean(Constant.IS_QUALITY));
                            itemEpisode.setSubTitle(objJson.getBoolean(Constant.IS_SUBTITLE));
                            itemEpisode.setQuality480(objJson.getString(Constant.QUALITY_480));
                            itemEpisode.setQuality720(objJson.getString(Constant.QUALITY_720));
                            itemEpisode.setQuality1080(objJson.getString(Constant.QUALITY_1080));

                            itemEpisode.setSubTitleLanguage1(objJson.getString(Constant.SUBTITLE_LANGUAGE_1));
                            itemEpisode.setSubTitleUrl1(objJson.getString(Constant.SUBTITLE_URL_1));
                            itemEpisode.setSubTitleLanguage2(objJson.getString(Constant.SUBTITLE_LANGUAGE_2));
                            itemEpisode.setSubTitleUrl2(objJson.getString(Constant.SUBTITLE_URL_2));
                            itemEpisode.setSubTitleLanguage3(objJson.getString(Constant.SUBTITLE_LANGUAGE_3));
                            itemEpisode.setSubTitleUrl3(objJson.getString(Constant.SUBTITLE_URL_3));
                            itemEpisode.setWatchList(objJson.getBoolean(Constant.USER_WATCHLIST_STATUS));
                            mListItemEpisode.add(itemEpisode);
                        }
                        displayEpisode();

                    } else {
                        mProgressBarEpisode.setVisibility(View.GONE);
                        rvEpisode.setVisibility(View.GONE);
                        textNoEpisode.setVisibility(View.VISIBLE);
                        //setImageIfSeasonAndEpisodeNone(itemShow.getShowImage());
                        btnWatchList.setVisibility(View.GONE);
                        btnDownload.setVisibility(View.GONE);
                        lytShare.setVisibility(View.GONE);
                    }
                    if (!isEpisodeRedirect) {
                        initRipplePlay();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                mProgressBarEpisode.setVisibility(View.GONE);
                rvEpisode.setVisibility(View.GONE);
                textNoEpisode.setVisibility(View.VISIBLE);
                btnWatchList.setVisibility(View.GONE);
                btnDownload.setVisibility(View.GONE);
                lytShare.setVisibility(View.GONE);
            }
        });
    }

    private void displayEpisode() {
        episodeAdapter = new EpisodeAdapter(ShowDetailsActivity.this, mListItemEpisode);
        rvEpisode.setAdapter(episodeAdapter);

        //  play episode by default if it is episode redirect
        if (!mListItemEpisode.isEmpty() && isEpisodeRedirect) {
            int epSelected = getEpisodePos();
            Log.e("ep", "yes");
            playEpisode(epSelected);
            episodeAdapter.select(epSelected);
        }

        episodeAdapter.setOnItemClickListener(new RvOnClickListener() {
            @Override
            public void onItemClick(int position) {
                episodeAdapter.select(position);
                playEpisode(position);
            }
        });

    }

    private void initRipplePlay() {
        toolbar.setVisibility(View.VISIBLE);
        PlayRippleFragment playRippleFragment = PlayRippleFragment.newInstance(itemShow.getShowImage());
        playRippleFragment.setOnSkipClickListener(new RvOnClickListener() {
            @Override
            public void onItemClick(int position) {
                toolbar.setVisibility(View.GONE);
                trailerSkipClick();
            }
        });
        fragmentManager.beginTransaction().replace(R.id.playerSection, playRippleFragment).commitAllowingStateLoss();
    }

    private void initTrailerPlayer(String seasonTrailerUrl) {
        if (seasonTrailerUrl.isEmpty()) {
            btnTrailer.setVisibility(View.GONE);
        } else {
            btnTrailer.setOnClickListener(view -> {
                Intent intent = getIntent();
                if (PlayerUtil.isYoutubeUrl(seasonTrailerUrl)) {
                    intent.setClass(ShowDetailsActivity.this, YtPlayerActivity.class);
                    intent.putExtra("videoId", PlayerUtil.getVideoIdFromYoutubeUrl(seasonTrailerUrl));
                } else if (PlayerUtil.isVimeoUrl(seasonTrailerUrl)) {
                    intent.setClass(ShowDetailsActivity.this, VimeoPlayerActivity.class);
                    intent.putExtra("videoId", PlayerUtil.getVideoIdFromVimeoUrl(seasonTrailerUrl));
                } else {
                    intent.setClass(ShowDetailsActivity.this, TrailerExoPlayerActivity.class);
                    intent.putExtra("streamUrl", seasonTrailerUrl);
                }
                startActivity(intent);
            });
        }
    }

    private void trailerSkipClick() {
        if (episodeAdapter != null && mListItemEpisode.size() > 0 && !isUpcoming) {
            episodeAdapter.select(0);
            playEpisode(0);
        } else {
            setImageIfSeasonAndEpisodeNone(itemShow.getShowImage());
        }
    }

    private void playEpisode(int playPosition) {
        toolbar.setVisibility(View.GONE);
        selectedEpisode = playPosition;
        ItemEpisode itemEpisode = mListItemEpisode.get(playPosition);
        setEpisodeInfoText(itemEpisode);
        if (itemEpisode.getEpisodeType().equals(Constant.VIDEO_TYPE_HLS) && !itemEpisode.getEpisodeUrl().isEmpty()) {
            if (PlayerUtil.isYoutubeUrl(itemEpisode.getEpisodeUrl())) {
                itemEpisode.setEpisodeType(Constant.VIDEO_TYPE_YOUTUBE);
            } else if (PlayerUtil.isVimeoUrl(itemEpisode.getEpisodeUrl())) {
                itemEpisode.setEpisodeType(Constant.VIDEO_TYPE_VIMEO);
            }
        }
        if (itemEpisode.isPremium()) {
            if (isPurchased) {
                setPlayer(playPosition);
            } else {
                PremiumContentFragment premiumContentFragment = PremiumContentFragment.newInstance(Id, "Shows");
                fragmentManager.beginTransaction().replace(R.id.playerSection, premiumContentFragment).commitAllowingStateLoss();
            }
        } else {
            setPlayer(playPosition);
        }

        if (NetworkUtils.isConnected(ShowDetailsActivity.this)) {
            episodeRecentlyWatched(itemEpisode.getEpisodeId());
        }

        //  initWatchList(playPosition, itemEpisode.getEpisodeId(), itemEpisode.isWatchList());
    }

    private void clickOnWatchList(int position) {
        if (myApplication.getIsLogin()) {
            if (NetworkUtils.isConnected(ShowDetailsActivity.this)) {
                WatchListClickListener watchListClickListener = new WatchListClickListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onItemClick(boolean isAddWatchList, String message) {
                        mListItemEpisode.get(position).setWatchList(isAddWatchList);
                        episodeAdapter.notifyDataSetChanged();
                        btnWatchList.setIconResource(isAddWatchList ? R.drawable.ic_watch_list_remove : R.drawable.ic_watch_list_add);
                        //   btnWatchList.setText(isAddWatchList ? getString(R.string.remove_from_watch_list) : getString(R.string.add_to_watch_list));
                    }
                };
                new WatchList(ShowDetailsActivity.this).applyWatch(mListItemEpisode.get(position).isWatchList(), mListItemEpisode.get(position).getEpisodeId(), "Shows", watchListClickListener);
            } else {
                showToast(getString(R.string.conne_msg1));
            }
        } else {
            showToast(getString(R.string.login_first_watch_list));
            Intent intentLogin = new Intent(ShowDetailsActivity.this, SignInActivity.class);
            intentLogin.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intentLogin.putExtra("isOtherScreen", true);
            intentLogin.putExtra("postId", itemShow.getShowId());
            intentLogin.putExtra("postType", "Shows");
            startActivity(intentLogin);
        }
    }

    private void initCastPlayer(int playPosition) {
        ItemEpisode itemEpisode = mListItemEpisode.get(playPosition);
        if (itemEpisode.isPremium()) {
            if (isPurchased) {
                castScreen();
            } else {
                PremiumContentFragment premiumContentFragment = PremiumContentFragment.newInstance(Id, "Shows");
                fragmentManager.beginTransaction().replace(R.id.playerSection, premiumContentFragment).commitAllowingStateLoss();
            }
        } else {
            castScreen();
        }
    }

    private void setPlayer(int playPosition) {
        ItemEpisode itemEpisode = mListItemEpisode.get(playPosition);
        if (itemEpisode.getEpisodeUrl().isEmpty()) {
            showToast(getString(R.string.stream_not_found));
            EmbeddedImageFragment embeddedImageFragment = EmbeddedImageFragment.newInstance(itemEpisode.getEpisodeUrl(), itemEpisode.getEpisodeImage(), false);
            fragmentManager.beginTransaction().replace(R.id.playerSection, embeddedImageFragment).commitAllowingStateLoss();
        } else {
            switch (itemEpisode.getEpisodeType()) { //URL Embed
                case Constant.VIDEO_TYPE_LOCAL:
                case Constant.VIDEO_TYPE_URL:
                case Constant.VIDEO_TYPE_HLS:
                    if (casty != null && casty.isConnected()) {
                        castScreen();
                    } else {
                        ExoPlayerFragment exoPlayerFragment = ExoPlayerFragment.newInstance(getPlayerData(), mListItemEpisode.size(), selectedEpisode);
                        exoPlayerFragment.setOnNextPrevClickListener(new EpisodeNextPrevListener() {
                            @Override
                            public void onNextClick() {
                                selectedEpisode = selectedEpisode + 1;
                                playEpisode(selectedEpisode);
                                if (episodeAdapter != null) {
                                    episodeAdapter.select(selectedEpisode);
                                }
                            }
                        });
                        fragmentManager.beginTransaction().replace(R.id.playerSection, exoPlayerFragment).commitAllowingStateLoss();
                    }
                    break;
                case Constant.VIDEO_TYPE_EMBED:
                case Constant.VIDEO_TYPE_YOUTUBE:
                case Constant.VIDEO_TYPE_VIMEO:
                    EmbeddedImageFragment embeddedImageFragment = EmbeddedImageFragment.newInstance(itemEpisode.getEpisodeUrl(), itemEpisode.getEpisodeImage(), true);
                    fragmentManager.beginTransaction().replace(R.id.playerSection, embeddedImageFragment).commitAllowingStateLoss();
                    break;

            }
        }
    }

    private void episodeRecentlyWatched(String episodeId) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        jsObj.addProperty("episode_id", episodeId);
        jsObj.addProperty("user_id", myApplication.getIsLogin() ? myApplication.getUserId() : "");
        params.put("data", API.toBase64(jsObj.toString()));
        client.post(Constant.EPISODE_RECENTLY_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //    GlobalBus.getBus().unregister(this);
    }

    @Subscribe
    public void getFullScreen(Events.FullScreen fullScreen) {
        isFullScreen = fullScreen.isFullScreen();
        if (fullScreen.isFullScreen()) {
            gotoFullScreen();
        } else {
            gotoPortraitScreen();
        }
    }

    private void gotoPortraitScreen() {
        nestedScrollView.setVisibility(View.VISIBLE);
//        toolbar.setVisibility(View.VISIBLE);
        mAdViewLayout.setVisibility(View.VISIBLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        frameLayout.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, playerHeight));
        windowInsetsController.show(WindowInsetsCompat.Type.systemBars());
    }

    private void gotoFullScreen() {
        nestedScrollView.setVisibility(View.GONE);
//        toolbar.setVisibility(View.GONE);
        mAdViewLayout.setVisibility(View.GONE);
        frameLayout.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
    }

    @Override
    public void onBackPressed() {
        if (isFullScreen) {
            Events.FullScreen fullScreen = new Events.FullScreen();
            fullScreen.setFullScreen(false);
            GlobalBus.getBus().post(fullScreen);
        } else {
            if (isFromNotification) {
                Intent intent = new Intent(ShowDetailsActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void setImageIfSeasonAndEpisodeNone(String imageCover) {
        EmbeddedImageFragment embeddedImageFragment = EmbeddedImageFragment.newInstance("", imageCover, false);
        fragmentManager.beginTransaction().replace(R.id.playerSection, embeddedImageFragment).commitAllowingStateLoss();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //   casty.addMediaRouteMenuItem(menu);
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    private void playViaCast() {
        if (!mListItemEpisode.isEmpty()) {
            ItemEpisode itemEpisode = mListItemEpisode.get(selectedEpisode);
            if (itemEpisode.getEpisodeType().equals(Constant.VIDEO_TYPE_LOCAL) || itemEpisode.getEpisodeType().equals(Constant.VIDEO_TYPE_URL) || itemEpisode.getEpisodeType().equals(Constant.VIDEO_TYPE_HLS)) {
                casty.getPlayer().loadMediaAndPlay(createSampleMediaData(itemEpisode.getEpisodeUrl(), itemEpisode.getEpisodeName(), itemEpisode.getEpisodeImage()));
            } else {
                showToast(getResources().getString(R.string.cast_youtube));
            }
        } else {
            showToast(getString(R.string.stream_not_found));
        }
    }

    private MediaData createSampleMediaData(String videoUrl, String videoTitle, String videoImage) {
        return new MediaData.Builder(videoUrl).setStreamType(MediaData.STREAM_TYPE_BUFFERED).setContentType(getType(videoUrl)).setMediaType(MediaData.MEDIA_TYPE_MOVIE).setTitle(videoTitle).setSubtitle(getString(R.string.app_name)).addPhotoUrl(videoImage).build();
    }

    private String getType(String videoUrl) {
        if (videoUrl.endsWith(".mp4")) {
            return "videos/mp4";
        } else if (videoUrl.endsWith(".m3u8")) {
            return "application/x-mpegurl";
        } else {
            return "application/x-mpegurl";
        }
    }

    private void castScreen() {
        ChromecastScreenFragment chromecastScreenFragment = new ChromecastScreenFragment();
        fragmentManager.beginTransaction().replace(R.id.playerSection, chromecastScreenFragment).commitAllowingStateLoss();
        chromecastScreenFragment.setOnItemClickListener(new RvOnClickListener() {
            @Override
            public void onItemClick(int position) {
                playViaCast();
            }
        });
    }

    private ItemPlayer getPlayerData() {
        ItemPlayer itemPlayer = new ItemPlayer();
        ItemEpisode itemEpisode = mListItemEpisode.get(selectedEpisode);
        itemPlayer.setDefaultUrl(itemEpisode.getEpisodeUrl());
        if (itemEpisode.getEpisodeType().equals(Constant.VIDEO_TYPE_LOCAL) || itemEpisode.getEpisodeType().equals(Constant.VIDEO_TYPE_URL) || itemEpisode.getEpisodeType().equals(Constant.VIDEO_TYPE_HLS)) {
            itemPlayer.setQuality(itemEpisode.isQuality());
            itemPlayer.setSubTitle(itemEpisode.isSubTitle());
            itemPlayer.setQuality480(itemEpisode.getQuality480());
            itemPlayer.setQuality720(itemEpisode.getQuality720());
            itemPlayer.setQuality1080(itemEpisode.getQuality1080());
            //     itemPlayer.setSubTitles(itemEpisode.getSubTitles());
            ArrayList<ItemSubTitle> itemSubTitles = new ArrayList<>();
            ItemSubTitle subTitleOff = new ItemSubTitle("0", getString(R.string.off_sub_title), "");
            itemSubTitles.add(subTitleOff);
            if (!itemEpisode.getSubTitleLanguage1().isEmpty()) {
                ItemSubTitle subTitle1 = new ItemSubTitle("1", itemEpisode.getSubTitleLanguage1(), itemEpisode.getSubTitleUrl1());
                itemSubTitles.add(subTitle1);
            }
            if (!itemEpisode.getSubTitleLanguage2().isEmpty()) {
                ItemSubTitle subTitle2 = new ItemSubTitle("2", itemEpisode.getSubTitleLanguage2(), itemEpisode.getSubTitleUrl2());
                itemSubTitles.add(subTitle2);
            }
            if (!itemEpisode.getSubTitleLanguage3().isEmpty()) {
                ItemSubTitle subTitle3 = new ItemSubTitle("3", itemEpisode.getSubTitleLanguage3(), itemEpisode.getSubTitleUrl3());
                itemSubTitles.add(subTitle3);
            }
            itemPlayer.setSubTitles(itemSubTitles);

            if (itemEpisode.getQuality480().isEmpty() && itemEpisode.getQuality720().isEmpty() && itemEpisode.getQuality1080().isEmpty()) {
                itemPlayer.setQuality(false);
            }

            if (itemEpisode.getSubTitleLanguage1().isEmpty() && itemEpisode.getSubTitleLanguage2().isEmpty() && itemEpisode.getSubTitleLanguage3().isEmpty()) {
                itemPlayer.setSubTitle(false);
            }
        } else {
            itemPlayer.setQuality(false);
            itemPlayer.setSubTitle(false);
        }
        return itemPlayer;
    }

    private int getEpSeasonPos() {
        String seasonId = getIntent().getStringExtra("seasonId");
        for (int i = 0; i < mListSeason.size(); i++) {
            ItemSeason itemSeason = mListSeason.get(i);
            if (itemSeason.getSeasonId().equals(seasonId)) {
                return i;
            }
        }
        return 0;
    }

    private int getEpisodePos() {
        String episodeId = getIntent().getStringExtra("episodeId");
        for (int i = 0; i < mListItemEpisode.size(); i++) {
            ItemEpisode itemEpisode = mListItemEpisode.get(i);
            if (itemEpisode.getEpisodeId().equals(episodeId)) {
                return i;
            }
        }
        return 0;
    }
}
