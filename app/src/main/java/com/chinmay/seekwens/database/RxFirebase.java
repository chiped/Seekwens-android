package com.chinmay.seekwens.database;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;

import rx.Observable;
import rx.Subscriber;

public class RxFirebase {

    public static <T> Observable<T> create(DatabaseReference ref, Class<T> clazz) {
        return Observable.create(new ValueOnSubscribe(ref, clazz));
    }

    private static class ValueOnSubscribe<T> implements Observable.OnSubscribe<Object> {
        private final DatabaseReference ref;
        private Class<T> clazz;

        public ValueOnSubscribe(DatabaseReference ref, Class<T> clazz) {
            this.ref = ref;
            this.clazz = clazz;
        }

        public void call(final Subscriber<Object> subscriber) {
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (subscriber.isUnsubscribed()) {
                        return;
                    }
                    T t = dataSnapshot.getValue(clazz);
                    subscriber.onNext(t);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    if (subscriber.isUnsubscribed()) {
                        return;
                    }
                    subscriber.onError(new IOException(databaseError.toException()));
                }
            });
        }
    }
}
