package com.chinmay.seekwens.game.board;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chinmay.seekwens.R;

public class BoardAdapter extends RecyclerView.Adapter<BoardCellViewHolder> {

    private final String[] dataSet;

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
        holder.bind(dataSet[position]);
    }

    @Override
    public int getItemCount() {
        return dataSet.length;
    }
}
