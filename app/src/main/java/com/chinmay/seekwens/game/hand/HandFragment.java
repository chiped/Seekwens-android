package com.chinmay.seekwens.game.hand;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.chinmay.seekwens.R;
import com.chinmay.seekwens.database.FireBaseUtils;
import com.chinmay.seekwens.ui.BaseSeeKwensFragment;
import com.f2prateek.dart.InjectExtra;
import com.google.firebase.database.Query;

import javax.inject.Inject;

import butterknife.BindView;

import static android.content.Context.MODE_PRIVATE;
import static com.chinmay.seekwens.SeeKwensApplication.PREFS;
import static com.chinmay.seekwens.SeeKwensApplication.USER_ID_KEY;

public class HandFragment extends BaseSeeKwensFragment {

    @BindView(R.id.hand_recycler) RecyclerView handRecycler;
    @BindView(R.id.player_turn) TextView playerTurn;

    @InjectExtra String gameId;

    @Inject FireBaseUtils fireBaseUtils;

    private String playerId;
    private HandAdapter handAdapter;
    private float offset;

    @Override
    protected int getLayoutId() {
        return R.layout.hand_bottom_sheet;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        playerId = getActivity().getApplicationContext().getSharedPreferences(PREFS, MODE_PRIVATE).getString(USER_ID_KEY, null);

        final Query handRef = fireBaseUtils.getHandRef(gameId, playerId);
        handAdapter = new HandAdapter(handRef);
        handRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        handRecycler.setAdapter(handAdapter);
        handRecycler.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                if (parent.getChildAdapterPosition(view) == 0 ) {
                    return;
                }
                final float width = getResources().getDimension(R.dimen.card_width);
                final int leftOffset = (int) (0.75 * width * (1 - offset));
                outRect.set(-leftOffset, 0, 0, 0);
            }
        });
    }

    public void setOffset(float offset) {
        this.offset = Math.max(0, offset);
        if (handRecycler != null) {
            handRecycler.getAdapter().notifyDataSetChanged();
        }
    }
}
