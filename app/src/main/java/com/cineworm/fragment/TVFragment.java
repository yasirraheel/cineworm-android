package com.cineworm.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cineworm.adapter.TVAdapter;
import com.cineworm.item.FilterType;
import com.cineworm.item.ItemTV;
import com.cineworm.util.API;
import com.cineworm.util.Constant;
import com.cineworm.util.EndlessRecyclerViewScrollListener;
import com.cineworm.util.FilterUtil;
import com.cineworm.util.NetworkUtils;
import com.cineworm.videostreamingapp.R;
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

public class TVFragment extends Fragment {

    private ArrayList<ItemTV> mListItem;
    private RecyclerView recyclerView;
    private TVAdapter adapter;
    private ProgressBar progressBar;
    private LinearLayout lyt_not_found;
    private boolean isFirst = true, isLoadMore = false;
    private int pageIndex = 1;
    private EndlessRecyclerViewScrollListener endlessRecyclerViewScrollListener;
    String filterData = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.row_recyclerview, container, false);

        mListItem = new ArrayList<>();
        lyt_not_found = rootView.findViewById(R.id.lyt_not_found);
        progressBar = rootView.findViewById(R.id.progressBar);
        recyclerView = rootView.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        int spanCount = getResources().getInteger(R.integer.showColumn);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), spanCount);
        recyclerView.setLayoutManager(layoutManager);

        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                switch (adapter.getItemViewType(position)) {
                    case 0:
                        return spanCount;
                    default:
                        return 1;
                }
            }
        });

        if (NetworkUtils.isConnected(getActivity())) {
            getTV();
        } else {
            Toast.makeText(getActivity(), getString(R.string.conne_msg1), Toast.LENGTH_SHORT).show();
        }


        endlessRecyclerViewScrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                if (isLoadMore) {
                    new Handler().postDelayed(() -> {
                        pageIndex++;
                        getTV();
                    }, 1000);
                } else {
                    adapter.hideHeader();
                }
            }
        };

        recyclerView.addOnScrollListener(endlessRecyclerViewScrollListener);

        return rootView;
    }

    private void getTV() {

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        if (filterData.isEmpty()) {
            jsObj.addProperty("cat_id", "");
            jsObj.addProperty("filter", Constant.FILTER_NEWEST);
        } else {
            FilterUtil.jsonForParameter(FilterUtil.jsonToSelectList(filterData), jsObj, FilterType.TV);
        }
        Log.e("jsObj", "" + jsObj);
        params.put("data", API.toBase64(jsObj.toString()));
        params.put("page", pageIndex);
        client.post(Constant.TV_FILTER_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                if (isFirst)
                    showProgress(true);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if (isFirst)
                    showProgress(false);

                String result = new String(responseBody);
                try {
                    JSONObject mainJson = new JSONObject(result);
                    isLoadMore = mainJson.getBoolean(Constant.LOAD_MORE);
                    JSONArray jsonArray = mainJson.getJSONArray(Constant.ARRAY_NAME);
                    JSONObject objJson;
                    if (jsonArray.length() > 0) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            objJson = jsonArray.getJSONObject(i);
                            if (objJson.has(Constant.STATUS)) {
                                lyt_not_found.setVisibility(View.VISIBLE);
                            } else {
                                ItemTV objItem = new ItemTV();
                                objItem.setTvId(objJson.getString(Constant.TV_ID));
                                objItem.setTvName(objJson.getString(Constant.TV_TITLE));
                                objItem.setTvImage(objJson.getString(Constant.TV_IMAGE));
                                objItem.setPremium(objJson.getString(Constant.TV_ACCESS).equals("Paid"));
                                mListItem.add(objItem);
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                displayData();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                showProgress(false);
                lyt_not_found.setVisibility(View.VISIBLE);
            }

        });
    }

    private void displayData() {
        if (mListItem.size() == 0) {
            lyt_not_found.setVisibility(View.VISIBLE);
        } else {

            lyt_not_found.setVisibility(View.GONE);
            if (isFirst) {
                isFirst = false;
                adapter = new TVAdapter(getActivity(), mListItem);
                recyclerView.setAdapter(adapter);
            } else {
                adapter.notifyDataSetChanged();
            }

            adapter.setOnItemClickListener(position -> {
                String sportId = mListItem.get(position).getTvId();
                Intent intent = new Intent(getActivity(), TVDetailsActivity.class);
                intent.putExtra("Id", sportId);
                startActivity(intent);
            });
        }
    }


    private void showProgress(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            lyt_not_found.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    public void applyFilter() {
        endlessRecyclerViewScrollListener.resetState();
        mListItem.clear();
        isFirst = true;
        isLoadMore = false;
        pageIndex = 1;

        if (NetworkUtils.isConnected(getActivity())) {
            getTV();
        } else {
            Toast.makeText(getActivity(), getString(R.string.conne_msg1), Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MenuHost menuHost = requireActivity();
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_filter, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.menu_filter) {
                    Bundle args = new Bundle();
                    args.putInt("filterTypeSec", FilterType.TV);
                    args.putString("filterData", filterData);
                    FilterBottomFragment filterBottomFragment = new FilterBottomFragment();
                    filterBottomFragment.setArguments(args);
                    filterBottomFragment.show(requireActivity().getSupportFragmentManager(), filterBottomFragment.getTag());
                    filterBottomFragment.setOnFilterButtonClickListener(jsonList -> {
                        filterData = jsonList;
                        applyFilter();
                    });
                }
                return true;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }
}

