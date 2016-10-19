package com.chinmay.seekwens.database;

import com.chinmay.seekwens.model.Game;
import com.chinmay.seekwens.model.Player;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
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

    public static Observable<Boolean> joinGame(final String gameId, final String playerId, final String playerName) {
        return Observable.create(new FirebaseGameJoinerSubscriber(gameId, playerId, playerName));
    }

    public static Query getPlayerRef(String gameId) {
        return FirebaseDatabase.getInstance().getReference(gameId).child(PLAYERS_KEY).orderByChild("order");
    }

    public static void selectTeamForPlayer(String gameId, String playerId, int team) {
        final DatabaseReference child = FirebaseDatabase.getInstance().getReference(gameId).child(PLAYERS_KEY).child(playerId);
        final Map<String, Object> teamMap = Collections.singletonMap(TEAM_KEY, (Object) new Integer(team));
        child.updateChildren(teamMap);
    }

    public static void swapPlayers(final String gameId, final int fromPosition, final int toPosition) {
        final DatabaseReference playersRef = FirebaseDatabase.getInstance().getReference(gameId).child(PLAYERS_KEY);
        playersRef.orderByChild("order").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                int currentPosition = 0;
                final boolean down = fromPosition < toPosition;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    final Player player = snapshot.getValue(Player.class);
                    final DatabaseReference orderRef = playersRef.child(player.getId()).child("order");
                    if (currentPosition == fromPosition) {
                        orderRef.setValue(toPosition);
                    } else if (down && currentPosition > fromPosition && currentPosition <= toPosition) {
                        orderRef.setValue(currentPosition - 1);
                    } else if (!down && currentPosition >= toPosition && currentPosition < fromPosition) {
                        orderRef.setValue(currentPosition + 1);
                    } else {
                        orderRef.setValue(currentPosition);
                    }
                    currentPosition++;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private static final class FirebaseGameJoinerSubscriber implements Observable.OnSubscribe<Boolean> {

        private final String playerName;
        private final String playerId;
        private final String gameId;

        public FirebaseGameJoinerSubscriber(String gameId, String playerId, String playerName) {
            this.gameId = gameId;
            this.playerId = playerId;
            this.playerName = playerName;
        }

        @Override
        public void call(final Subscriber<? super Boolean> subscriber) {
            Observable.create(new FirebaseGameCheckerSubscriber(gameId))
                    .subscribe(new Subscriber<Game>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                            subscriber.onError(e);
                        }

                        @Override
                        public void onNext(final Game validGame) {
                            if (subscriber.isUnsubscribed()) {
                                return;
                            }
                            final FirebaseDatabase database = FirebaseDatabase.getInstance();
                            final DatabaseReference players = database.getReference(gameId).child(PLAYERS_KEY);
                            players.runTransaction(new Transaction.Handler() {
                                @Override
                                public Transaction.Result doTransaction(MutableData mutableData) {
                                    final Player player;
                                    if (mutableData.hasChild(playerId)) {
                                        player = mutableData.child(playerId).getValue(Player.class);
                                        player.name = playerName;
                                    } else {
                                        player = Player.with(playerId, playerName);
                                        player.order = (int) mutableData.getChildrenCount();
                                    }
                                    mutableData.child(playerId).setValue(player);
                                    return Transaction.success(mutableData);
                                }

                                @Override
                                public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                                    subscriber.onNext(playerId.equals(validGame.ownerId));
                                    subscriber.onCompleted();
                                }
                            });
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
                    subscriber.onCompleted();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    subscriber.onError(new IOException(databaseError.getMessage()));
                }
            });
        }
    }
}
