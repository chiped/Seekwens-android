package com.chinmay.seekwens.game.hand;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.chinmay.seekwens.R;
import com.chinmay.seekwens.model.Card;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;

public class HandCardViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.image) ImageView imageView;
    @BindView(R.id.overlay) View overlay;

    private final HandCardClickListener clickListener;

    public HandCardViewHolder(View itemView, HandCardClickListener clickListener) {
        super(itemView);
        this.clickListener = clickListener;
        ButterKnife.bind(this, itemView);
        itemView.setOnClickListener(new CardClickListenerImpl());
    }

    public void bind(Card card, boolean selected) {
        Glide.with(imageView.getContext()).load(card.image).placeholder(R.drawable.ic_card_back).into(imageView);
        overlay.setVisibility(selected ? View.VISIBLE : View.GONE);
    }

    interface HandCardClickListener {
        void cardClicked(int position);
    }

    private class CardClickListenerImpl implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (clickListener != null) {
                clickListener.cardClicked(getAdapterPosition());
            }
        }
    }
}
