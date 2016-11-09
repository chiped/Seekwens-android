package com.chinmay.seekwens.game;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
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

    @InjectExtra String gameId;

    private HandDialogFragment handDialogFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpHand();
    }

    private void setUpHand() {
        handDialogFragment = new HandDialogFragment();
        final Bundle bundle = new Bundle();
        bundle.putString("gameId", gameId);
        handDialogFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().add(R.id.hand_content, handDialogFragment).commit();

        BottomSheetBehavior.from(bottomSheet).setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                handDialogFragment.setOffset(slideOffset);
            }
        });
    }

    @Override
    protected int getlayoutId() {
        return R.layout.game_layout;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater inflater = getMenuInflater();
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
