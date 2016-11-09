package com.chinmay.seekwens.cards;

import com.chinmay.seekwens.cards.models.DeckResponse;
import com.chinmay.seekwens.model.Card;

import javax.inject.Singleton;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

@Singleton
public class CardManager {
    private Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://deckofcardsapi.com/api/")
            .addConverterFactory(JacksonConverterFactory.create())
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .build();

    private DeckOfCardsApi service = retrofit.create(DeckOfCardsApi.class);

    public Observable<DeckResponse> newDeck() {
        return service.createNewDeck().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Card> drawCard(String deckId) {
        return service.drawCard(deckId, 1).map(new Func1<DeckResponse, Card>() {
            @Override
            public Card call(DeckResponse deckResponse) {
                return deckResponse.cards.get(0);
            }
        });
    }

    public Observable<Card> drawCards(String deckId, int count) {
        return service.drawCard(deckId, count).flatMap(new Func1<DeckResponse, Observable<Card>>() {
            @Override
            public Observable<Card> call(DeckResponse deckResponse) {
                return Observable.from(deckResponse.cards);
            }
        });
    }
}
