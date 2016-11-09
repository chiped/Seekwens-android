package com.chinmay.seekwens.cards;

import com.chinmay.seekwens.cards.models.DeckResponse;

import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

public interface DeckOfCardsApi {

    @GET("deck/new/shuffle/?deck_count=2")
    Observable<DeckResponse> createNewDeck();

    @GET("deck/{deckId}/draw/")
    Observable<DeckResponse> drawCard(@Path("deckId") String deckId, @Query("count") int count);
}
