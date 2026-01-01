package com.cineworm.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.cineworm.item.ItemHomeContent;
import com.cineworm.util.PopUpAds;
import com.cineworm.util.RvOnClickListener;
import com.cineworm.videostreamingapp.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class HomeContentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final ArrayList<ItemHomeContent> dataList;
    private final Context mContext;
    private RvOnClickListener clickListener;
    private final boolean isHomeMore;
    private final int VIEW_TYPE_MOVIE = 0;
    private final int VIEW_TYPE_SHOW = 1; // sport and tv also have same layout so

    public HomeContentAdapter(Context context, ArrayList<ItemHomeContent> dataList, boolean isHomeMore) {
        this.dataList = dataList;
        this.mContext = context;
        this.isHomeMore = isHomeMore;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_MOVIE:
            default:
                View vMovie = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_movie_item, parent, false);
                return new MovieItemRowHolder(vMovie);
            case VIEW_TYPE_SHOW:
                View vShow = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_show_item, parent, false);
                return new ShowItemRowHolder(vShow);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, final int position) {
        if (viewHolder.getItemViewType() == VIEW_TYPE_MOVIE) {
            final MovieItemRowHolder holder = (MovieItemRowHolder) viewHolder;
            final ItemHomeContent singleItem = dataList.get(position);

            if (!isHomeMore) {
                ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(0, 0, (int) mContext.getResources().getDimension(R.dimen.item_space), (int) mContext.getResources().getDimension(R.dimen.item_space));
                holder.rootLayout.setLayoutParams(layoutParams);
            }
            if (!singleItem.getVideoImage().isEmpty()) {
                Picasso.get().load(singleItem.getVideoImage()).placeholder(R.drawable.place_holder_movie).into(holder.image);
            }
            holder.rootLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopUpAds.showInterstitialAds(mContext, holder.getBindingAdapterPosition(), clickListener);
                }
            });

            holder.ivPremium.setVisibility(singleItem.isPremium() ? View.VISIBLE : View.GONE);

        } else if (viewHolder.getItemViewType() == VIEW_TYPE_SHOW) {
            final ShowItemRowHolder holder = (ShowItemRowHolder) viewHolder;
            final ItemHomeContent singleItem = dataList.get(position);

            if (!isHomeMore) {
                ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(0, 0, (int) mContext.getResources().getDimension(R.dimen.item_space), (int) mContext.getResources().getDimension(R.dimen.item_space));
                holder.rootLayout.setLayoutParams(layoutParams);
            }
            if (!singleItem.getVideoImage().isEmpty()) {
                Picasso.get().load(singleItem.getVideoImage()).placeholder(R.drawable.place_holder_show).into(holder.image);
            }
            holder.rootLayout.setOnClickListener(new View.OnClickListener() {
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
        return (null != dataList ? dataList.size() : 0);
    }

    @Override
    public int getItemViewType(int position) {
        switch (dataList.get(position).getHomeType()) {
            case "Movie":
            default:
                return VIEW_TYPE_MOVIE;
            case "Shows":
            case "Sports":
            case "LiveTV":
            case "Recent":
                return VIEW_TYPE_SHOW;
        }
    }

    public void setOnItemClickListener(RvOnClickListener clickListener) {
        this.clickListener = clickListener;
    }

    static class MovieItemRowHolder extends RecyclerView.ViewHolder {
        ImageView image, ivPremium;
        ConstraintLayout rootLayout;

        MovieItemRowHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            ivPremium = itemView.findViewById(R.id.ivPremium);
            rootLayout = itemView.findViewById(R.id.rootView);
        }
    }

    static class ShowItemRowHolder extends RecyclerView.ViewHolder {
        ImageView image, ivPremium;
        ConstraintLayout rootLayout;

        ShowItemRowHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            ivPremium = itemView.findViewById(R.id.ivPremium);
            rootLayout = itemView.findViewById(R.id.rootView);
        }
    }
}
