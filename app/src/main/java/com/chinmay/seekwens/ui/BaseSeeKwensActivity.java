package com.chinmay.seekwens.ui;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.f2prateek.dart.Dart;

import butterknife.ButterKnife;
import toothpick.Scope;
import toothpick.Toothpick;

public abstract class BaseSeeKwensActivity extends AppCompatActivity {

    private Scope scope;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        scope = Toothpick.openScopes(getApplication());
        super.onCreate(savedInstanceState);
        Toothpick.inject(this, scope);
        setContentView(getlayoutId());
        ButterKnife.bind(this);
        Dart.inject(this);
    }

    protected abstract @LayoutRes int getlayoutId();
}
