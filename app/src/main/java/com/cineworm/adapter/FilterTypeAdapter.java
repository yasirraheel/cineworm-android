package com.cineworm.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cineworm.item.FilterType;
import com.cineworm.util.RvTOnClickListener;
import com.cineworm.videostreamingapp.R;

import java.util.ArrayList;

public class FilterTypeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Activity activity;
    ArrayList<FilterType> listType;
    RvTOnClickListener<FilterType> clickListener;
    private int row_index = -1;

    public FilterTypeAdapter(Activity activity, ArrayList<FilterType> listType) {
        this.activity = activity;
        this.listType = listType;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_filter_type, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder) holder;
        FilterType filterType = listType.get(position);
        viewHolder.tvFilterTypeName.setText(filterType.getFilterName());
        viewHolder.tvFilterTypeName.setOnClickListener(view -> clickListener.onItemClick(filterType, position));
        if (row_index > -1) {
            if (row_index == position) {
                viewHolder.tvFilterTypeName.setBackgroundColor(activity.getResources().getColor(R.color.bottom_menu_divider));
                viewHolder.tvFilterTypeName.setTextColor(activity.getResources().getColor(R.color.text));
            } else {
                viewHolder.tvFilterTypeName.setBackgroundColor(Color.TRANSPARENT);
                viewHolder.tvFilterTypeName.setTextColor(activity.getResources().getColor(R.color.text_sub));
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void select(int position) {
        row_index = position;
        notifyDataSetChanged();
    }

    public void selectFirstByDefault() {
        FilterType filterType = listType.get(0);
        clickListener.onItemClick(filterType, 0);
    }

    @Override
    public int getItemCount() {
        return listType.size();
    }

    public void setOnItemClickListener(RvTOnClickListener<FilterType> clickListener) {
        this.clickListener = clickListener;
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFilterTypeName;

        public ViewHolder(View itemView) {
            super(itemView);
            tvFilterTypeName = itemView.findViewById(R.id.tvFilterTypeName);
        }
    }
}
