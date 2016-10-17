package com.chinmay.seekwens;

import android.app.Application;
import android.content.SharedPreferences;

import java.util.UUID;

public class SeeKwensApplication extends Application {

    public static final String PREFS = "prefs";
    public static final String USER_ID_KEY = "UserID";

    @Override
    public void onCreate() {
        super.onCreate();
        final SharedPreferences prefs = getApplicationContext().getSharedPreferences(PREFS, MODE_PRIVATE);
        prefs.edit().putString(USER_ID_KEY, UUID.randomUUID().toString()).apply();
    }
}
