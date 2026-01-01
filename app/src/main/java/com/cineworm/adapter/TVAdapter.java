package com.cineworm.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.cineworm.item.ItemTV;
import com.cineworm.util.PopUpAds;
import com.cineworm.util.RvOnClickListener;
import com.cineworm.videostreamingapp.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class TVAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final ArrayList<ItemTV> dataList;
    private final Context mContext;
    private RvOnClickListener clickListener;
    private final int VIEW_TYPE_LOADING = 0;
    private final int VIEW_TYPE_ITEM = 1;

    public TVAdapter(Context context, ArrayList<ItemTV> dataList) {
        this.dataList = dataList;
        this.mContext = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_sport_item, parent, false);
            return new ItemRowHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_loading_item, parent, false);
            return new ProgressViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, final int position) {
        if (viewHolder.getItemViewType() == VIEW_TYPE_ITEM) {
            final ItemRowHolder holder = (ItemRowHolder) viewHolder;
            final ItemTV singleItem = dataList.get(position);
            holder.text.setText(singleItem.getTvName());
            if (!singleItem.getTvImage().isEmpty()) {
                Picasso.get().load(singleItem.getTvImage()).placeholder(R.drawable.place_holder_show).into(holder.image);
            }
            holder.rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopUpAds.showInterstitialAds(mContext, holder.getBindingAdapterPosition(), clickListener);
                }
            });

            holder.ivPremium.setVisibility(singleItem.isPremium() ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return (null != dataList ? dataList.size() + 1 : 0);
    }

    public void hideHeader() {
        ProgressViewHolder.progressBar.setVisibility(View.GONE);
    }

    private boolean isHeader(int position) {
        return position == dataList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return isHeader(position) ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    public void setOnItemClickListener(RvOnClickListener clickListener) {
        this.clickListener = clickListener;
    }

    class ItemRowHolder extends RecyclerView.ViewHolder {
        ImageView image, ivPremium;
        TextView text;
        ConstraintLayout rootView;

        ItemRowHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            text = itemView.findViewById(R.id.text);
            ivPremium = itemView.findViewById(R.id.ivPremium);
            rootView = itemView.findViewById(R.id.rootView);
        }
    }

    static class ProgressViewHolder extends RecyclerView.ViewHolder {
        static ProgressBar progressBar;

        ProgressViewHolder(View v) {
            super(v);
            progressBar = v.findViewById(R.id.progressBar);
        }
    }
}
