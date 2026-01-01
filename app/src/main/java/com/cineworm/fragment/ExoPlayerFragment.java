package com.cineworm.fragment;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.fragment.app.Fragment;
import androidx.media3.common.C;
import androidx.media3.common.Format;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.common.util.Util;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DataSpec;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.dash.DashMediaSource;
import androidx.media3.exoplayer.hls.HlsMediaSource;
import androidx.media3.exoplayer.ima.ImaAdsLoader;
import androidx.media3.exoplayer.smoothstreaming.SsMediaSource;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.MergingMediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.media3.exoplayer.source.ads.AdsMediaSource;
import androidx.media3.extractor.Extractor;
import androidx.media3.extractor.ExtractorsFactory;
import androidx.media3.extractor.text.DefaultSubtitleParserFactory;
import androidx.media3.extractor.text.SubtitleExtractor;
import androidx.media3.extractor.text.SubtitleParser;
import androidx.media3.ui.CaptionStyleCompat;
import androidx.media3.ui.PlayerView;
import androidx.media3.ui.SubtitleView;
import androidx.recyclerview.widget.RecyclerView;

import com.cineworm.adapter.SubTitleAdapter;
import com.cineworm.item.ItemPlayer;
import com.cineworm.item.ItemSubTitle;
import com.cineworm.util.Constant;
import com.cineworm.util.EpisodeNextPrevListener;
import com.cineworm.util.Events;
import com.cineworm.util.GlobalBus;
import com.cineworm.util.RvOnClickListener;
import com.cineworm.util.UnknownSubtitlesExtractor;
import com.cineworm.videostreamingapp.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;

import org.greenrobot.eventbus.Subscribe;
import org.jetbrains.annotations.NotNull;

@OptIn(markerClass = UnstableApi.class)
public class ExoPlayerFragment extends Fragment {
    private ExoPlayer player;
    private ProgressBar progressBar;
    ImageView imgFull, imgSetting;
    public boolean isFullScr = false, isShow = false, isNextEpisode = true, durationSet = false, isAd = false;
    Button btnTryAgain;
    SubtitleView subtitleView;
    private static final String playerData = "playerData";
    ItemPlayer itemPlayer;
    int mSubPosition = 0, mQualityPosition = 0, mSubPreviousPosition = 0, mQualityPreviousPosition = 0;
    private long playerPosition;
    int mListSize, selectedEpisode;
    private EpisodeNextPrevListener episodeNextPrevListener;
    PlayerView playerView;
    private ImaAdsLoader adsLoader;

    public static ExoPlayerFragment newInstance(ItemPlayer itemPlayer) {
        ExoPlayerFragment f = new ExoPlayerFragment();
        Bundle args = new Bundle();
        args.putParcelable(playerData, itemPlayer);
        f.setArguments(args);
        return f;
    }

    public static ExoPlayerFragment newInstance(ItemPlayer itemPlayer, int numberOfEp, int selectedEp) {
        ExoPlayerFragment f = new ExoPlayerFragment();
        Bundle args = new Bundle();
        args.putParcelable(playerData, itemPlayer);
        args.putInt("numberOfEp", numberOfEp);
        args.putInt("selectedEp", selectedEp);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_exo_player, container, false);
        GlobalBus.getBus().register(this);
        Bundle bundle = getArguments();
        if (bundle != null) {
            itemPlayer = (ItemPlayer) bundle.getParcelable(playerData);
            if (itemPlayer == null) {
                Log.e("ExoPlayerFragment", "ItemPlayer is null!");
                return rootView;
            }
            if (bundle.containsKey("numberOfEp")) {
                isShow = true;
                mListSize = bundle.getInt("numberOfEp");
                selectedEpisode = bundle.getInt("selectedEp");
            }
        } else {
            Log.e("ExoPlayerFragment", "Bundle is null!");
            return rootView;
        }
        player = new ExoPlayer.Builder(requireActivity()).setSeekBackIncrementMs(10000).setSeekForwardIncrementMs(10000).build();
        playerView = rootView.findViewById(R.id.exoPlayerView);
        playerView.setPlayer(player);
        playerView.setUseController(true);
        playerView.requestFocus();

        subtitleView = playerView.findViewById(R.id.exo_subtitles);
        imgFull = playerView.findViewById(R.id.exo_fullscreen);
        imgSetting = playerView.findViewById(R.id.exo_settings);
        progressBar = rootView.findViewById(R.id.progressBar);
        btnTryAgain = rootView.findViewById(R.id.btn_try_again);
        imgFull.setVisibility(itemPlayer.isTrailer() ? View.GONE : View.VISIBLE);

