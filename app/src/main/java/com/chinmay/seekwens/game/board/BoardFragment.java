package com.chinmay.seekwens.game.board;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
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
        final BoardLayoutManager layout = new BoardLayoutManager();
        layout.setTotalColumnCount(10);
        boardRecycler.setLayoutManager(layout);
        boardRecycler.setAdapter(boardAdapter);
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
        boardAdapter.highlightCard(card);
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
}
