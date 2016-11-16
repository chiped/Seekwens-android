package com.chinmay.seekwens.gamedetail;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.chinmay.seekwens.R;
import com.chinmay.seekwens.cards.models.DeckResponse;
import com.chinmay.seekwens.database.FireBaseUtils;
import com.chinmay.seekwens.model.Game;
import com.chinmay.seekwens.model.GameState;
import com.chinmay.seekwens.ui.BaseSeeKwensActivity;
import com.chinmay.seekwens.ui.Henson;
import com.chinmay.seekwens.util.GameUtil;
import com.f2prateek.dart.InjectExtra;
import com.google.firebase.database.Query;

import javax.inject.Inject;

import butterknife.BindView;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

public class GameDetail extends BaseSeeKwensActivity {

    @InjectExtra String gameId;
    @InjectExtra boolean isOwner;

    @BindView(R.id.gameIdValue) TextView gameIdTextView;
    @BindView(R.id.game_detail_recyclerview) RecyclerView gameDetailRecyclerView;
    @BindView(R.id.start_game) Button startGame;

    @Inject GameUtil gameUtil;

    private GameDetailAdapter gameDetailAdapter;
    private Subscription gameValidatorSubscription;
    private Subscription gameStateSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setUpRecyclerView();
        gameIdTextView.setText(gameId);
        startGame.setVisibility(isOwner ? View.VISIBLE : View.GONE);
        setUpGameValidator();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_game_detail;
    }

    private void setUpGameValidator() {
        gameValidatorSubscription = FireBaseUtils.getGameValidObservable(gameId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean valid) {
                        startGame.setEnabled(valid);
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameStateSubscription = gameUtil.getGameStateObservable(gameId)
                .subscribe(new Action1<GameState>() {
                    @Override
                    public void call(GameState gameState) {
                        if (gameState == GameState.STARTED) {
                            moveToGameActivity();
                        }
                    }
                });
    }

    @Override
    protected void onPause() {
        if (gameValidatorSubscription != null) {
            gameValidatorSubscription.unsubscribe();
        }
        if (gameStateSubscription != null) {
            gameStateSubscription.unsubscribe();
        }
        super.onPause();
    }

    private void setUpRecyclerView() {
        final Query playerRef = FireBaseUtils.getPlayerRef(gameId);
        gameDetailAdapter = new GameDetailAdapter(playerRef, isOwner);
        gameDetailRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        gameDetailRecyclerView.setAdapter(gameDetailAdapter);
        gameDetailAdapter.setTeamSelectListener(new TeamSelectListener() {
            @Override
            public void onTeamSelected(String playerId, int team) {
                FireBaseUtils.selectTeamForPlayer(gameId, playerId, team);
            }
        });

        if (isOwner) {
            ItemTouchHelper.Callback callback = new PlayerTouchHelperCallback(new PlayerMoveListener() {
                @Override
                public void onItemMove(int fromPosition, int toPosition) {
                    FireBaseUtils.swapPlayers(gameId, fromPosition, toPosition);
                }
            });
            final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
            itemTouchHelper.attachToRecyclerView(gameDetailRecyclerView);
            gameDetailAdapter.setDragStartListener(new DragStartListener() {
                @Override
                public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
                    itemTouchHelper.startDrag(viewHolder);
                }
            });
            startGame.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    gameValidatorSubscription.unsubscribe();
                    gameUtil.gameReadObservable(gameId)
                            .subscribeOn(Schedulers.io())
                            .filter(new Func1<Game, Boolean>() {
                                @Override
                                public Boolean call(Game game) {
                                    return game.state == GameState.NOT_STARTED;
                                }
                            })
                            .doOnNext(new Action1<Game>() {
                                @Override
                                public void call(Game game) {
                                    gameUtil.readyNewGame(gameId);
                                }
                            })
                            .flatMap(new Func1<Game, Observable<DeckResponse>>() {
                                @Override
                                public Observable<DeckResponse> call(Game game) {
                                    return gameUtil.getNewDeckObservable();
                                }
                            })
                            .doOnNext(new Action1<DeckResponse>() {
                                @Override
                                public void call(DeckResponse deckResponse) {
                                    gameUtil.setDeck(gameId, deckResponse.deckId);
                                }
                            })
                            .flatMap(new Func1<DeckResponse, Observable<Boolean>>() {
                                @Override
                                public Observable<Boolean> call(DeckResponse deckResponse) {
                                    return gameUtil.distributeCards(gameId, deckResponse.deckId);
                                }
                            })
                            .reduce(new Func2<Boolean, Boolean, Boolean>() {
                                @Override
                                public Boolean call(Boolean bool1, Boolean bool2) {
                                    return bool1 && bool2;
                                }
                            })
                            .doOnNext(new Action1<Boolean>() {
                                @Override
                                public void call(Boolean aBoolean) {
                                    gameUtil.setGameState(gameId, GameState.STARTED);
                                }
                            })
                            .observeOn(AndroidSchedulers.mainThread())
                            .doAfterTerminate(new Action0() {
                                @Override
                                public void call() {
                                    moveToGameActivity();
                                }
                            })
                            .subscribe();
                }
            });
        }
    }

    private void moveToGameActivity() {
        final Intent intent = Henson.with(GameDetail.this)
                .gotoGameActivity()
                .gameId(gameId)
                .build();
        startActivity(intent);
        finish();
    }
}
