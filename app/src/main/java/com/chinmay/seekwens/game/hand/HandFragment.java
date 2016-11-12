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
import com.chinmay.seekwens.game.GameActivity;
import com.chinmay.seekwens.model.Card;
import com.chinmay.seekwens.model.Game;
import com.chinmay.seekwens.model.Player;
import com.chinmay.seekwens.ui.BaseSeeKwensFragment;
import com.chinmay.seekwens.util.GameUtil;
import com.f2prateek.dart.InjectExtra;
import com.google.firebase.database.Query;

import javax.inject.Inject;

import butterknife.BindView;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static android.content.Context.MODE_PRIVATE;
import static com.chinmay.seekwens.SeeKwensApplication.PREFS;
import static com.chinmay.seekwens.SeeKwensApplication.USER_ID_KEY;

public class HandFragment extends BaseSeeKwensFragment implements HandAdapter.CardSelectListener {

    @BindView(R.id.hand_recycler) RecyclerView handRecycler;
    @BindView(R.id.player_turn) TextView playerTurn;

    @InjectExtra String gameId;

    @Inject FireBaseUtils fireBaseUtils;
    @Inject GameUtil gameUtil;

    private String playerId;
    private HandAdapter handAdapter;
    private float offset;
    private Game game;
    private int playerOrder = -1;
    private Subscription currentPlayerSubscription;
    private HandListener handListener;
    private boolean myTurn;

    @Override
    protected int getLayoutId() {
        return R.layout.hand_bottom_sheet;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        playerId = getActivity().getApplicationContext().getSharedPreferences(PREFS, MODE_PRIVATE).getString(USER_ID_KEY, null);

        gameUtil.gameReadObservable(gameId)
                .subscribeOn(Schedulers.io())
                .doOnNext(new Action1<Game>() {
                    @Override
                    public void call(Game game) {
                        HandFragment.this.game = game;
                    }
                })
                .flatMap(new Func1<Game, Observable<Player>>() {
                    @Override
                    public Observable<Player> call(Game game) {
                        return Observable.from(game.players.values());
                    }
                })
                .filter(new Func1<Player, Boolean>() {
                    @Override
                    public Boolean call(Player player) {
                        return player.getId().equals(playerId);
                    }
                })
                .doOnNext(new Action1<Player>() {
                    @Override
                    public void call(Player player) {
                        playerOrder = player.order;
                    }
                })
                .map(new Func1<Player, Boolean>() {
                    @Override
                    public Boolean call(Player player) {
                        return game.currentPlayer == playerOrder;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean myTurn) {
                        updateMyTurn(myTurn);
                    }
                });

        final Query handRef = fireBaseUtils.getHandRef(gameId, playerId);
        handAdapter = new HandAdapter(handRef, this);
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

    @Override
    public void onResume() {
        super.onResume();
        currentPlayerSubscription = gameUtil.currentPlayerObservable(gameId)
                .map(new Func1<Long, Boolean>() {
                    @Override
                    public Boolean call(Long currentPlayer) {
                        return currentPlayer.intValue() == playerOrder;
                    }
                })
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean myTurn) {
                        updateMyTurn(myTurn);
                    }
                });
    }

    private void updateMyTurn(boolean myTurn) {
        this.myTurn = myTurn;
        setPlayerTurnMessage();
        handRecycler.setClickable(myTurn);
    }

    @Override
    public void onPause() {
        if (currentPlayerSubscription != null) {
            currentPlayerSubscription.unsubscribe();
        }
        super.onPause();
    }

    private void setPlayerTurnMessage() {
        if (myTurn) {
            playerTurn.setText(R.string.your_turn);
        } else {
            playerTurn.setText(R.string.waiting_for_turn);
        }
    }

    public void setHandListener(HandListener handListener) {
        this.handListener = handListener;
    }

    @Override
    public void cardSelected(Card card) {
        if (myTurn && handListener != null) {
            handListener.cardSelected(card);
        }
    }
}