        isAd = Constant.isPlayerAd && !Constant.playerAdVastUrl.isEmpty() && !itemPlayer.isTrailer();

        String videoUrl = getSelectedQuantityStreamUrl(mQualityPosition);
        Log.d("ExoPlayerFragment", "Video URL: " + videoUrl);
        
        if (videoUrl == null || videoUrl.isEmpty()) {
            Log.e("ExoPlayerFragment", "Video URL is null or empty!");
            progressBar.setVisibility(View.GONE);
            btnTryAgain.setVisibility(View.VISIBLE);
            return rootView;
        }
        
        try {
            Uri uri = Uri.parse(videoUrl);
            if (isAd) {
                adsLoader = new ImaAdsLoader.Builder(requireActivity()).build();
                adsLoader.setPlayer(player);
            }
            MediaSource mediaSource = buildMediaSourceWithAd(uri);
            player.setMediaSource(mediaSource);
            player.prepare();
            player.setPlayWhenReady(true);
        } catch (Exception e) {
            Log.e("ExoPlayerFragment", "Error initializing player: " + e.getMessage());
            e.printStackTrace();
            progressBar.setVisibility(View.GONE);
            btnTryAgain.setVisibility(View.VISIBLE);
            return rootView;
        }

        Typeface subtitleTypeface = Typeface.createFromAsset(requireActivity().getAssets(), "fonts/custom.otf");
        subtitleView.setStyle(new CaptionStyleCompat(Color.WHITE, Color.parseColor("#99000000"), Color.TRANSPARENT, CaptionStyleCompat.EDGE_TYPE_NONE, Color.WHITE, subtitleTypeface));
        subtitleView.setFixedTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);

        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_READY) {
                    progressBar.setVisibility(View.GONE);
                }
                if (isShow) {
                    if (playbackState == ExoPlayer.STATE_READY && !durationSet) {
                        durationSet = true;
                        if (isNextEpisode) {
                            startNextEpisode();
                        }
                    }
                }
            }

            @Override
            public void onPlayerError(@NotNull PlaybackException error) {
                Log.e("ExoPlayerFragment", "Player Error: " + error.getMessage());
                Log.e("ExoPlayerFragment", "Error code: " + error.errorCode);
                error.printStackTrace();
                player.stop();
                btnTryAgain.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }
        });

        imgFull.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFullScr) {
                    isFullScr = false;
                    Events.FullScreen fullScreen = new Events.FullScreen();
                    fullScreen.setFullScreen(false);
                    GlobalBus.getBus().post(fullScreen);
                } else {
                    isFullScr = true;
                    Events.FullScreen fullScreen = new Events.FullScreen();
                    fullScreen.setFullScreen(true);
                    GlobalBus.getBus().post(fullScreen);
                }
            }
        });

        btnTryAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnTryAgain.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                retryLoad();
            }
        });

        if (!itemPlayer.isQuality() && !itemPlayer.isSubTitle()) {
            imgSetting.setVisibility(View.GONE);
        }

        imgSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settingOpen();
            }
        });

        if (isShow) {
            enableDisableEpNext(mListSize, selectedEpisode);
        }

        return rootView;
    }

    public void retryLoad() {
        mSubPosition = 0;
        mQualityPosition = 0;
        Uri uri = Uri.parse(getSelectedQuantityStreamUrl(mQualityPosition));
        MediaSource mediaSource = buildMediaSourceWithAd(uri);
        player.setMediaSource(mediaSource);
        player.prepare();
        player.setPlayWhenReady(true);
    }

    private MediaSource buildMediaSourceWithAd(Uri uri) {
        if (isAd) {
            Uri adTagUri = Uri.parse(Constant.playerAdVastUrl);
            return new AdsMediaSource(buildMediaSource(uri), new DataSpec(adTagUri), adTagUri, new DefaultMediaSourceFactory(requireActivity()), adsLoader, playerView);
        } else {
            return buildMediaSource(uri);
        }
    }

    private MediaSource buildMediaSource(Uri uri) {
        try {
            int type = Util.inferContentType(uri);
            MediaItem mediaItem = MediaItem.fromUri(uri);
            Log.d("ExoPlayerFragment", "Media type: " + type + ", URI: " + uri.toString());
            switch (type) {
                case C.CONTENT_TYPE_SS:
                    return new SsMediaSource.Factory(buildDataSourceFactory()).createMediaSource(mediaItem);
                case C.CONTENT_TYPE_DASH:
                    return new DashMediaSource.Factory(buildDataSourceFactory()).createMediaSource(mediaItem);
                case C.CONTENT_TYPE_HLS:
                    return new HlsMediaSource.Factory(buildDataSourceFactory()).createMediaSource(mediaItem);
                case C.CONTENT_TYPE_OTHER:
                    return new ProgressiveMediaSource.Factory(buildDataSourceFactory()).createMediaSource(mediaItem);
                default: {
                    Log.e("ExoPlayerFragment", "Unsupported type: " + type);
                    throw new IllegalStateException("Unsupported type: " + type);
                }
            }
        } catch (Exception e) {
            Log.e("ExoPlayerFragment", "Error building media source: " + e.getMessage());
            throw e;
        }
    }

    private MediaSource buildSubtitleMediaSource(Uri subtitleUri) {
        MediaItem mediaItem = MediaItem.fromUri(subtitleUri);
        SubtitleParser.Factory subtitleParserFactory = new DefaultSubtitleParserFactory();
        DataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(requireActivity());
        Format format =
                new Format.Builder()
                        .setSampleMimeType(getSubTitleMimeType(subtitleUri))
                        .setLanguage("en")
                        .setSelectionFlags(Format.NO_VALUE)
                        .build();
        ExtractorsFactory extractorsFactory =
                () ->
                        new Extractor[]{
                                subtitleParserFactory.supportsFormat(format)
                                        ? new SubtitleExtractor(subtitleParserFactory.create(format), format)
                                        : new UnknownSubtitlesExtractor(format)
                        };
        ProgressiveMediaSource.Factory progressiveMediaSourceFactory =
                new ProgressiveMediaSource.Factory(dataSourceFactory, extractorsFactory);

        return progressiveMediaSourceFactory.createMediaSource(mediaItem);

    }

    private DataSource.Factory buildDataSourceFactory() {
        return new DefaultHttpDataSource.Factory().setUserAgent(Util.getUserAgent(requireActivity(), "ExoPlayerDemo"));
    }

    private String getSubTitleMimeType(Uri subtitleUri) {
        String url = subtitleUri.toString();
        if (url.endsWith(".vtt")) {
            return MimeTypes.TEXT_VTT;
        } else if (url.endsWith(".xml")) {
            return MimeTypes.APPLICATION_TTML;
        } else if (url.endsWith(".ass")) {
            return MimeTypes.TEXT_SSA;
        } else {
            return MimeTypes.APPLICATION_SUBRIP;
        }
    }

    @Subscribe
    public void getFullScreen(Events.FullScreen fullScreen) {
        isFullScr = fullScreen.isFullScreen();
        if (fullScreen.isFullScreen()) {
            imgFull.setImageResource(R.drawable.ic_fullscreen_exit);
        } else {
            imgFull.setImageResource(R.drawable.ic_fullscreen);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (player != null && player.getPlayWhenReady()) {
            player.setPlayWhenReady(false);
            player.getPlaybackState();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (player != null && player.getPlayWhenReady()) {
            player.setPlayWhenReady(false);
            player.getPlaybackState();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (player != null) {
            player.setPlayWhenReady(true);
            player.getPlaybackState();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        GlobalBus.getBus().unregister(this);
        if (player != null) {
            if (adsLoader != null) {
                adsLoader.release();
            }
            player.setPlayWhenReady(false);
            player.stop();
            player.release();
        }
    }

    private void settingOpen() {
        BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(requireActivity());
        View sheetView = requireActivity().getLayoutInflater().inflate(R.layout.layout_player_setting, null);
        mBottomSheetDialog.setContentView(sheetView);
        mBottomSheetDialog.show();

        LinearLayout lytQuality = sheetView.findViewById(R.id.lytQuality);
        LinearLayout lytSubTitles = sheetView.findViewById(R.id.lytSubTitles);
        MaterialButton btn480 = sheetView.findViewById(R.id.btn420p);
        MaterialButton btn720 = sheetView.findViewById(R.id.btn720p);
        MaterialButton btn1080 = sheetView.findViewById(R.id.btn1080p);
        MaterialButton btnOk = sheetView.findViewById(R.id.btnOk);
        MaterialButton btnCancel = sheetView.findViewById(R.id.btnCancel);
        RecyclerView rcSubtitle = sheetView.findViewById(R.id.rcSubtitle);

        MaterialButtonToggleGroup toggleGroup = sheetView.findViewById(R.id.toggleButton);
        toggleGroup.check(getSelectedQuantity());

        if (!itemPlayer.isQuality()) {
            lytQuality.setVisibility(View.GONE);
        }

        if (!itemPlayer.isSubTitle()) {
            lytSubTitles.setVisibility(View.GONE);
        }

        if (itemPlayer.getQuality480().isEmpty()) {
            btn480.setVisibility(View.GONE);
        }

        if (itemPlayer.getQuality720().isEmpty()) {
            btn720.setVisibility(View.GONE);
        }

        if (itemPlayer.getQuality1080().isEmpty()) {
            btn1080.setVisibility(View.GONE);
        }

        SubTitleAdapter subTitleAdapter = new SubTitleAdapter(requireActivity(), itemPlayer.getSubTitles());
        rcSubtitle.setAdapter(subTitleAdapter);
        subTitleAdapter.select(mSubPosition);

        subTitleAdapter.setOnItemClickListener(new RvOnClickListener() {
            @Override
            public void onItemClick(int position) {
                subTitleAdapter.select(position);
            }
        });

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBottomSheetDialog.dismiss();
                playerPosition = player.getCurrentPosition();
                mSubPosition = subTitleAdapter.getSelect();
                mQualityPosition = setSelectedQuantity(toggleGroup.getCheckedButtonId());
                ItemSubTitle itemSubTitle = itemPlayer.getSubTitles().get(mSubPosition);
                boolean isSubTitleOff = itemSubTitle.getSubTitleId().equals("0");
                if (mSubPosition != mSubPreviousPosition || mQualityPosition != mQualityPreviousPosition) {
                    playWithSubtitleQuality(getSelectedQuantityStreamUrl(mQualityPosition), itemSubTitle.getSubTitleUrl(), isSubTitleOff);
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBottomSheetDialog.dismiss();
            }
        });
    }

    private void playWithSubtitleQuality(String qtyUrl, String subTitleUrl, boolean isSubTitleOff) {
        mSubPreviousPosition = mSubPosition;
        mQualityPreviousPosition = mQualityPosition;

        player.stop();
        progressBar.setVisibility(View.VISIBLE);
        btnTryAgain.setVisibility(View.GONE);
        Uri uri = Uri.parse(qtyUrl);
        MediaSource mediaSource;
        if (isSubTitleOff) {
            mediaSource = buildMediaSourceWithAd(uri);
        } else {
            Uri subTitleUri = Uri.parse(subTitleUrl);
            mediaSource = new MergingMediaSource(buildMediaSourceWithAd(uri), buildSubtitleMediaSource(subTitleUri));
        }
        player.setMediaSource(mediaSource);
        player.prepare();
        player.setPlayWhenReady(true);
        player.seekTo(playerPosition);
    }

    private int getSelectedQuantity() {
        int btnId;
        switch (mQualityPosition) {
            case 1:
                btnId = R.id.btn420p;
                break;
            case 2:
                btnId = R.id.btn720p;
                break;
            case 3:
                btnId = R.id.btn1080p;
                break;
            case 0:
            default:
                btnId = R.id.btnDefault;
                break;
        }
        return btnId;
    }

    @SuppressLint("NonConstantResourceId")
    private int setSelectedQuantity(int btnId) {
        int selectedId;
        switch (btnId) {
            case R.id.btn420p:
                selectedId = 1;
                break;
            case R.id.btn720p:
                selectedId = 2;
                break;
            case R.id.btn1080p:
                selectedId = 3;
                break;
            case R.id.btnDefault:
            default:
                selectedId = 0;
                break;
        }
        return selectedId;
    }

    private String getSelectedQuantityStreamUrl(int mQPosition) {
        String videoUrl;
        switch (mQPosition) {
            case 1:
                videoUrl = itemPlayer.getQuality480();
                break;
            case 2:
                videoUrl = itemPlayer.getQuality720();
                break;
            case 3:
                videoUrl = itemPlayer.getQuality1080();
                break;
            case 0:
            default:
                videoUrl = itemPlayer.getDefaultUrl();
                break;
        }
        return videoUrl;
    }

    private void enableDisableEpNext(int mListSize, int selectedEpisode) {
        if (mListSize == 1) {
            isNextEpisode = false;
        } else {
            if (selectedEpisode == mListSize - 1) {
                isNextEpisode = false;
            }
        }
    }

    public void setOnNextPrevClickListener(EpisodeNextPrevListener clickListener) {
        this.episodeNextPrevListener = clickListener;
    }

    private void startNextEpisode() {
        player.createMessage((messageType, payload) -> requireActivity().runOnUiThread(() -> episodeNextPrevListener.onNextClick())).setPosition(0, player.getDuration()).setDeleteAfterDelivery(false).send();
    }

}
