package com.chinmay.seekwens.game.hand;

import com.chinmay.seekwens.R;
import com.chinmay.seekwens.model.Card;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.Query;

public class HandAdapter extends FirebaseRecyclerAdapter<Card, HandCardViewHolder> {

    public HandAdapter(Query handRef) {
        super(Card.class, R.layout.hand_card_row, HandCardViewHolder.class, handRef);
    }

    @Override
    protected void populateViewHolder(HandCardViewHolder viewHolder, Card model, int position) {
        viewHolder.bind(model);
    }
}
