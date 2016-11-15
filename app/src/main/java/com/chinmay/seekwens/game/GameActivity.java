package com.chinmay.seekwens.game;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.chinmay.seekwens.R;
import com.chinmay.seekwens.game.board.BoardFragment;
import com.chinmay.seekwens.game.board.CellListener;
import com.chinmay.seekwens.game.hand.HandFragment;
import com.chinmay.seekwens.game.hand.HandListener;
import com.chinmay.seekwens.model.Card;
import com.chinmay.seekwens.model.GameState;
import com.chinmay.seekwens.ui.BaseSeeKwensActivity;
import com.chinmay.seekwens.ui.SeeKwensDialogFragment;
import com.chinmay.seekwens.util.GameUtil;
import com.f2prateek.dart.InjectExtra;

import javax.inject.Inject;

import butterknife.BindView;
import rx.Subscription;
import rx.functions.Action1;

public class GameActivity extends BaseSeeKwensActivity implements HandListener, CellListener {

    @BindView(R.id.bottom_sheet_hand) View bottomSheet;
    @BindView(R.id.floating_play_button) FloatingActionButton playButton;

    @InjectExtra String gameId;

    @Inject GameUtil gameUtil;

    private HandFragment handFragment;
    private BoardFragment boardFragment;
    private Subscription gameStateSubscription;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpBoard();
        setUpHand();
    }

    private void setUpBoard() {
        boardFragment = new BoardFragment();
        final Bundle bundle = new Bundle();
        bundle.putString("gameId", gameId);
        boardFragment.setArguments(bundle);
        boardFragment.setCellListener(this);
        getSupportFragmentManager().beginTransaction().add(R.id.board_content, boardFragment).commit();
    }

    private void setUpHand() {
        handFragment = new HandFragment();
        final Bundle bundle = new Bundle();
        bundle.putString("gameId", gameId);
        handFragment.setArguments(bundle);
        handFragment.setHandListener(this);
        getSupportFragmentManager().beginTransaction().add(R.id.hand_content, handFragment).commit();

        BottomSheetBehavior.from(bottomSheet).setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                handFragment.setOffset(slideOffset);
                final float fabScale = 1 + Math.min(0, slideOffset);
                playButton.animate().scaleX(fabScale).scaleY(fabScale).setDuration(0).start();
            }
        });
    }

    @Override
    protected int getLayoutId() {
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

    @Override
    public void cardSelected(Card card) {
        if (boardFragment != null) {
            boardFragment.cardSelected(card);
        }
    }

    @Override
    public void cellSelected(int position, int playerTeam) {
        handFragment.deselectCard();
    }

    @Override
    public void onBackPressed() {
        final BottomSheetBehavior<View> bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            showExitConfirmationDialog();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameStateSubscription = gameUtil.getGameStateObservable(gameId)
                .subscribe(new Action1<GameState>() {
                    @Override
                    public void call(GameState gameState) {
                        if (gameState == GameState.FINISHED) {
                            showGameOverDialog();
                        }
                    }
                });
    }

    @Override
    protected void onPause() {
        if (gameStateSubscription != null) {
            gameStateSubscription.unsubscribe();
        }
        super.onPause();
    }

    private void showGameOverDialog() {
        final SeeKwensDialogFragment dialog = new SeeKwensDialogFragment.Builder()
                .setTitle(getString(R.string.game_over))
                .setMessage(getString(R.string.winning_message))
                .setPositiveButton(getString(R.string.inspect_board), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //do nothing for now
                    }
                })
                .setNegativeButton(getString(R.string.quit), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setCancelable(false)
                .build();
        dialog.show(getSupportFragmentManager(), dialog.getTag());
    }

    private void showExitConfirmationDialog() {
        final SeeKwensDialogFragment dialog = new SeeKwensDialogFragment.Builder().setMessage(getString(R.string.game_quit_confirmation))
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton(getString(R.string.no), null)
                .setCancelable(false)
                .build();
        dialog.show(getSupportFragmentManager(), dialog.getTag());
    }
}
