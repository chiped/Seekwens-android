package com.chinmay.seekwens.game.hand;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chinmay.seekwens.R;
import com.chinmay.seekwens.model.Card;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.Query;

public class HandAdapter extends FirebaseRecyclerAdapter<Card, HandCardViewHolder> implements HandCardViewHolder.HandCardClickListener {

    private CardSelectListener cardSelectListener;
    private int selectedIndex = -1;

    public HandAdapter(Query handRef, CardSelectListener cardSelectListener) {
        super(Card.class, R.layout.hand_card_row, HandCardViewHolder.class, handRef);
        this.cardSelectListener = cardSelectListener;
    }

    @Override
    public HandCardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.hand_card_row, parent, false);
        return new HandCardViewHolder(view, this);
    }

    @Override
    protected void populateViewHolder(HandCardViewHolder viewHolder, Card model, int position) {
        viewHolder.bind(model, selectedIndex == position);
    }

    public void cardClicked(int position) {
        if (cardSelectListener != null) {
            cardSelectListener.cardSelected(getItem(position));
            if (selectedIndex != -1) {
                notifyItemChanged(selectedIndex);
            }
            selectedIndex = position;
            notifyItemChanged(selectedIndex);
        }
    }

    interface CardSelectListener {
        void cardSelected(Card card);
    }
}
