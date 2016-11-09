package com.chinmay.seekwens.gameapi;

import com.chinmay.seekwens.model.Game;

import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

public interface GameReadOnlyApi {

    @GET("{gameId}.json")
    Observable<Game> readGame(@Path("gameId") String gameId);
}
