package com.chinmay.seekwens;

import android.app.Application;
import android.content.SharedPreferences;

import java.util.UUID;

import javax.inject.Inject;

import toothpick.Scope;
import toothpick.Toothpick;
import toothpick.configuration.Configuration;
import toothpick.registries.FactoryRegistryLocator;
import toothpick.registries.MemberInjectorRegistryLocator;
import toothpick.smoothie.module.SmoothieApplicationModule;

public class SeeKwensApplication extends Application {

    public static final String PREFS = "prefs";
    public static final String USER_ID_KEY = "UserID";

    @Inject SharedPreferences preferences;

    @Override
    public void onCreate() {
        super.onCreate();
        Toothpick.setConfiguration(Configuration.forProduction().disableReflection());
        FactoryRegistryLocator.setRootRegistry(new com.chinmay.seekwens.FactoryRegistry());
        MemberInjectorRegistryLocator.setRootRegistry(new com.chinmay.seekwens.MemberInjectorRegistry());

        final Scope scope = Toothpick.openScopes(this);
        scope.installModules(new SmoothieApplicationModule(this));
        Toothpick.inject(this, scope);

        if (preferences.getString(USER_ID_KEY, null) == null) {
            preferences.edit().putString(USER_ID_KEY, UUID.randomUUID().toString()).apply();
        }
    }
}
