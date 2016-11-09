package com.chinmay.seekwens.game;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.chinmay.seekwens.R;
import com.chinmay.seekwens.model.Card;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HandCardViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.image) ImageView imageView;

    public HandCardViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void bind(Card card) {
        Glide.with(imageView.getContext()).load(card.image).into(imageView);
    }
}
