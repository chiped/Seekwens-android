package com.chinmay.seekwens.game.board;

import android.animation.Animator;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.chinmay.seekwens.R;
import com.chinmay.seekwens.game.hand.HandListener;
import com.chinmay.seekwens.model.Card;
import com.chinmay.seekwens.model.Game;
import com.chinmay.seekwens.model.LastMove;
import com.chinmay.seekwens.model.Player;
import com.chinmay.seekwens.ui.BaseSeeKwensFragment;
import com.chinmay.seekwens.util.GameUtil;
import com.f2prateek.dart.InjectExtra;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import butterknife.BindView;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static android.content.Context.MODE_PRIVATE;
import static com.chinmay.seekwens.SeeKwensApplication.PREFS;
import static com.chinmay.seekwens.SeeKwensApplication.USER_ID_KEY;

public class BoardFragment extends BaseSeeKwensFragment implements HandListener, BoardAdapter.CellSelectListener {

    @BindView(R.id.board_recycler) RecyclerView boardRecycler;
    @BindView(R.id.board_container) View boardContainer;

    @InjectExtra String gameId;

    @Inject GameUtil gameUtil;

    private BoardAdapter boardAdapter;
    private Subscription boardSubscription;
    private String playerId;
    private int playerTeam;
    private CellListener cellListener;
    private Card selectedHandCard;
    private int totalTeams;
    private Game game;
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector panGestureDetector;
    private float recyclerScale = 1;

    @Override
    protected int getLayoutId() {
        return R.layout.board_fragment;
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
                        BoardFragment.this.game = game;
                        final Set<Integer> teams = new HashSet<>();
                        for (Player player : game.players.values()) {
                            teams.add(player.team);
                        }
                        totalTeams = teams.size();
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
                .subscribe(new Action1<Player>() {
                    @Override
                    public void call(Player player) {
                        playerTeam = player.team;
                        boardAdapter.setPlayerTeam(playerTeam);
                    }
                });

        boardAdapter = new BoardAdapter(gameUtil.getBoard(), this);
//        final BoardLayoutManager layout = new BoardLayoutManager();
//        layout.setTotalColumnCount(10);
        boardRecycler.setLayoutManager(new GridLayoutManager(getContext(), 10));
        boardRecycler.setAdapter(boardAdapter);

        boardRecycler.setOnTouchListener(new MyOnTouchListener());
        final ScaleGestureDetector.SimpleOnScaleGestureListener listener = new MySimpleOnScaleGestureListener();
        scaleGestureDetector = new ScaleGestureDetector(getContext(), listener);
        panGestureDetector = new GestureDetector(getContext(), new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                float translateX = 0;
                float translateY = 0;
                int[] l = new int[2];
                final Rect boardViewBounds = new Rect();
                boardRecycler.getLocalVisibleRect(boardViewBounds);
//                final Rect boardViewBounds = new Rect(l[0], l[1],
//                        l[0] + (int) (boardRecycler.getWidth() * boardRecycler.getScaleX()),
//                        l[1] + (int) (boardRecycler.getHeight() * boardRecycler.getScaleY()));
                if (Math.abs(distanceX) > 50) {
                    if (boardViewBounds.left - distanceX > 0) {
                        translateX = boardViewBounds.left;
                    } else if (boardViewBounds.right - distanceX < boardContainer.getRight()) {
                        translateX = -(boardViewBounds.right - boardContainer.getRight());
                    } else {
                        translateX = -distanceX;
                    }
                }
//                if (Math.abs(distanceY) > 50
//                        && boardViewBounds.top - distanceY <= 0
//                        && boardViewBounds.bottom - distanceY >= boardContainer.getBottom()) {
//                    translateY = -distanceY;
//                }
                if (translateX != 0 || translateY != 0) {
                    boardRecycler.animate().translationXBy(translateX).translationYBy(translateY).setDuration(0).start();
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public void onLongPress(MotionEvent e) {

            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return false;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        boardSubscription = gameUtil.getBoardObservable(gameId)
                .subscribe(new Action1<ArrayList>() {
                    @Override
                    public void call(ArrayList board) {
                        boardAdapter.setChips(board);
                        boardAdapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    public void onPause() {
        if (boardSubscription != null) {
            boardSubscription.unsubscribe();
        }
        super.onPause();
    }

    public void setCellListener(CellListener cellListener) {
        this.cellListener = cellListener;
    }

    @Override
    public void cardSelected(Card card) {
        this.selectedHandCard = card;
        boardAdapter.highlightCards(card, gameUtil.getPlayablePositions(card, playerTeam, boardAdapter.getChips()));
    }

    @Override
    public void cellClicked(int position) {
        //TODO possibly move this to GameActivity and enable send button
        final LastMove lastMove = new LastMove();
        lastMove.card = selectedHandCard.code;
        lastMove.player = game.players.get(playerId).name;
        lastMove.team = playerTeam;
        lastMove.tile = position;
        final boolean didRemove = gameUtil.playMove(gameId, lastMove);
        if (cellListener != null) {
            cellListener.cellSelected(position, playerTeam);
        }
        gameUtil.checkWinner(gameId, boardAdapter.getChips(), playerTeam, position, didRemove, totalTeams);
    }

    public void lastMovePlayed(int tile) {
        if (boardRecycler != null) {
            boardRecycler.smoothScrollToPosition(tile);
        }
    }

    private class MyOnTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            scaleGestureDetector.onTouchEvent(event);
            panGestureDetector.onTouchEvent(event);
            return false;
        }
    }

    private class MySimpleOnScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        private static final int SPAN_SLOP = 7;

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (gestureTolerance(detector)) {
                recyclerScale *= detector.getScaleFactor();
                recyclerScale = Math.max(1f, Math.min(recyclerScale, 2.0f));
                boardRecycler.animate().scaleX(recyclerScale).scaleY(recyclerScale).setDuration(0)
                        .setListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                boardRecycler.animate().setListener(null);
                                final Rect boardViewBounds = new Rect();
                                boardRecycler.getLocalVisibleRect(boardViewBounds);
                                if (boardViewBounds.left > 0) {
                                    boardRecycler.animate().translationXBy(-boardViewBounds.left).setDuration(100).start();
                                }
                                if (boardViewBounds.right < boardContainer.getRight()) {
                                    boardRecycler.animate().translationXBy(boardContainer.getRight() - boardViewBounds.right).setDuration(100).start();
                                }
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        }).start();
                return true;
            } else {
                return false;
            }
        }

        private boolean gestureTolerance(ScaleGestureDetector detector) {
            final float spanDelta = Math.abs(detector.getCurrentSpan() - detector.getPreviousSpan());
            return spanDelta > SPAN_SLOP;
        }
    }
}
