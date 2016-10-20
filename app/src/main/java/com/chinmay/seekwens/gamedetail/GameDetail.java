package com.chinmay.seekwens.gamedetail;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.chinmay.seekwens.R;
import com.chinmay.seekwens.database.FireBaseUtils;
import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.google.firebase.database.Query;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class GameDetail extends AppCompatActivity {

    @InjectExtra String gameId;
    @InjectExtra boolean isOwner;

    @BindView(R.id.gameIdValue) TextView gameIdTextView;
    @BindView(R.id.game_detail_recyclerview) RecyclerView gameDetailRecyclerView;
    @BindView(R.id.start_game) Button startGame;
    private GameDetailAdapter gameDetailAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_detail);
        ButterKnife.bind(this);
        Dart.inject(this);

        setUpRecyclerView();
        gameIdTextView.setText(gameId);
        startGame.setVisibility(isOwner ? View.VISIBLE : View.GONE);
        setUpGameValidator();
    }

    private void setUpGameValidator() {
        FireBaseUtils.getGameValidObservable(gameId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean valid) {
                        startGame.setEnabled(valid);
                    }
                });
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
        }
    }
}
