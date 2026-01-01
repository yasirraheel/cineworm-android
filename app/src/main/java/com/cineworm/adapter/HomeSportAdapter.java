package com.cineworm.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.cineworm.item.ItemSport;
import com.cineworm.util.PopUpAds;
import com.cineworm.util.RvOnClickListener;
import com.cineworm.videostreamingapp.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class HomeSportAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final ArrayList<ItemSport> dataList;
    private final Context mContext;
    private RvOnClickListener clickListener;

    public HomeSportAdapter(Context context, ArrayList<ItemSport> dataList) {
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
        final ItemSport singleItem = dataList.get(position);
        holder.text.setText(singleItem.getSportName());

        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 0, (int) mContext.getResources().getDimension(R.dimen.item_space), (int) mContext.getResources().getDimension(R.dimen.item_space));
        holder.rootLayout.setLayoutParams(layoutParams);

        if (!singleItem.getSportImage().isEmpty()) {
            Picasso.get().load(singleItem.getSportImage()).placeholder(R.drawable.place_holder_show).into(holder.image);
        }
        holder.rootLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopUpAds.showInterstitialAds(mContext, holder.getBindingAdapterPosition(), clickListener);
            }
        });
        holder.ivPremium.setVisibility(singleItem.isPremium() ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return (null != dataList ? dataList.size() : 0);
    }

    public void setOnItemClickListener(RvOnClickListener clickListener) {
        this.clickListener = clickListener;
    }

    class ItemRowHolder extends RecyclerView.ViewHolder {
        ImageView image, ivPremium;
        TextView text;
        ConstraintLayout rootLayout;

        ItemRowHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            text = itemView.findViewById(R.id.text);
            ivPremium = itemView.findViewById(R.id.ivPremium);
            rootLayout = itemView.findViewById(R.id.rootView);
        }
    }
}
