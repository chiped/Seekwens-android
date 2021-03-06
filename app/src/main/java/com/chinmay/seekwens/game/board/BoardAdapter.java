package com.chinmay.seekwens.game.board;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chinmay.seekwens.R;
import com.chinmay.seekwens.model.Card;

import java.util.List;

public class BoardAdapter extends RecyclerView.Adapter<BoardCellViewHolder> implements BoardCellViewHolder.BoardCellClickListener {

    private final String[] dataSet;
    private List<Long> chips;
    private Card selectedCard;
    private CellSelectListener cellSelectListener;
    private int playerTeam = -1;

    public BoardAdapter(String[] dataSet, CellSelectListener cellSelectListener) {
        this.dataSet = dataSet;
        this.cellSelectListener = cellSelectListener;
    }

    @Override
    public BoardCellViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.board_cell_layout, parent, false);
        return new BoardCellViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(BoardCellViewHolder holder, int position) {
        holder.bind(dataSet[position],
                chips == null ? -1 : chips.get(position).intValue(),
                selectedCard,
                playerTeam);
    }

    @Override
    public int getItemCount() {
        return dataSet.length;
    }

    public void setChips(List<Long> chips) {
        this.chips = chips;
    }

    public void highlightCard(Card card) {
        selectedCard = card;
        notifyDataSetChanged();
    }

    @Override
    public void boardCellClicked(int position) {
        if (cellSelectListener != null) {
            cellSelectListener.cellClicked(position);
            selectedCard = null;
        }

    }

    public void setPlayerTeam(int playerTeam) {
        this.playerTeam = playerTeam;
    }

    public List<Long> getChips() {
        return chips;
    }

    interface CellSelectListener {
        void cellClicked(int position);
    }
}
