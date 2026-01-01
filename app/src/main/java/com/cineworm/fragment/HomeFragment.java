package com.cineworm.fragment;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import com.cineworm.adapter.HomeAdapter;
import com.cineworm.adapter.HomeContentAdapter;
import com.cineworm.adapter.HomeVerticalAdapter;
import com.cineworm.item.ItemHome;
import com.cineworm.item.ItemHomeContent;
import com.cineworm.item.ItemHomeDisplay;
import com.cineworm.util.API;
import com.cineworm.util.Constant;
import com.cineworm.util.NetworkUtils;
import com.cineworm.util.RvOnClickListener;
import com.cineworm.videostreamingapp.MainActivity;
import com.cineworm.videostreamingapp.MovieDetailsActivity;
import com.cineworm.videostreamingapp.MyApplication;
import com.cineworm.videostreamingapp.R;
import com.cineworm.videostreamingapp.ShowDetailsActivity;
import com.cineworm.videostreamingapp.SportDetailsActivity;
import com.cineworm.videostreamingapp.TVDetailsActivity;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class HomeFragment extends Fragment {

    private ProgressBar mProgressBar;
    private LinearLayout lyt_not_found;
    private NestedScrollView nestedScrollView;
    private PlayerView playerView;
    private ExoPlayer exoPlayer;
    private ArrayList<ItemHome> homeList;
    private ArrayList<ItemHome> horizontalSectionsList;
    private ArrayList<ItemHomeDisplay> allDisplayList;
    private ArrayList<ItemHomeDisplay> displayedList;
    private ArrayList<ItemHomeContent> allContentList;
    private ArrayList<String> movieUrlsList;
    private RecyclerView rvHome;
    private RecyclerView rvHorizontalSections;
    private RelativeLayout lytSlider;
    private MyApplication myApplication;
    private HomeVerticalAdapter homeVerticalAdapter;
    private HomeAdapter horizontalSectionsAdapter;
    private MaterialButton btnLoadMore;
    private ProgressBar progressBarLoadMore;
    private LinearLayout lytLoadMore;
    private FloatingActionButton fabScrollToTop;
    private int itemsPerPage = 20;
    private int currentPage = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        myApplication = MyApplication.getInstance();

        homeList = new ArrayList<>();
        movieUrlsList = new ArrayList<>();
        horizontalSectionsList = new ArrayList<>();
        allDisplayList = new ArrayList<>();
        displayedList = new ArrayList<>();
        allContentList = new ArrayList<>();
        
        mProgressBar = rootView.findViewById(R.id.progressBar1);
        lyt_not_found = rootView.findViewById(R.id.lyt_not_found);
        nestedScrollView = rootView.findViewById(R.id.nestedScrollView);
        playerView = rootView.findViewById(R.id.playerView);
        lytSlider = rootView.findViewById(R.id.lytSlider);
        
        // Initialize ExoPlayer
        exoPlayer = new ExoPlayer.Builder(requireContext()).build();
        playerView.setPlayer(exoPlayer);
        exoPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);
        exoPlayer.setVolume(1f); // Unmuted autoplay
        rvHorizontalSections = rootView.findViewById(R.id.rv_horizontal_sections);
        rvHome = rootView.findViewById(R.id.rv_home);
        lytLoadMore = rootView.findViewById(R.id.lytLoadMore);
        btnLoadMore = rootView.findViewById(R.id.btnLoadMore);
        progressBarLoadMore = rootView.findViewById(R.id.progressBarLoadMore);
        fabScrollToTop = rootView.findViewById(R.id.fabScrollToTop);

        recyclerViewPropertyVertical(rvHome);
        recyclerViewPropertyHorizontal(rvHorizontalSections);
        
        // Setup scroll to top button
        nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY > 500) {
                    fabScrollToTop.setVisibility(View.VISIBLE);
                } else {
                    fabScrollToTop.setVisibility(View.GONE);
                }
            }
        });
        
        fabScrollToTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nestedScrollView.post(() -> {
                    ObjectAnimator animator = ObjectAnimator.ofInt(nestedScrollView, "scrollY", 0);
                    animator.setDuration(800);
                    animator.start();
                });
            }
        });
        
        btnLoadMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadMoreItems();
            }
        });

        if (NetworkUtils.isConnected(getActivity())) {
            getHome();
        } else {
            Toast.makeText(getActivity(), getString(R.string.conne_msg1), Toast.LENGTH_SHORT).show();
        }


        return rootView;
    }

    private void recyclerViewPropertyVertical(RecyclerView recyclerView) {
        recyclerView.setHasFixedSize(true);
        androidx.recyclerview.widget.GridLayoutManager gridLayoutManager = new androidx.recyclerview.widget.GridLayoutManager(getActivity(), 3);
        // Make headers span full width
        gridLayoutManager.setSpanSizeLookup(new androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (homeVerticalAdapter != null && position < displayedList.size()) {
                    return displayedList.get(position).getType() == ItemHomeDisplay.TYPE_HEADER ? 3 : 1;
                }
                return 1;
            }
        });
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setFocusable(false);
        recyclerView.setNestedScrollingEnabled(false);
    }
    
    private void recyclerViewPropertyHorizontal(RecyclerView recyclerView) {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setFocusable(false);
        recyclerView.setNestedScrollingEnabled(false);
    }


    private void getHome() {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        jsObj.addProperty("user_id", myApplication.getIsLogin() ? myApplication.getUserId() : "");
        params.put("data", API.toBase64(jsObj.toString()));

        client.post(Constant.HOME_URL, params, new AsyncHttpResponseHandler() {
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
                Log.d("HomeFragment", "API Response: " + result);
                try {
                    JSONObject mainJson = new JSONObject(result);
                    Log.d("HomeFragment", "Main JSON parsed successfully");
                    JSONObject liveTVJson = mainJson.getJSONObject(Constant.ARRAY_NAME);
                    Log.d("HomeFragment", "Array Name: " + Constant.ARRAY_NAME);

                    // Collect movie trailer URLs for auto-play
                    if (liveTVJson.has("slider")) {
                        JSONArray sliderArray = liveTVJson.getJSONArray("slider");
                        for (int i = 0; i < sliderArray.length(); i++) {
                            JSONObject jsonObject = sliderArray.getJSONObject(i);
                            // We'll collect trailer URLs from movies instead
                        }
                    }

                    if (liveTVJson.has("recently_watched")) {
                        JSONArray recentArray = liveTVJson.getJSONArray("recently_watched");
                        if (recentArray.length() > 0) {
                            ItemHome objItem = new ItemHome();
                            objItem.setHomeId("-1");
                            objItem.setHomeTitle(getString(R.string.home_recently_watched));
                            objItem.setHomeType("Recent");

                            ArrayList<ItemHomeContent> homeContentList = new ArrayList<>();
                            for (int i = 0; i < recentArray.length(); i++) {
                                JSONObject jsonObject = recentArray.getJSONObject(i);
                                ItemHomeContent itemHomeContent = new ItemHomeContent();
                                itemHomeContent.setVideoId(jsonObject.getString("video_id"));
                                itemHomeContent.setVideoImage(jsonObject.getString("video_thumb_image"));
                                itemHomeContent.setVideoType(jsonObject.getString("video_type"));
                                itemHomeContent.setHomeType("Recent");
                                itemHomeContent.setSeasonId(jsonObject.getString("season_id"));
                                itemHomeContent.setEpisodeId(jsonObject.getString("episode_id"));
                                homeContentList.add(itemHomeContent);
                            }
                            objItem.setItemHomeContents(homeContentList);
                            homeList.add(objItem);
                        }
                    }

                    if (liveTVJson.has("upcoming_movies")) {
                        JSONArray upcomingMovieArray = liveTVJson.getJSONArray("upcoming_movies");
                        if (upcomingMovieArray.length() > 0) {
                            ItemHome objItem = new ItemHome();
                            objItem.setHomeId("-1");
                            objItem.setHomeTitle(getString(R.string.home_upcoming_movie));
                            objItem.setHomeType("Movie");

                            ArrayList<ItemHomeContent> homeContentList = new ArrayList<>();
                            for (int i = 0; i < upcomingMovieArray.length(); i++) {
                                JSONObject objJson = upcomingMovieArray.getJSONObject(i);
                                ItemHomeContent itemHomeContent = new ItemHomeContent();
                                itemHomeContent.setVideoId(objJson.getString(Constant.MOVIE_ID));
                                itemHomeContent.setVideoTitle(objJson.getString(Constant.MOVIE_TITLE));
                                itemHomeContent.setVideoImage(objJson.getString(Constant.MOVIE_POSTER));
                                itemHomeContent.setVideoType("Movie");
                                itemHomeContent.setHomeType("Movie");
                                itemHomeContent.setPremium(objJson.getString(Constant.MOVIE_ACCESS).equals("Paid"));
                                homeContentList.add(itemHomeContent);
                            }
                            objItem.setItemHomeContents(homeContentList);
                            homeList.add(objItem);
                        }
                    }

                    if (liveTVJson.has("upcoming_series")) {
                        JSONArray upcomingSeriesArray = liveTVJson.getJSONArray("upcoming_series");
                        if (upcomingSeriesArray.length() > 0) {
                            ItemHome objItem = new ItemHome();
                            objItem.setHomeId("-1");
                            objItem.setHomeTitle(getString(R.string.home_upcoming_show));
                            objItem.setHomeType("Shows");

                            ArrayList<ItemHomeContent> homeContentList = new ArrayList<>();
                            for (int i = 0; i < upcomingSeriesArray.length(); i++) {
                                JSONObject objJson = upcomingSeriesArray.getJSONObject(i);
                                ItemHomeContent itemHomeContent = new ItemHomeContent();
                                itemHomeContent.setVideoId(objJson.getString(Constant.SHOW_ID));
                                itemHomeContent.setVideoTitle(objJson.getString(Constant.SHOW_TITLE));
                                itemHomeContent.setVideoImage(objJson.getString(Constant.SHOW_POSTER));
                                itemHomeContent.setVideoType("Shows");
                                itemHomeContent.setHomeType("Shows");
                                itemHomeContent.setPremium(objJson.getString(Constant.SHOW_ACCESS).equals("Paid"));
                                homeContentList.add(itemHomeContent);
                            }
                            objItem.setItemHomeContents(homeContentList);
                            homeList.add(objItem);
                        }
                    }

                    if (liveTVJson.has("home_sections")) {
                        JSONArray homeSecArray = liveTVJson.getJSONArray("home_sections");
                        for (int i = 0; i < homeSecArray.length(); i++) {
                            JSONObject objJson = homeSecArray.getJSONObject(i);
                            ItemHome objItem = new ItemHome();
                            objItem.setHomeId(objJson.getString("home_id"));
                            objItem.setHomeTitle(objJson.getString("home_title"));
                            objItem.setHomeType(objJson.getString("home_type"));

                            JSONArray homeContentArray = objJson.getJSONArray("home_content");
                            ArrayList<ItemHomeContent> homeContentList = new ArrayList<>();
                            for (int j = 0; j < homeContentArray.length(); j++) {
                                JSONObject objJsonSec = homeContentArray.getJSONObject(j);
                                ItemHomeContent itemHomeContent = new ItemHomeContent();
                                itemHomeContent.setVideoId(objJsonSec.getString("video_id"));
                                itemHomeContent.setVideoTitle(objJsonSec.getString("video_title"));
                                itemHomeContent.setVideoImage(objJsonSec.getString("video_image"));
                                itemHomeContent.setVideoType(objJsonSec.getString("video_type"));
                                itemHomeContent.setHomeType(objJsonSec.getString("video_type"));
                                itemHomeContent.setPremium(objJsonSec.getString("video_access").equals("Paid"));
                                homeContentList.add(itemHomeContent);
                            }
                            objItem.setItemHomeContents(homeContentList);
                            if (!homeContentList.isEmpty()) // when there is not date in section
                                homeList.add(objItem);
                        }
                    }
                    displayData();
                    Log.d("HomeFragment", "Total home sections: " + homeList.size());
                    loadRandomMovieTrailer();

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("HomeFragment", "JSON Parsing Error: " + e.getMessage());
                    Log.e("HomeFragment", "Response was: " + result);
                    nestedScrollView.setVisibility(View.GONE);
                    lyt_not_found.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                mProgressBar.setVisibility(View.GONE);
                nestedScrollView.setVisibility(View.GONE);
                lyt_not_found.setVisibility(View.VISIBLE);
                Log.e("HomeFragment", "API Failure - Status Code: " + statusCode);
                if (error != null) {
                    Log.e("HomeFragment", "Error: " + error.getMessage());
                }
                if (responseBody != null) {
                    Log.e("HomeFragment", "Response: " + new String(responseBody));
                }
            }
        });
    }

    private void displayData() {
        // Player will be initialized separately with loadRandomMovieTrailer()
        lytSlider.setVisibility(View.VISIBLE);

        // Separate horizontal sections (Recent, Upcoming) from vertical content
        horizontalSectionsList.clear();
        allDisplayList.clear();
        allContentList.clear();
        
        if (!homeList.isEmpty()) {
            for (ItemHome itemHome : homeList) {
                String homeId = itemHome.getHomeId();
                String homeType = itemHome.getHomeType();
                
                // Keep Recently Watched and Upcoming sections horizontal
                if (homeId.equals("-1") && (homeType.equals("Recent") || homeType.equals("Movie") || homeType.equals("Shows"))) {
                    horizontalSectionsList.add(itemHome);
                } else {
                    // Add section header and content to vertical list
                    if (itemHome.getItemHomeContents() != null && !itemHome.getItemHomeContents().isEmpty()) {
                        // Add section header
                        allDisplayList.add(new ItemHomeDisplay(itemHome.getHomeTitle(), itemHome.getHomeId()));
                        
                        // Add all content items from this section
                        for (ItemHomeContent content : itemHome.getItemHomeContents()) {
                            allDisplayList.add(new ItemHomeDisplay(content));
                            allContentList.add(content);
                        }
                    }
                }
            }
        }
        
        Log.d("HomeFragment", "Horizontal sections: " + horizontalSectionsList.size());
        Log.d("HomeFragment", "Vertical display items: " + allDisplayList.size());
        Log.d("HomeFragment", "Vertical content items: " + allContentList.size());

        // Display horizontal sections (Recently Watched, Upcoming)
        if (!horizontalSectionsList.isEmpty()) {
            horizontalSectionsAdapter = new HomeAdapter(getActivity(), horizontalSectionsList);
            rvHorizontalSections.setAdapter(horizontalSectionsAdapter);
            horizontalSectionsAdapter.setOnItemClickListener(new RvOnClickListener() {
                @Override
                public void onItemClick(int position) {
                    ItemHome itemHome = horizontalSectionsList.get(position);
                    Bundle bundle = new Bundle();
                    bundle.putString("Id", itemHome.getHomeId());
                    bundle.putString("Type", itemHome.getHomeType());
                    HomeContentMoreFragment homeMovieMoreFragment = new HomeContentMoreFragment();
                    homeMovieMoreFragment.setArguments(bundle);
                    changeFragment(homeMovieMoreFragment, itemHome.getHomeTitle());
                }
            });
            rvHorizontalSections.setVisibility(View.VISIBLE);
        } else {
            rvHorizontalSections.setVisibility(View.GONE);
        }

        // Display vertical content with pagination
        if (!allDisplayList.isEmpty()) {
            currentPage = 0;
            displayedList.clear();
            loadMoreItems();
            rvHome.setVisibility(View.VISIBLE);
        } else {
            rvHome.setVisibility(View.GONE);
            lytLoadMore.setVisibility(View.GONE);
        }

        // Show "No Item Found" only if all lists are empty
        if (horizontalSectionsList.isEmpty() && allDisplayList.isEmpty()) {
            nestedScrollView.setVisibility(View.GONE);
            lyt_not_found.setVisibility(View.VISIBLE);
            lytSlider.setVisibility(View.GONE);
            Log.d("HomeFragment", "No data available - showing no item found");
        }

    }
    
    private void loadMoreItems() {
        btnLoadMore.setVisibility(View.GONE);
        progressBarLoadMore.setVisibility(View.VISIBLE);
        
        // Save current scroll position
        final int scrollY = nestedScrollView.getScrollY();
        
        nestedScrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                int startIndex = currentPage * itemsPerPage;
                int endIndex = Math.min(startIndex + itemsPerPage, allDisplayList.size());
                
                if (startIndex < allDisplayList.size()) {
                    for (int i = startIndex; i < endIndex; i++) {
                        displayedList.add(allDisplayList.get(i));
                    }
                    
                    if (homeVerticalAdapter == null) {
                        homeVerticalAdapter = new HomeVerticalAdapter(getActivity(), displayedList);
                        rvHome.setAdapter(homeVerticalAdapter);
                        setupContentClickListener();
                    } else {
                        homeVerticalAdapter.notifyDataSetChanged();
                    }
                    
                    currentPage++;
                    
                    // Show/hide load more button based on remaining items
                    if (endIndex >= allDisplayList.size()) {
                        lytLoadMore.setVisibility(View.GONE);
                    } else {
                        lytLoadMore.setVisibility(View.VISIBLE);
                        btnLoadMore.setVisibility(View.VISIBLE);
                        progressBarLoadMore.setVisibility(View.GONE);
                    }
                } else {
                    lytLoadMore.setVisibility(View.GONE);
                }
            }
        }, 500);
    }
    
    private void setupContentClickListener() {
        homeVerticalAdapter.setOnItemClickListener(new RvOnClickListener() {
            @Override
            public void onItemClick(int position) {
                // Position here is the actual content position (headers are excluded in adapter)
                ItemHomeContent itemHomeContent = allContentList.get(position);
                Class<?> aClass;
                Intent intent = new Intent();
                switch (itemHomeContent.getVideoType()) {
                    case "Movie":
                    default:
                        aClass = MovieDetailsActivity.class;
                        break;
                    case "Shows":
                        aClass = ShowDetailsActivity.class;
                        if (itemHomeContent.getHomeType().equals("Recent")) {
                            intent.putExtra("episodeRedirect", true);
                            intent.putExtra("episodeId", itemHomeContent.getEpisodeId());
                            intent.putExtra("seasonId", itemHomeContent.getSeasonId());
                        }
                        break;
                    case "Sports":
                        aClass = SportDetailsActivity.class;
                        break;
                    case "LiveTV":
                        aClass = TVDetailsActivity.class;
                        break;
                }
                intent.setClass(getActivity(), aClass);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("Id", itemHomeContent.getVideoId());
                startActivity(intent);
            }
        });
    }

    private void changeFragment(Fragment fragment, String Name) {
        FragmentManager fm = getFragmentManager();
        assert fm != null;
        FragmentTransaction ft = fm.beginTransaction();
        ft.hide(HomeFragment.this);
        ft.add(R.id.Container, fragment, Name);
        ft.addToBackStack(Name);
        ft.commit();
        ((MainActivity) requireActivity()).setToolbarTitle(Name);
    }

    private void loadRandomMovieTrailer() {
        // Get a random movie from all content
        if (allContentList != null && !allContentList.isEmpty()) {
            int randomIndex = (int) (Math.random() * allContentList.size());
            ItemHomeContent randomContent = allContentList.get(randomIndex);
            
            // Fetch movie details to get trailer URL
            String videoId = randomContent.getVideoId();
            String videoType = randomContent.getVideoType();
            String videoTitle = randomContent.getVideoTitle() != null ? randomContent.getVideoTitle() : "Featured Content";
            
            Log.d("HomeFragment", "Loading random video: " + videoId + " Type: " + videoType);
            
            // For now, we'll use a demo video URL - you can fetch actual trailer from API
            // In a real scenario, you'd call the movie details API to get the trailer URL
            String demoVideoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4";
            
            if (exoPlayer != null) {
                // Create MediaItem with metadata to show title and details
                MediaItem mediaItem = new MediaItem.Builder()
                        .setUri(demoVideoUrl)
                        .setMediaMetadata(
                                new androidx.media3.common.MediaMetadata.Builder()
                                        .setTitle(videoTitle)
                                        .setArtist(videoType)
                                        .setDescription("Tap to view details")
                                        .build()
                        )
                        .build();
                
                exoPlayer.setMediaItem(mediaItem);
                exoPlayer.prepare();
                exoPlayer.setPlayWhenReady(true);
            }
        } else {
            lytSlider.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (exoPlayer != null) {
            exoPlayer.pause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (exoPlayer != null && exoPlayer.getPlaybackState() != Player.STATE_IDLE) {
            exoPlayer.setPlayWhenReady(true);
        }
    }
}
