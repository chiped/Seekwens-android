package com.chinmay.seekwens.database;

import com.chinmay.seekwens.model.Game;
import com.chinmay.seekwens.model.Player;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import rx.Observable;
import rx.Subscriber;

public class FireBaseUtils {

    public static final String PLAYERS_KEY = "players";
    public static final String TEAM_KEY = "team";

    public static String createNewGame(String playerId, String playerName) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();

        final Game game = Game.with(playerId);
        final DatabaseReference gameRef = database.getReference(game.getId());
        gameRef.setValue(game);

        final DatabaseReference players = gameRef.child(PLAYERS_KEY);
        final Player player = Player.with(playerId, playerName);
        final Map<String, Object> map = Collections.singletonMap(playerId, (Object) player);
        players.updateChildren(map);

        return game.getId();
    }

    public static Observable<String> joinGame(final String gameId, final String playerId, final String playerName) {
        return Observable.create(new FirebaseGameJoinerSubscriber(gameId, playerId, playerName));
    }

    public static DatabaseReference getPlayerRef(String gameId) {
        return FirebaseDatabase.getInstance().getReference(gameId).child(PLAYERS_KEY);
    }

    public static void selectTeamForPlayer(String gameId, String playerId, int team) {
        final DatabaseReference child = FirebaseDatabase.getInstance().getReference(gameId).child(PLAYERS_KEY).child(playerId);
        final Map<String, Object> teamMap = Collections.singletonMap(TEAM_KEY, (Object) new Integer(team));
        child.updateChildren(teamMap);
    }

    private static final class FirebaseGameJoinerSubscriber implements Observable.OnSubscribe<String> {

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
                            if (subscriber.isUnsubscribed()) {
                                return;
                            }
                            final FirebaseDatabase database = FirebaseDatabase.getInstance();
                            final DatabaseReference players = database.getReference(gameId).child(PLAYERS_KEY);
                            final Player player = Player.with(playerId, playerName);
                            final Map<String, Object> map = Collections.singletonMap(playerId, (Object) player);
                            players.updateChildren(map);
                            subscriber.onNext(null);
                        }
                    });
        }
    }

    private static final class FirebaseGameCheckerSubscriber implements Observable.OnSubscribe<Game> {

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
