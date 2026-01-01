package com.cineworm.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.cineworm.adapter.FilterAdapter;
import com.cineworm.adapter.FilterTypeAdapter;
import com.cineworm.item.FilterType;
import com.cineworm.util.API;
import com.cineworm.util.Constant;
import com.cineworm.util.FilterUtil;
import com.cineworm.videostreamingapp.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
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

public class FilterBottomFragment extends BottomSheetDialogFragment {
    FilterTypeAdapter filterTypeAdapter;
    public ArrayList<FilterType> filterTypeList;
    FilterAdapter filterAdapter;
    FilterButtonOnClickListener filterButtonOnClickListener;
    int filterTypeSec;
    String filterData;
    LinearLayout progressLayout, rootLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_filter_sheet, container, false);

        boolean isRTL = Boolean.parseBoolean(getString(R.string.isRTL));
        if (isRTL) {
            requireActivity().getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
        if (getArguments() != null) {
            filterTypeSec = getArguments().getInt("filterTypeSec");
            filterData = getArguments().getString("filterData");
        }
        filterTypeList = new ArrayList<>();
        progressLayout = rootView.findViewById(R.id.progressLayout);
        rootLayout = rootView.findViewById(R.id.rootLayout);
        RecyclerView rvFilter = rootView.findViewById(R.id.rvFilter);
        RecyclerView rvFilterType = rootView.findViewById(R.id.rvFilterType);
        MaterialButton btnApplyFilter = rootView.findViewById(R.id.btnApplyFilter);
        MaterialButton btnClearFilter = rootView.findViewById(R.id.btnClearFilter);
        ImageView ivClose = rootView.findViewById(R.id.ivClose);

        filterAdapter = new FilterAdapter(getActivity());
        rvFilter.setAdapter(filterAdapter);

        filterTypeAdapter = new FilterTypeAdapter(requireActivity(), FilterType.getFilterTypeListBySec(filterTypeSec));
        rvFilterType.setAdapter(filterTypeAdapter);
        filterTypeAdapter.setOnItemClickListener((item, position) -> {
            filterTypeAdapter.select(position);
            filterAdapter.setList(getFilterTypeList(item.getFilterType()));
            rvFilter.smoothScrollToPosition(0);
        });

        if (filterData.isEmpty()) {
            fillList();
        } else {
            showProgress(false);
            filterTypeList = FilterUtil.jsonToList(filterData);
            filterTypeAdapter.selectFirstByDefault();
        }

        btnApplyFilter.setOnClickListener(view -> {
            filterData = FilterUtil.listToJson(filterTypeList);
            filterButtonOnClickListener.onButtonClick(filterData);
            dismiss();
        });
        ivClose.setOnClickListener(view -> dismiss());
        btnClearFilter.setOnClickListener(view -> resetFilter());
        return rootView;
    }

    private void fillList() {

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        params.put("data", API.toBase64(jsObj.toString()));

        client.post(Constant.FILTER_LIST_URL, params, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();
                showProgress(true);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                showProgress(false);

                String result = new String(responseBody);
                try {
                    JSONObject mainJson = new JSONObject(result);
                    JSONObject liveTVJson = mainJson.getJSONObject(Constant.ARRAY_NAME);

                    JSONArray langArray = liveTVJson.getJSONArray("language");
                    for (int i = 0; i < langArray.length(); i++) {
                        JSONObject jsonObject = langArray.getJSONObject(i);
                        filterTypeList.add(new FilterType(jsonObject.getString("language_id"),
                                jsonObject.getString("language_name"), FilterType.TY_LANGUAGE, false));
                    }

                    JSONArray genreArray = liveTVJson.getJSONArray("genre");
                    for (int i = 0; i < genreArray.length(); i++) {
                        JSONObject jsonObject = genreArray.getJSONObject(i);
                        filterTypeList.add(new FilterType(jsonObject.getString("genre_id"),
                                jsonObject.getString("genre_name"), FilterType.TY_GENRE, false));
                    }

                    JSONArray sportArray = liveTVJson.getJSONArray("sports_category");
                    for (int i = 0; i < sportArray.length(); i++) {
                        JSONObject jsonObject = sportArray.getJSONObject(i);
                        filterTypeList.add(new FilterType(jsonObject.getString("category_id"),
                                jsonObject.getString("category_name"), FilterType.TY_CATEGORY_SP, false));
                    }

                    JSONArray tvArray = liveTVJson.getJSONArray("tv_category");
                    for (int i = 0; i < tvArray.length(); i++) {
                        JSONObject jsonObject = tvArray.getJSONObject(i);
                        filterTypeList.add(new FilterType(jsonObject.getString("category_id"),
                                jsonObject.getString("category_name"), FilterType.TY_CATEGORY_TV, false));
                    }

                    filterTypeList.addAll(listOfOrderType());
                    filterTypeAdapter.selectFirstByDefault();

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(requireActivity(), getString(R.string.no_item_found), Toast.LENGTH_SHORT).show();
                    dismiss();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(requireActivity(), getString(R.string.no_item_found), Toast.LENGTH_SHORT).show();
                dismiss();
            }
        });
    }

    public ArrayList<FilterType> listOfOrderType() {
        ArrayList<FilterType> list = new ArrayList<>();
        list.add(new FilterType(Constant.FILTER_NEWEST, getString(R.string.filter_newest), FilterType.TY_ORDER_TYPE, true));
        list.add(new FilterType(Constant.FILTER_OLDEST, getString(R.string.filter_oldest), FilterType.TY_ORDER_TYPE, false));
        list.add(new FilterType(Constant.FILTER_ALPHA, getString(R.string.filter_alpha), FilterType.TY_ORDER_TYPE, false));
        list.add(new FilterType(Constant.FILTER_RANDOM, getString(R.string.filter_random), FilterType.TY_ORDER_TYPE, false));
        return list;
    }

    private void showProgress(boolean show) {
        progressLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        rootLayout.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private ArrayList<FilterType> getFilterTypeList(String type) {
        ArrayList<FilterType> list = new ArrayList<>();
        for (FilterType itemFilterType : filterTypeList) {
            if (itemFilterType.getFilterType().equals(type)) {
                list.add(itemFilterType);
            }
        }
        return list;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void resetFilter() {
        for (int i = 0; i < filterTypeList.size(); i++) {
            FilterType filterType = filterTypeList.get(i);
            if (filterType.isSelected()) {
                filterType.setSelected(false);
                filterTypeList.set(i, filterType);
            }
            if (filterType.getFilterId().equals(Constant.FILTER_NEWEST)) {
                filterType.setSelected(true);//by default it is true so
                filterTypeList.set(i, filterType);
            }
        }
        filterAdapter.notifyDataSetChanged();
    }

    public void setOnFilterButtonClickListener(FilterButtonOnClickListener clickListener) {
        this.filterButtonOnClickListener = clickListener;
    }

    public interface FilterButtonOnClickListener {
        void onButtonClick(String jsonList);

    }

    @Override
    public int getTheme() {
        return R.style.BottomSheetDialog;
    }

}
