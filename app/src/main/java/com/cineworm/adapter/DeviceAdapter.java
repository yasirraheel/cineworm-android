package com.cineworm.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cineworm.item.ItemDevice;
import com.cineworm.util.RvOnClickListener;
import com.cineworm.videostreamingapp.R;

import java.util.ArrayList;

/**
 * Created by laxmi.
 */
public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ItemRowHolder> {

    private ArrayList<ItemDevice> dataList;
    private Context mContext;
    private RvOnClickListener clickListener;

    public DeviceAdapter(Context context, ArrayList<ItemDevice> dataList) {
        this.dataList = dataList;
        this.mContext = context;
    }

    @NonNull
    @Override
    public ItemRowHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_device_item, parent, false);
        return new ItemRowHolder(v);
    }

    @Override
    public void onBindViewHolder(ItemRowHolder holder, final int position) {
        final ItemDevice singleItem = dataList.get(position);
        holder.textDeviceName.setText(singleItem.getDeviceName());
        if (singleItem.isSameUser()) {
            holder.textLogoutNow.setVisibility(View.GONE);
        } else {
            holder.textLogoutNow.setVisibility(View.VISIBLE);
        }
        holder.textLogoutNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickListener.onItemClick(holder.getBindingAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return (null != dataList ? dataList.size() : 0);
    }

    public static class ItemRowHolder extends RecyclerView.ViewHolder {
        public TextView textDeviceName, textLogoutNow;

        public ItemRowHolder(View itemView) {
            super(itemView);
            textDeviceName = itemView.findViewById(R.id.text);
            textLogoutNow = itemView.findViewById(R.id.textLogout);
        }
    }

    public void onClickOnLogout(RvOnClickListener onDevice) {
        this.clickListener = onDevice;
    }
}
