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

import com.cineworm.item.ItemHomeContent;
import com.cineworm.item.ItemHomeDisplay;
import com.cineworm.util.PopUpAds;
import com.cineworm.util.RvOnClickListener;
import com.cineworm.videostreamingapp.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class HomeVerticalAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final ArrayList<ItemHomeDisplay> dataList;
    private final Context mContext;
    private RvOnClickListener clickListener;

    public HomeVerticalAdapter(Context context, ArrayList<ItemHomeDisplay> dataList) {
        this.dataList = dataList;
        this.mContext = context;
    }

    @Override
    public int getItemViewType(int position) {
        return dataList.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ItemHomeDisplay.TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_home_section_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_movie_item, parent, false);
            return new ContentViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ItemHomeDisplay item = dataList.get(position);
        
        if (holder instanceof HeaderViewHolder) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            headerHolder.tvSectionTitle.setText(item.getSectionTitle());
        } else if (holder instanceof ContentViewHolder) {
            ContentViewHolder contentHolder = (ContentViewHolder) holder;
            ItemHomeContent content = item.getContent();
            
            if (content != null && !content.getVideoImage().isEmpty()) {
                Picasso.get().load(content.getVideoImage())
                        .placeholder(R.drawable.place_holder_movie)
                        .into(contentHolder.image);
            }
            
            contentHolder.ivPremium.setVisibility(content != null && content.isPremium() ? View.VISIBLE : View.GONE);
            
            contentHolder.rootLayout.setOnClickListener(v -> {
                if (clickListener != null) {
                    // Calculate actual content position (excluding headers)
                    int contentPosition = 0;
                    for (int i = 0; i <= holder.getBindingAdapterPosition(); i++) {
                        if (dataList.get(i).getType() == ItemHomeDisplay.TYPE_CONTENT) {
                            if (i == holder.getBindingAdapterPosition()) {
                                break;
                            }
                            contentPosition++;
                        }
                    }
                    PopUpAds.showInterstitialAds(mContext, contentPosition, clickListener);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return dataList != null ? dataList.size() : 0;
    }

    public void setOnItemClickListener(RvOnClickListener clickListener) {
        this.clickListener = clickListener;
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvSectionTitle;

        HeaderViewHolder(View itemView) {
            super(itemView);
            tvSectionTitle = itemView.findViewById(R.id.tvSectionTitle);
        }
    }

    static class ContentViewHolder extends RecyclerView.ViewHolder {
        ImageView image, ivPremium;
        ConstraintLayout rootLayout;

        ContentViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            ivPremium = itemView.findViewById(R.id.ivPremium);
            rootLayout = itemView.findViewById(R.id.rootView);
        }
    }
}
