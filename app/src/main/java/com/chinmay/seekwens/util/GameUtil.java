package com.chinmay.seekwens.util;

import android.util.Log;

import com.chinmay.seekwens.cards.CardManager;
import com.chinmay.seekwens.cards.models.DeckResponse;
import com.chinmay.seekwens.database.FireBaseUtils;
import com.chinmay.seekwens.gameapi.GameReader;
import com.chinmay.seekwens.model.Card;
import com.chinmay.seekwens.model.Game;
import com.chinmay.seekwens.model.GameState;
import com.chinmay.seekwens.model.LastMove;
import com.chinmay.seekwens.model.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

@Singleton
public class GameUtil {

    @Inject GameReader gameReader;
    @Inject CardManager cardManager;
    @Inject Rules rules;
    @Inject FireBaseUtils fireBaseUtils;


    public Observable<Game> gameReadObservable(String gameId) {
        return gameReader.readGame(gameId);
    }

    public Observable<DeckResponse> getNewDeckObservable() {
        return cardManager.newDeck();
    }

    public Observable<Boolean> distributeCards(final String gameId, final String deckId) {
        return gameReadObservable(gameId)
                .subscribeOn(Schedulers.io())
                .doOnNext(new Action1<Game>() {
                    @Override
                    public void call(Game game) {
                        for (Player player : game.players.values()) {
                            fireBaseUtils.clearHand(gameId, player.getId());
                        }
                    }
                })
                .flatMap(new Func1<Game, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(Game game) {
                        final int cardsPerPlayer = rules.cardsPerPlayer(game.players.size());
                        return Observable.from(game.players.values())
                                .observeOn(Schedulers.io())
                                .sorted(new Func2<Player, Player, Integer>() {
                                    @Override
                                    public Integer call(Player player, Player player2) {
                                        return player.order - player2.order;
                                    }
                                })
                                .repeat(cardsPerPlayer)
                                .zipWith(cardManager.drawCards(deckId, cardsPerPlayer*game.players.size()), new Func2<Player, Card, Boolean>() {
                                    @Override
                                    public Boolean call(Player player, Card card) {
                                        fireBaseUtils.distributeCard(gameId, player.getId(), card);
                                        return true;
                                    }
                                });
                    }
                });
    }

    public void setDeck(String gameId, String deckId) {
        fireBaseUtils.setDeck(gameId, deckId);
    }
    public void setGameState(String gameId, GameState gameState) {
        fireBaseUtils.setGameState(gameId, gameState);
    }

    public String[] getBoard() {
        return rules.board;
    }

    public Observable<ArrayList> getBoardObservable(String gameId) {
        return fireBaseUtils.getBoardObservable(gameId);
    }

    public void readyNewGame(String gameId) {
        fireBaseUtils.setGameState(gameId, GameState.STARTING);
        fireBaseUtils.setBoard(gameId, rules.emptyBoard());
        fireBaseUtils.setCurrentPlayer(gameId, 0);
        fireBaseUtils.setLastMove(gameId, null);
    }

    public Observable<Long> currentPlayerObservable(String gameId) {
        return fireBaseUtils.currentPlayerObservable(gameId);
    }

    public boolean playMove(String gameId, LastMove lastMove) {
        fireBaseUtils.setLastMove(gameId, lastMove);
        if (rules.shouldRemoveCoin(lastMove.card)) {
            fireBaseUtils.placeCoin(gameId, lastMove.tile, -1);
            return true;
        } else {
            fireBaseUtils.placeCoin(gameId, lastMove.tile, lastMove.team);
            return false;
        }
    }

    public void drawNewCard(final Game game, final String playerId, final Card oldCard) {
        cardManager.drawCard(game.deckId)
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<Card>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Log.e("ERROR", e.getMessage());
                    }

                    @Override
                    public void onNext(Card newCard) {
                        final int nextPlayer = (game.currentPlayer + 1) % game.players.size();
                        fireBaseUtils.setCurrentPlayer(game.id, nextPlayer);
                        fireBaseUtils.discardCard(game.id, playerId, oldCard.id);
                        fireBaseUtils.distributeCard(game.id, playerId, newCard);
                    }
                });
    }

    public void checkWinner(String gameId, List<Long> chips, long playerTeam, int position, boolean didRemove, int totalTeams) {
        final ArrayList<Long> chipsCopy = new ArrayList<>(chips);
        chipsCopy.set(position, didRemove ? -1 : playerTeam);
        final String[][] chips2D = rules.transform2D(chipsCopy, playerTeam);
        int sequences = 0;
        final String horizontal = rules.horizontalString(chips2D);
        sequences += countSequences(horizontal, playerTeam);
        final String vertical = rules.verticalString(chips2D);
        sequences += countSequences(vertical, playerTeam);
        final String backSlash = rules.backSlashString(chips2D);
        sequences += countSequences(backSlash, playerTeam);
        final String forwardSlash = rules.forwardSlashString(chips2D);
        sequences += countSequences(forwardSlash, playerTeam);

        if (sequences >= rules.sequencesNeeded(totalTeams)) {
            fireBaseUtils.setCurrentPlayer(gameId, -1);
            fireBaseUtils.setGameState(gameId, GameState.FINISHED);
        }
    }

    private int countSequences(String board, long playerTeam) {
        final Pattern pattern = Pattern.compile(String.format("%d{5,}", playerTeam));
        final Matcher matcher = pattern.matcher(board);
        int sum = 0;
        while (matcher.find()) {
            sum += matcher.group(0).length() > 9 ? 2 : 1;
        }
        return sum;
    }

    public Observable<GameState> getGameStateObservable(String gameId) {
        return fireBaseUtils.getGameStateObservable(gameId);
    }

    public Observable<LastMove> getLastMoveObservable(String gameId) {
        return fireBaseUtils.getLastMoveObservable(gameId);
    }

    public String cardName(String cardCode, int tile) {
        return rules.cardName(cardCode, tile);
    }
}
