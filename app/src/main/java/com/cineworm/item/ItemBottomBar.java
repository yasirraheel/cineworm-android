package com.cineworm.item;

import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cineworm.videostreamingapp.R;

import java.util.ArrayList;

public class ItemBottomBar {
    TextView textView;
    ImageView imageView;

    public TextView getTextView() {
        return textView;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public ItemBottomBar(TextView textView, ImageView imageView) {
        this.textView = textView;
        this.imageView = imageView;
    }

    public static ArrayList<ItemBottomBar> listOfBottomBarItem(ViewGroup v) {
        ArrayList<ItemBottomBar> list = new ArrayList<>();
        list.add(new ItemBottomBar(v.findViewById(R.id.tvHome), v.findViewById(R.id.ivHome)));
        list.add(new ItemBottomBar(v.findViewById(R.id.tvWatchlist), v.findViewById(R.id.ivWatchlist)));
        list.add(new ItemBottomBar(v.findViewById(R.id.tvAccount), v.findViewById(R.id.ivAccount)));
        list.add(new ItemBottomBar(v.findViewById(R.id.tvSetting), v.findViewById(R.id.ivSetting)));
        return list;
    }
}
