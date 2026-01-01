package com.cineworm.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.cineworm.item.ItemWatchList;
import com.cineworm.util.PopUpAds;
import com.cineworm.util.RvOnClickListener;
import com.cineworm.videostreamingapp.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class WatchListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final ArrayList<ItemWatchList> dataList;
    private final Context mContext;
    private RvOnClickListener clickListener;

    public WatchListAdapter(Context context, ArrayList<ItemWatchList> dataList) {
        this.dataList = dataList;
        this.mContext = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_sport_item, parent, false);
        return new ItemRowHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, final int position) {
        final ItemRowHolder holder = (ItemRowHolder) viewHolder;
        final ItemWatchList singleItem = dataList.get(position);
        holder.text.setText(singleItem.getPostTitle());
        holder.text.setSelected(true);
        holder.text.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        if (!singleItem.getPostImage().isEmpty()) {
            Picasso.get().load(singleItem.getPostImage()).placeholder(R.drawable.place_holder_show).into(holder.image);
        }
        holder.rootLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopUpAds.showInterstitialAds(mContext, holder.getBindingAdapterPosition(), clickListener);
            }
        });

    }

    @Override
    public int getItemCount() {
        return (null != dataList ? dataList.size() : 0);
    }

    public void setOnItemClickListener(RvOnClickListener clickListener) {
        this.clickListener = clickListener;
    }

    class ItemRowHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView text;
        ConstraintLayout rootLayout;

        ItemRowHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            text = itemView.findViewById(R.id.text);
            rootLayout = itemView.findViewById(R.id.rootView);
        }
    }

}
