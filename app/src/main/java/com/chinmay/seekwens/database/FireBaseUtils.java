package com.chinmay.seekwens.database;

import android.util.Log;

import com.chinmay.seekwens.model.Card;
import com.chinmay.seekwens.model.Game;
import com.chinmay.seekwens.model.GameState;
import com.chinmay.seekwens.model.Player;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Singleton;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

@Singleton
public class FireBaseUtils {

    public static final String PLAYERS_KEY = "players";
    public static final String TEAM_KEY = "team";
    public static final String HAND_KEY = "hand";
    public static final String GAME_STATE_KEY = "state";
    public static final String DECK_ID_KEY = "deckId";
    public static final String TEAM_VALIDATION_REGEX = "^(\\d+?)\\1*$";

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

    public static Observable<Game> joinGame(final String gameId, final String playerId, final String playerName) {
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

    public static Observable<Boolean> getGameValidObservable(String gameId) {
        return Observable.create(new FirebaseGameValidatorSubscriber(gameId));
    }

    public static Observable<Game> getGameReaderObservable(String gameId) {
        return Observable.create(new FirebaseGameReader(gameId));
    }

    public void distributeCard(String gameId, String playerId, Card card) {
        final DatabaseReference cardReference = FirebaseDatabase.getInstance()
                .getReference(gameId)
                .child(PLAYERS_KEY)
                .child(playerId)
                .child(HAND_KEY)
                .push();

        card.setId(cardReference.getKey());
        cardReference.setValue(card);
    }

    public void clearHand(String gameId, String playerId) {
        FirebaseDatabase.getInstance()
                .getReference(gameId)
                .child(PLAYERS_KEY)
                .child(playerId)
                .child(HAND_KEY)
                .removeValue();
    }

    public void setDeck(String gameId, String deckId) {
        FirebaseDatabase.getInstance()
                .getReference(gameId)
                .child(DECK_ID_KEY)
                .setValue(deckId);
    }

    public void setGameState(String gameId, GameState gameState) {
        FirebaseDatabase.getInstance()
                .getReference(gameId)
                .child(GAME_STATE_KEY)
                .setValue(gameState);
    }

    public Query getHandRef(String gameId, String playerId) {
        return FirebaseDatabase.getInstance().getReference(gameId).child(PLAYERS_KEY).child(playerId).child(HAND_KEY);
    }

    private static final class FirebaseGameJoinerSubscriber implements Observable.OnSubscribe<Game> {

        private final String playerName;
        private final String playerId;
        private final String gameId;

        public FirebaseGameJoinerSubscriber(String gameId, String playerId, String playerName) {
            this.gameId = gameId;
            this.playerId = playerId;
            this.playerName = playerName;
        }

        @Override
        public void call(final Subscriber<? super Game> subscriber) {
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
                                    subscriber.onNext(validGame);
                                    subscriber.onCompleted();
                                }
                            });
                        }
                    });
        }
    }

    private static final class FirebaseGameCheckerSubscriber implements Observable.OnSubscribe<Game> {

        public static final String NOT_FOUND_ERROR_STRING = "Game id %s doesn't exist";
        public static final String GAME_RUNNING_ERROR_STRING = "Game %s has already finished.";
        public static final String TRY_AGAIN_ERROR_STRING = "Game %s is being configured. Try again later.";
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
                    if (game.state == GameState.FINISHED) {
                        subscriber.onError(new IllegalStateException(String.format(GAME_RUNNING_ERROR_STRING, gameId)));
                        return;
                    } else if (game.state == GameState.STARTING) {
                        subscriber.onError(new IllegalStateException(String.format(TRY_AGAIN_ERROR_STRING, gameId)));
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

    private static final class FirebaseGameValidatorSubscriber implements Observable.OnSubscribe<Boolean> {
        private final String gameId;

        public FirebaseGameValidatorSubscriber(String gameId) {
            this.gameId = gameId;
        }

        @Override
        public void call(final Subscriber<? super Boolean> subscriber) {
            FirebaseDatabase.getInstance().getReference(gameId).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    validateGame();
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    validateGame();
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    validateGame();
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                    validateGame();
                }

                private void validateGame() {
                    getGameReaderObservable(gameId)
                            .observeOn(Schedulers.io())
                            .subscribe(new Action1<Game>() {
                                @Override
                                public void call(Game game) {
                                    if (subscriber.isUnsubscribed()) {
                                        return;
                                    }
                                    final ArrayList<Player> players = new ArrayList<>(game.players.values());
                                    if (players.size() > 12 || (players.size() % 2 != 0 && players.size() % 3 != 0)) {
                                        subscriber.onNext(false);
                                    }
                                    Collections.sort(players, new Comparator<Player>() {
                                        @Override
                                        public int compare(Player o1, Player o2) {
                                            return o1.order - o2.order;
                                        }
                                    });
                                    final StringBuffer stringBuffer = new StringBuffer();
                                    for (Player player : players) {
                                        stringBuffer.append(player.team);
                                    }
                                    Log.v("Player teams: ", stringBuffer.toString());
                                    final Pattern pattern = Pattern.compile(TEAM_VALIDATION_REGEX);
                                    final Matcher matcher = pattern.matcher(stringBuffer);
                                    if (!matcher.find()) {
                                        subscriber.onNext(false);
                                    }
                                    final String group = matcher.group(1);
                                    final HashSet<Character> characters = new HashSet<>();
                                    for (char c : group.toCharArray()){
                                        characters.add(c);
                                    }
                                    Log.v("matcher group: ", group);
                                    Log.v("character set: ", characters.toString());
                                    boolean isValid = (group.length() == 2 && characters.size() == 2)
                                                        || (group.length() == 3 && characters.size() == 3);
                                    subscriber.onNext(isValid);
                                }
                            });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    if (subscriber.isUnsubscribed()) {
                        return;
                    }
                    subscriber.onError(new IOException(databaseError.getMessage()));
                }
            });
        }
    }

    private static final class FirebaseGameReader implements Observable.OnSubscribe<Game> {
        private final String gameId;

        public FirebaseGameReader(String gameId) {
            this.gameId = gameId;
        }

        @Override
        public void call(final Subscriber<? super Game> subscriber) {
            FirebaseDatabase.getInstance().getReference(gameId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (subscriber.isUnsubscribed()) {
                                return;
                            }
                            final Game game = dataSnapshot.getValue(Game.class);
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
