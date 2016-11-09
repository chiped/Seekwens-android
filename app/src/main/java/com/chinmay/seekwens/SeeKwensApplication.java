package com.chinmay.seekwens;

import android.app.Application;
import android.content.SharedPreferences;

import java.util.UUID;

import toothpick.Scope;
import toothpick.Toothpick;
import toothpick.configuration.Configuration;
import toothpick.smoothie.module.SmoothieActivityModule;
import toothpick.smoothie.module.SmoothieApplicationModule;

public class SeeKwensApplication extends Application {

    public static final String PREFS = "prefs";
    public static final String USER_ID_KEY = "UserID";

    private Scope scope;

    @Override
    public void onCreate() {
        super.onCreate();
        final SharedPreferences prefs = getApplicationContext().getSharedPreferences(PREFS, MODE_PRIVATE);
        if (prefs.getString(USER_ID_KEY, null) == null) {
            prefs.edit().putString(USER_ID_KEY, UUID.randomUUID().toString()).apply();
        }
        Toothpick.setConfiguration(Configuration.forProduction());

        scope = Toothpick.openScopes(this);
        scope.installModules(new SmoothieApplicationModule(this));
    }
}
