package com.cineworm.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cineworm.item.FilterType;
import com.cineworm.videostreamingapp.R;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.radiobutton.MaterialRadioButton;

import java.util.ArrayList;


public class FilterAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Activity activity;
    ArrayList<FilterType> listFilterType;

    private final int vCheckBox = 1;
    private final int vRadio = 2;

    public FilterAdapter(Activity activity) {
        this.activity = activity;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setList(ArrayList<FilterType> list) {
        this.listFilterType = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == vCheckBox) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_filter, parent, false);
            return new ViewHolderCb(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_filter_order, parent, false);
            return new ViewHolderRb(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == vCheckBox) {
            ViewHolderCb viewHolderCb = (ViewHolderCb) holder;
            FilterType item = listFilterType.get(position);
            viewHolderCb.cbFilter.setText(item.getFilterName());
            viewHolderCb.cbFilter.setChecked(item.isSelected());
            viewHolderCb.cbFilter.setTag(item);
            viewHolderCb.cbFilter.setOnClickListener(v -> {
                CheckBox cb = (CheckBox) v;
                FilterType filterType = (FilterType) cb.getTag();
                filterType.setSelected(cb.isChecked());
            });
        } else if (holder.getItemViewType() == vRadio) {
            ViewHolderRb viewHolderRb = (ViewHolderRb) holder;
            FilterType item = listFilterType.get(position);
            viewHolderRb.rbFilter.setText(item.getFilterName());
            viewHolderRb.rbFilter.setChecked(item.isSelected());
            viewHolderRb.rbFilter.setTag(item);
            viewHolderRb.rbFilter.setOnClickListener(v -> {
                RadioButton rb = (RadioButton) v;
                FilterType filterType = (FilterType) rb.getTag();
                filterType.setSelected(rb.isChecked());
                for (int i = 0; i < listFilterType.size(); i++) {
                    if (i != position) {
                        FilterType update = listFilterType.get(i);
                        update.setSelected(false);
                        notifyItemChanged(i, update);
                    }
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return (null != listFilterType ? listFilterType.size() : 0);
    }

    public int getItemViewType(int position) {
        return listFilterType.get(position).getFilterType().equals(4) ? vRadio : vCheckBox;
    }


    private static class ViewHolderCb extends RecyclerView.ViewHolder {
        MaterialCheckBox cbFilter;

        public ViewHolderCb(View itemView) {
            super(itemView);
            cbFilter = itemView.findViewById(R.id.cbFilter);
        }
    }

    private static class ViewHolderRb extends RecyclerView.ViewHolder {
        MaterialRadioButton rbFilter;

        public ViewHolderRb(View itemView) {
            super(itemView);
            rbFilter = itemView.findViewById(R.id.rbFilter);
        }
    }
}
