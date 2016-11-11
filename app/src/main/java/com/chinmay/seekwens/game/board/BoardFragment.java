package com.chinmay.seekwens.game.board;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.chinmay.seekwens.R;
import com.chinmay.seekwens.ui.BaseSeeKwensFragment;
import com.chinmay.seekwens.util.GameUtil;

import javax.inject.Inject;

import butterknife.BindView;

public class BoardFragment extends BaseSeeKwensFragment {

    @BindView(R.id.board_recycler) RecyclerView boardRecycler;

    @Inject GameUtil gameUtil;

    private BoardAdapter boardAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.board_fragment;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        boardAdapter = new BoardAdapter(gameUtil.getBoard());
        final BoardLayoutManager layout = new BoardLayoutManager();
        layout.setTotalColumnCount(10);
        boardRecycler.setLayoutManager(layout);
        boardRecycler.setAdapter(boardAdapter);
    }
}
