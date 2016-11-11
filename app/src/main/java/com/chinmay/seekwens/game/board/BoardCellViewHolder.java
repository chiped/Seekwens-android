package com.chinmay.seekwens.game.board;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.chinmay.seekwens.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BoardCellViewHolder extends RecyclerView.ViewHolder {

    private static final String IMAGE_URL = "https://deckofcardsapi.com/static/img/%s.png";

    @BindView(R.id.image) ImageView imageView;

    public BoardCellViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void bind(String cardCode) {
        final String url = String.format(IMAGE_URL, cardCode);
        Glide.with(imageView.getContext())
                .load(url)
                .placeholder(R.drawable.ic_card_back)
                .fallback(R.drawable.ic_card_back)
                .into(imageView);
    }
}
