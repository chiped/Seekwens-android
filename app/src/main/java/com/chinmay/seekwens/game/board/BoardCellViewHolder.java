package com.chinmay.seekwens.game.board;

import android.graphics.PorterDuff;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.chinmay.seekwens.R;
import com.chinmay.seekwens.model.Card;
import com.chinmay.seekwens.util.Rules;

import javax.inject.Inject;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.ButterKnife;
import toothpick.Scope;
import toothpick.Toothpick;

public class BoardCellViewHolder extends RecyclerView.ViewHolder {

    private static final String IMAGE_URL = "https://deckofcardsapi.com/static/img/%s.png";

    @BindView(R.id.image) ImageView imageView;
    @BindView(R.id.chip) ImageView chip;
    @BindView(R.id.overlay) View overlay;

    @BindArray(R.array.team_colors) int[] teamColors;

    @Inject Rules rules;

    private BoardCellClickListener clickListener;

    public BoardCellViewHolder(View itemView, BoardCellClickListener clickListener) {
        super(itemView);
        this.clickListener = clickListener;
        ButterKnife.bind(this, itemView);
        final Scope scope = Toothpick.openScope(itemView.getContext().getApplicationContext());
        Toothpick.inject(this, scope);
        overlay.setOnClickListener(new CellClickListenerImpl());
    }

    public void bind(String cardCode, int teamCode, Card selectedCard, int playerTeam) {
        final String url = String.format(IMAGE_URL, cardCode);
        Glide.with(imageView.getContext())
                .load(url)
                .placeholder(R.drawable.ic_card_back)
                .fallback(R.drawable.ic_card_back)
                .into(imageView);

        if (teamCode == -1) {
            chip.setVisibility(View.GONE);
        } else {
            chip.setVisibility(View.VISIBLE);
            chip.setColorFilter(teamColors[teamCode], PorterDuff.Mode.MULTIPLY);
        }

        if (rules.canPlayCardHere(selectedCard, cardCode, teamCode, playerTeam)) {
            overlay.setVisibility(View.VISIBLE);
        } else {
            overlay.setVisibility(View.GONE);
        }
    }

    interface BoardCellClickListener {
        void boardCellClicked(int position);
    }

    private class CellClickListenerImpl implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (clickListener != null) {
                clickListener.boardCellClicked(getAdapterPosition());
            }
        }
    }
}
