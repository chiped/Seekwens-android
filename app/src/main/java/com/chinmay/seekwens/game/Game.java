package com.chinmay.seekwens.game;

import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.chinmay.seekwens.R;
import com.chinmay.seekwens.ui.BaseSeeKwensActivity;
import com.f2prateek.dart.InjectExtra;

import butterknife.BindView;

public class Game extends BaseSeeKwensActivity {

    @BindView(R.id.bottom_sheet_hand) View bottomSheet;
    @BindView(R.id.hand_recyclerview) RecyclerView handRecycler;

    @InjectExtra String gameId;

    @Override
    protected int getlayoutId() {
        return R.layout.game_layout;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.game_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.show_hand) {
            BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
        }
        return true;
    }
}
