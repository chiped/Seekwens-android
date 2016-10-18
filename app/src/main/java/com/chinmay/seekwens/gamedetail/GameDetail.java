package com.chinmay.seekwens.gamedetail;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.chinmay.seekwens.R;
import com.chinmay.seekwens.database.FireBaseUtils;
import com.chinmay.seekwens.model.Player;
import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.functions.Action1;

public class GameDetail extends AppCompatActivity {

    @InjectExtra String gameId;

    @BindView(R.id.gameIdValue) TextView gameIdTextView;
    @BindView(R.id.game_detail_recyclerview) RecyclerView gameDetailRecyclerView;
    private GameDetailAdapter gameDetailAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_detail);
        ButterKnife.bind(this);
        Dart.inject(this);

        gameDetailAdapter = new GameDetailAdapter(FireBaseUtils.getPlayerRef(gameId));
        gameDetailRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        gameDetailRecyclerView.setAdapter(gameDetailAdapter);
        gameDetailAdapter.setListener(new TeamSelectListener() {
            @Override
            public void onTeamSelected(String playerId, int team) {
                FireBaseUtils.selectTeamForPlayer(gameId, playerId, team);
            }
        });

        gameIdTextView.setText(gameId);
    }
}
