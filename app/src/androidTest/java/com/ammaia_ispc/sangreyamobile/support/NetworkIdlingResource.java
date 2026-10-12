package com.ammaia_ispc.sangreyamobile.support;

import android.util.Log;

import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.idling.CountingIdlingResource;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.Request;
import okio.Timeout;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/** Tracks the adapted Call: its callback has already been dispatched to the UI thread. */
public final class NetworkIdlingResource extends CallAdapter.Factory implements IdlingResource {
    private final CountingIdlingResource counter = new CountingIdlingResource("SangreYa Retrofit");

    @Override
    public String getName() {
        return counter.getName();
    }

    @Override
    public boolean isIdleNow() {
        return counter.isIdleNow();
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback callback) {
        counter.registerIdleTransitionCallback(callback);
    }

    @Override
    public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
        if (getRawType(returnType) != Call.class) {
            return null;
        }
        @SuppressWarnings("unchecked")
        CallAdapter<Object, Call<Object>> delegate =
                (CallAdapter<Object, Call<Object>>) retrofit.nextCallAdapter(this, returnType, annotations);
        return new CallAdapter<Object, Call<Object>>() {
            @Override
            public Type responseType() {
                return delegate.responseType();
            }

            @Override
            public Call<Object> adapt(Call<Object> call) {
                return new TrackedCall<>(delegate.adapt(call));
            }
        };
    }

    private final class TrackedCall<T> implements Call<T> {
        private final Call<T> delegate;

        private TrackedCall(Call<T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public void enqueue(Callback<T> callback) {
            counter.increment();
            try {
                delegate.enqueue(new Callback<T>() {
                    @Override
                    public void onResponse(Call<T> call, Response<T> response) {
                        try {
                            Log.i("CriticalFlows", "HTTP " + response.code() + " "
                                    + call.request().url().encodedPath());
                            callback.onResponse(TrackedCall.this, response);
                        } finally {
                            counter.decrement();
                        }
                    }

                    @Override
                    public void onFailure(Call<T> call, Throwable throwable) {
                        try {
                            callback.onFailure(TrackedCall.this, throwable);
                        } finally {
                            counter.decrement();
                        }
                    }
                });
            } catch (RuntimeException exception) {
                counter.decrement();
                throw exception;
            }
        }

        @Override
        public Response<T> execute() throws IOException {
            counter.increment();
            try {
                return delegate.execute();
            } finally {
                counter.decrement();
            }
        }

        @Override public boolean isExecuted() { return delegate.isExecuted(); }
        @Override public boolean isCanceled() { return delegate.isCanceled(); }
        @Override public void cancel() { delegate.cancel(); }
        @Override public Request request() { return delegate.request(); }
        @Override public Timeout timeout() { return delegate.timeout(); }
        @Override public Call<T> clone() { return new TrackedCall<>(delegate.clone()); }
    }
}
