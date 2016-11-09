package com.chinmay.seekwens.ui;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chinmay.seekwens.R;
import com.f2prateek.dart.Dart;

import butterknife.ButterKnife;
import toothpick.Toothpick;

public abstract class BaseSeeKwensFragment extends Fragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toothpick.inject(this, Toothpick.openScope(getActivity()));
        if (getArguments() != null) {
            Dart.inject(this, getArguments());
        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View contentView = View.inflate(getContext(), getLayoutId(), null);
        ButterKnife.bind(this, contentView);
        return contentView;
    }

    protected abstract @LayoutRes int getLayoutId();
}
