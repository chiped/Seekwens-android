package com.chinmay.seekwens.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;

public class SeeKwensDialogFragment extends DialogFragment {

    @InjectExtra @Nullable String title;
    @InjectExtra @Nullable String message;
    @InjectExtra @Nullable String positiveButton;
    @InjectExtra @Nullable String negativeButton;
    @InjectExtra boolean cancelable;
    private DialogInterface.OnClickListener positiveButtonListener;
    private DialogInterface.OnClickListener negativeButtonListener;

    private static SeeKwensDialogFragment newInstance(Bundle arguments,
                                                      DialogInterface.OnClickListener positveButtonListener,
                                                      DialogInterface.OnClickListener negativeButtonListener) {
        final SeeKwensDialogFragment fragment = new SeeKwensDialogFragment();
        fragment.positiveButtonListener = positveButtonListener;
        fragment.negativeButtonListener = negativeButtonListener;
        fragment.setArguments(arguments);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Bundle arguments = getArguments();
        if (arguments != null) {
            Dart.inject(this, arguments);
        }
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        if (title != null) {
            builder.setTitle(title);
        }
        if (message != null) {
            builder.setMessage(message);
        }
        if (positiveButton != null) {
            builder.setPositiveButton(positiveButton, positiveButtonListener);
        }
        if (negativeButton != null) {
            builder.setNegativeButton(negativeButton, negativeButtonListener);
        }
        setCancelable(cancelable);
        return builder.create();
    }

    public static class Builder {
        private String title;
        private String message;
        private String positiveButton;
        private DialogInterface.OnClickListener positiveButtonListener;
        private String negativeButton;
        private DialogInterface.OnClickListener negativeButtonListener;
        private boolean cancelable;

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder setPositiveButton(String text, DialogInterface.OnClickListener listener) {
            this.positiveButton = text;
            this.positiveButtonListener = listener;
            return this;
        }

        public Builder setNegativeButton(String text, DialogInterface.OnClickListener listener) {
            this.negativeButton = text;
            this.negativeButtonListener = listener;
            return this;
        }

        public Builder setCancelable(boolean cancelable) {
            this.cancelable = cancelable;
            return this;
        }

        public SeeKwensDialogFragment build() {
            final Bundle arguments = new Bundle();
            arguments.putString("title", title);
            arguments.putString("message", message);
            arguments.putString("positiveButton", positiveButton);
            arguments.putString("negativeButton", negativeButton);
            arguments.putBoolean("cancelable", cancelable);

            return SeeKwensDialogFragment.newInstance(arguments,
                    positiveButtonListener,
                    negativeButtonListener);
        }
    }

}
