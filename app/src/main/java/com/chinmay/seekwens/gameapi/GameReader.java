package com.chinmay.seekwens.gameapi;

import com.chinmay.seekwens.BuildConfig;
import com.chinmay.seekwens.model.Game;

import javax.inject.Singleton;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;
import rx.Observable;

@Singleton
public class GameReader {
    HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);
    OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();
    private Retrofit retrofit = new Retrofit.Builder()
            .client(client)
            .baseUrl(BuildConfig.FIREBASE_DATABASE_URL)
            .addConverterFactory(JacksonConverterFactory.create())
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .build();

    private GameReadOnlyApi service = retrofit.create(GameReadOnlyApi.class);

    public Observable<Game> readGame(String gameId) {
        return service.readGame(gameId);
    }
}
