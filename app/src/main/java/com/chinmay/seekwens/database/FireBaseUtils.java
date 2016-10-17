package com.chinmay.seekwens.database;

import com.chinmay.seekwens.model.Game;
import com.chinmay.seekwens.model.Player;
import com.chinmay.seekwens.model.Team;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;

import rx.Observable;
import rx.Subscriber;

public class FireBaseUtils {

    public static String createNewGame(String playerId, String playerName) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();

        final Game game = new Game();
        final DatabaseReference dbReference = database.getReference(game.getId());

        final Player player = Player.with(playerId, playerName);

        final Team team = new Team();
        team.playerList.add(player);
        game.teamList.add(team);
        dbReference.setValue(game);

        return game.getId();
    }

    public static Observable<String> joinGame(final String gameId, final String playerId, final String playerName) {
        return Observable.create(new FirebaseGameJoinerSubscriber(gameId, playerId, playerName));
    }

    final static class FirebaseGameJoinerSubscriber implements Observable.OnSubscribe<String> {

        private final String playerName;
        private final String playerId;
        private final String gameId;

        public FirebaseGameJoinerSubscriber(String gameId, String playerId, String playerName) {
            this.gameId = gameId;
            this.playerId = playerId;
            this.playerName = playerName;
        }

        @Override
        public void call(final Subscriber<? super String> subscriber) {
            Observable.create(new FirebaseGameCheckerSubscriber(gameId))
                    .subscribe(new Subscriber<Game>() {
                        @Override
                        public void onCompleted() {
                            subscriber.onCompleted();
                        }

                        @Override
                        public void onError(Throwable e) {
                            subscriber.onError(e);
                        }

                        @Override
                        public void onNext(Game validGame) {
                            final FirebaseDatabase database = FirebaseDatabase.getInstance();
                            final DatabaseReference dbReference = database.getReference(gameId);
                            final Player player = Player.with(playerId, playerName);
                            final Team team = new Team();
                            team.playerList.add(player);
                            validGame.teamList.add(team);

                            dbReference.setValue(validGame);
                            subscriber.onNext(null);
                        }
                    });
        }
    }

    final static class FirebaseGameCheckerSubscriber implements Observable.OnSubscribe<Game> {

        public static final String NOT_FOUND_ERROR_STRING = "Game id %s doesn't exist";
        public static final String GAME_RUNNING_ERROR_STRING = "Game %s has already started.";
        private final String gameId;

        public FirebaseGameCheckerSubscriber(String gameId) {
            this.gameId = gameId;
        }

        @Override
        public void call(final Subscriber<? super Game> subscriber) {
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            database.getReference().addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (subscriber.isUnsubscribed()) {
                        return;
                    }
                    if (!dataSnapshot.hasChild(gameId)) {
                        subscriber.onError(new IllegalStateException(String.format(NOT_FOUND_ERROR_STRING, gameId)));
                        return;
                    }

                    final Game game = dataSnapshot.child(gameId).getValue(Game.class);
                    if (game.started) {
                        subscriber.onError(new IllegalStateException(String.format(GAME_RUNNING_ERROR_STRING, gameId)));
                        return;
                    }

                    subscriber.onNext(game);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    subscriber.onError(new IOException(databaseError.getMessage()));
                }
            });
        }
    }
}
