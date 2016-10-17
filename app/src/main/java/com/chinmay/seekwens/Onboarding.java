package com.chinmay.seekwens;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.chinmay.seekwens.database.FireBaseUtils;
import com.jakewharton.rxbinding.widget.RxTextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func2;

import static com.chinmay.seekwens.SeeKwensApplication.PREFS;
import static com.chinmay.seekwens.SeeKwensApplication.USER_ID_KEY;

public class Onboarding extends AppCompatActivity {

    @BindView(R.id.button_join_game) Button joinGame;
    @BindView(R.id.button_new_game) Button createGame;
    @BindView(R.id.display_name_join) EditText displayNameJoin;
    @BindView(R.id.display_name_new) EditText displayNameNew;
    @BindView(R.id.game_id) EditText gameIdField;
    private Subscription createGameSubscription;
    private Subscription joinGameSubscription;
    private Subscription gameJoinFirebaseSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);
        ButterKnife.bind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        createGameSubscription = RxTextView.textChanges(displayNameNew)
                .subscribe(new Action1<CharSequence>() {
                    @Override
                    public void call(CharSequence charSequence) {
                        createGame.setEnabled(charSequence.length() > 0);
                    }
                });

        joinGameSubscription = Observable.combineLatest(RxTextView.textChanges(displayNameJoin),
                                    RxTextView.textChanges(gameIdField),
                                    new Func2<CharSequence, CharSequence, Boolean>() {
                                        @Override
                                        public Boolean call(CharSequence name, CharSequence gameId) {
                                            return name.length() > 0 && gameId.length() > 0;
                                        }
                                    })
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean valid) {
                        joinGame.setEnabled(valid);
                    }
                });
    }

    @Override
    protected void onPause() {
        if (createGameSubscription != null) {
            createGameSubscription.unsubscribe();
        }

        if (joinGameSubscription != null) {
            joinGameSubscription.unsubscribe();
        }

        if (gameJoinFirebaseSubscription != null) {
            gameJoinFirebaseSubscription.unsubscribe();
        }
        super.onPause();
    }

    @OnClick(R.id.button_new_game)
    public void onClickCreateGame() {
        final String playerId = getApplicationContext().getSharedPreferences(PREFS, MODE_PRIVATE).getString(USER_ID_KEY, null);
        final String playerName = displayNameNew.getText().toString();
        final String gameId = FireBaseUtils.createNewGame(playerId, playerName);
        Toast.makeText(this, "Created game with id " + gameId, Toast.LENGTH_LONG).show();

    }

    @OnClick(R.id.button_join_game)
    public void onClickJoinGame() {
        final String playerId = getApplicationContext().getSharedPreferences(PREFS, MODE_PRIVATE).getString(USER_ID_KEY, null);
        final String playerName = displayNameJoin.getText().toString();
        final String gameId = gameIdField.getText().toString();

        gameJoinFirebaseSubscription = FireBaseUtils.joinGame(gameId, playerId, playerName)
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(Onboarding.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onNext(String s) {
                        Toast.makeText(Onboarding.this, "Joining game " + gameId  + " with name " + playerName, Toast.LENGTH_LONG).show();
                    }
                });
    }
}
