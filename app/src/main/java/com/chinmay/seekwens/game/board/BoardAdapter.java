package com.chinmay.seekwens.game.board;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chinmay.seekwens.R;

import java.util.List;

public class BoardAdapter extends RecyclerView.Adapter<BoardCellViewHolder> {

    private final String[] dataSet;
    private List<Long> chips;

    public BoardAdapter(String[] dataSet) {
        this.dataSet = dataSet;
    }

    @Override
    public BoardCellViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.board_cell_layout, parent, false);
        return new BoardCellViewHolder(view);
    }

    @Override
    public void onBindViewHolder(BoardCellViewHolder holder, int position) {
        holder.bind(dataSet[position], chips == null ? -1 : chips.get(position).intValue());
    }

    @Override
    public int getItemCount() {
        return dataSet.length;
    }

    public void setChips(List<Long> chips) {
        this.chips = chips;
    }
}
