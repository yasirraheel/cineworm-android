package com.cineworm.videostreamingapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cineworm.adapter.HomeMovieAdapter;
import com.cineworm.adapter.HomeShowAdapter;
import com.cineworm.item.ItemMovie;
import com.cineworm.item.ItemShow;
import com.cineworm.util.API;
import com.cineworm.util.Constant;
import com.cineworm.util.IsRTL;
import com.cineworm.util.LinearLayoutPagerManager;
import com.cineworm.util.NetworkUtils;
import com.cineworm.util.RvOnClickListener;
import com.cineworm.util.StatusBarUtil;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class ActorDirectorDetailActivity extends BaseActivity {

    String adId, adName, adImage, adBirthPlace, adBirthDate, adBio;
    TextView tvAdName, tvAdBirthPlace, tvAdBirthDate, tvAdBio;
    ShapeableImageView ivAd;
    boolean isActor = false;
    ProgressBar mProgressBar;
    LinearLayout lyt_not_found;
    NestedScrollView nestedScrollView;
    RecyclerView rvMovie, rvShow;
    ArrayList<ItemMovie> mListMovies;
    ArrayList<ItemShow> mListShow;
    HomeMovieAdapter movieAdapter;
    HomeShowAdapter showAdapter;
    LinearLayout lytMovie, lytShow;


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        StatusBarUtil.setStatusBarGradiant(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actor_director_details);
        IsRTL.ifSupported(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        Intent intent = getIntent();
        adId = intent.getStringExtra("adId");
        adName = intent.getStringExtra("adName");
        isActor = intent.getBooleanExtra("isActor", false);
        mListMovies = new ArrayList<>();
        mListShow = new ArrayList<>();

        setTitle(adName);
        mProgressBar = findViewById(R.id.progressBar1);
        lyt_not_found = findViewById(R.id.lyt_not_found);
        nestedScrollView = findViewById(R.id.nestedScrollView);
        lytMovie = findViewById(R.id.lytMovie);
        lytShow = findViewById(R.id.lytHomeTVSeries);
        rvMovie = findViewById(R.id.rv_latest_movie);
        rvShow = findViewById(R.id.rv_tv_series);
        tvAdName = findViewById(R.id.tvName);
        tvAdBirthDate = findViewById(R.id.tvBirthDay);
        tvAdBirthPlace = findViewById(R.id.tvBirthPlace);
        tvAdBio = findViewById(R.id.tvBio);
        ivAd = findViewById(R.id.image);

        rvMovie.setHasFixedSize(true);
        rvMovie.setLayoutManager(new LinearLayoutPagerManager(ActorDirectorDetailActivity.this, LinearLayoutManager.HORIZONTAL, false, 1));
        rvMovie.setFocusable(false);
        rvMovie.setNestedScrollingEnabled(false);

        rvShow.setHasFixedSize(true);
        rvShow.setLayoutManager(new LinearLayoutPagerManager(ActorDirectorDetailActivity.this, LinearLayoutManager.HORIZONTAL, false, 2));
        rvShow.setFocusable(false);
        rvShow.setNestedScrollingEnabled(false);

        if (NetworkUtils.isConnected(ActorDirectorDetailActivity.this)) {
            getActorDirectorDetail();
        } else {
            Toast.makeText(ActorDirectorDetailActivity.this, getString(R.string.conne_msg1), Toast.LENGTH_SHORT).show();
        }
    }

    private void getActorDirectorDetail() {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        jsObj.addProperty(isActor ? "a_id" : "d_id", adId);
        params.put("data", API.toBase64(jsObj.toString()));
        client.post(isActor ? Constant.ACTOR_DETAILS_URL : Constant.DIRECTOR_DETAILS_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                mProgressBar.setVisibility(View.VISIBLE);
                nestedScrollView.setVisibility(View.GONE);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                mProgressBar.setVisibility(View.GONE);
                nestedScrollView.setVisibility(View.VISIBLE);
                String result = new String(responseBody);
                try {
                    JSONObject mainJson = new JSONObject(result);
                    JSONObject liveTVJson = mainJson.getJSONObject(Constant.ARRAY_NAME);
                    adImage = liveTVJson.getString("ad_image");
                    adName = liveTVJson.getString("ad_name");
                    adBirthPlace = liveTVJson.getString("ad_place_of_birth");
                    adBirthDate = liveTVJson.getString("ad_birthdate");
                    adBio = liveTVJson.getString("ad_bio");
                    if (liveTVJson.length() > 0) {
                        Log.e("yes", "-->");
                        if (liveTVJson.has("movies")) {
                            JSONArray latestMovieArray = liveTVJson.getJSONArray("movies");
                            for (int i = 0; i < latestMovieArray.length(); i++) {
                                JSONObject objJson = latestMovieArray.getJSONObject(i);
                                ItemMovie objItem = new ItemMovie();
                                objItem.setMovieId(objJson.getString(Constant.MOVIE_ID));
                                objItem.setMovieName(objJson.getString(Constant.MOVIE_TITLE));
                                objItem.setMovieImage(objJson.getString(Constant.MOVIE_POSTER));
                                objItem.setMovieDuration(objJson.getString(Constant.MOVIE_DURATION));
                                objItem.setPremium(objJson.getString(Constant.MOVIE_ACCESS).equals("Paid"));
                                mListMovies.add(objItem);
                            }
                        }

                        if (liveTVJson.has("shows")) {
                            JSONArray latestShowArray = liveTVJson.getJSONArray("shows");
                            for (int i = 0; i < latestShowArray.length(); i++) {
                                JSONObject objJson = latestShowArray.getJSONObject(i);
                                ItemShow objItem = new ItemShow();
                                objItem.setShowId(objJson.getString(Constant.SHOW_ID));
                                objItem.setShowName(objJson.getString(Constant.SHOW_TITLE));
                                objItem.setShowImage(objJson.getString(Constant.SHOW_POSTER));
                                objItem.setPremium(objJson.getString(Constant.SHOW_ACCESS).equals("Paid"));
                                mListShow.add(objItem);
                            }
                        }

                        displayData();
                    } else {
                        Log.e("NO", "-->");
                        mProgressBar.setVisibility(View.GONE);
                        nestedScrollView.setVisibility(View.VISIBLE);
                        //     lyt_not_found.setVisibility(View.VISIBLE);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable
                    error) {
                mProgressBar.setVisibility(View.GONE);
                nestedScrollView.setVisibility(View.GONE);
                lyt_not_found.setVisibility(View.VISIBLE);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void displayData() {
        if (!adImage.isEmpty()) {
            Picasso.get().load(adImage).placeholder(R.drawable.place_holder_movie).into(ivAd);
        }
        tvAdName.setText(adName);
        tvAdBirthDate.setText(adBirthDate.isEmpty() ? getString(R.string.not_added) : adBirthDate);

        if (adBirthPlace.equals("null") || adBirthPlace.isEmpty()) {
            tvAdBirthPlace.setText(getString(R.string.not_added));
        } else {
            tvAdBirthPlace.setText(adBirthPlace);
        }
        if (adBio.isEmpty()) {
            adBio = getString(R.string.not_added);
        }
        String text = "<font color=#f4f4f4>" + getString(R.string.bio) + " " + "</font>" + "<font color=#9c9c9c>" + adBio + "</font>";
        tvAdBio.setText(Html.fromHtml(text));
        tvAdBio.setMovementMethod(new ScrollingMovementMethod());
        tvAdBirthPlace.setSelected(true);
        tvAdBirthDate.setSelected(true);

        if (!mListMovies.isEmpty()) {
            movieAdapter = new HomeMovieAdapter(ActorDirectorDetailActivity.this, mListMovies);
            rvMovie.setAdapter(movieAdapter);

            movieAdapter.setOnItemClickListener(new RvOnClickListener() {
                @Override
                public void onItemClick(int position) {
                    String movieId = mListMovies.get(position).getMovieId();
                    Intent intent = new Intent(ActorDirectorDetailActivity.this, MovieDetailsActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("Id", movieId);
                    startActivity(intent);
                }
            });

        } else {
            lytMovie.setVisibility(View.GONE);
        }

        if (!mListShow.isEmpty()) {
            showAdapter = new HomeShowAdapter(ActorDirectorDetailActivity.this, mListShow);
            rvShow.setAdapter(showAdapter);

            showAdapter.setOnItemClickListener(new RvOnClickListener() {
                @Override
                public void onItemClick(int position) {
                    String seriesId = mListShow.get(position).getShowId();
                    Intent intent = new Intent(ActorDirectorDetailActivity.this, ShowDetailsActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("Id", seriesId);
                    startActivity(intent);
                }
            });

        } else {
            lytShow.setVisibility(View.GONE);
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}
