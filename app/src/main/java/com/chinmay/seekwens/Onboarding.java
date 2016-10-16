package com.chinmay.seekwens;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.jakewharton.rxbinding.widget.RxTextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func2;

public class Onboarding extends AppCompatActivity {

    @BindView(R.id.button_join_game) Button joinGame;
    @BindView(R.id.button_new_game) Button createGame;
    @BindView(R.id.display_name_join) EditText displayNameJoin;
    @BindView(R.id.display_name_new) EditText displayNameNew;
    @BindView(R.id.game_id) EditText gameIdField;
    private Subscription createGameSubscription;
    private Subscription joinGameSubscription;

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
        super.onPause();
    }

    @OnClick(R.id.button_new_game)
    public void onClickCreateGame() {
        Toast.makeText(this, "Creating new game with name " + displayNameNew.getText(), Toast.LENGTH_LONG).show();
    }

    @OnClick(R.id.button_join_game)
    public void onClickJoinGame() {
        Toast.makeText(this, "Joining game " + gameIdField.getText()  + " with name " + displayNameJoin.getText(), Toast.LENGTH_LONG).show();
    }
}
