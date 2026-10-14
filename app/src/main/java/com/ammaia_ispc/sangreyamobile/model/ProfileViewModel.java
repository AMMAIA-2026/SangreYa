package com.ammaia_ispc.sangreyamobile.model;

import com.ammaia_ispc.sangreyamobile.data.UserApiRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class ProfileViewModel {
    public interface SessionStore {
        int getUserId();

        void updateUserName(String name);
    }

    public interface Listener {
        void onStateChanged(State state);
    }

    public static final class State {
        public final UserUpdateRequest form;
        public final boolean loading;
        public final Response<AuthUser> response;
        public final Throwable failure;

        private State(UserUpdateRequest form, boolean loading,
                      Response<AuthUser> response, Throwable failure) {
            this.form = form;
            this.loading = loading;
            this.response = response;
            this.failure = failure;
        }

        public boolean isSuccess() {
            return response != null && response.isSuccessful() && response.body() != null;
        }
    }

    private final UserApiRepository repository;
    private final SessionStore sessionStore;
    private final int userId;
    private State state;

    public ProfileViewModel(UserApiRepository repository, SessionStore sessionStore) {
        this(repository, sessionStore, sessionStore.getUserId());
    }

    public ProfileViewModel(UserApiRepository repository, SessionStore sessionStore, int userId) {
        this.repository = repository;
        this.sessionStore = sessionStore;
        this.userId = userId;
    }

    public State getState() {
        return state;
    }

    public void save(UserUpdateRequest form, Listener listener) {
        state = new State(form, true, null, null);
        listener.onStateChanged(state);
        repository.updateProfile(userId, form, new Callback<AuthUser>() {
            @Override
            public void onResponse(Call<AuthUser> call, Response<AuthUser> response) {
                state = new State(form, false, response, null);
                if (state.isSuccess() && userId == sessionStore.getUserId()) {
                    sessionStore.updateUserName(response.body().getDisplayName());
                }
                listener.onStateChanged(state);
            }

            @Override
            public void onFailure(Call<AuthUser> call, Throwable throwable) {
                state = new State(form, false, null, throwable);
                listener.onStateChanged(state);
            }
        });
    }
}
