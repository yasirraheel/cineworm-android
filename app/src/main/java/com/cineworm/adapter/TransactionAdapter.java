package com.cineworm.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cineworm.item.ItemTransaction;
import com.cineworm.videostreamingapp.R;

import java.util.ArrayList;

public class TransactionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final ArrayList<ItemTransaction> dataList;
    private final Context mContext;

    public TransactionAdapter(Context context, ArrayList<ItemTransaction> dataList) {
        this.dataList = dataList;
        this.mContext = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_transaction_item, parent, false);
        return new ItemRowHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, final int position) {
        final ItemRowHolder holder = (ItemRowHolder) viewHolder;
        final ItemTransaction singleItem = dataList.get(position);
        holder.tvPlanName.setText(singleItem.getPlanName());
        holder.tvPlanAmount.setText(mContext.getString(R.string.transaction_amount, singleItem.getPlanAmount(), singleItem.getPaymentCurrency()));
        holder.tvPaymentGateway.setText(singleItem.getPaymentGateway());
        holder.tvPaymentId.setText(singleItem.getPaymentId());
        holder.tvPaymentDate.setText(singleItem.getPaymentDate());
    }

    @Override
    public int getItemCount() {
        return (null != dataList ? dataList.size() : 0);
    }

    static class ItemRowHolder extends RecyclerView.ViewHolder {
        TextView tvPlanName, tvPlanAmount, tvPaymentGateway, tvPaymentId, tvPaymentDate;

        ItemRowHolder(View itemView) {
            super(itemView);
            tvPlanName = itemView.findViewById(R.id.tvPlanName);
            tvPlanAmount = itemView.findViewById(R.id.tvPlanAmount);
            tvPaymentGateway = itemView.findViewById(R.id.tvPaymentGateway);
            tvPaymentId = itemView.findViewById(R.id.tvPaymentId);
            tvPaymentDate = itemView.findViewById(R.id.tvPaymentDate);
        }
    }
}
