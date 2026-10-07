package com.ammaia_ispc.sangreyamobile.data;

import com.ammaia_ispc.sangreyamobile.helpers.ApiService;
import com.ammaia_ispc.sangreyamobile.model.LoginRequest;
import com.ammaia_ispc.sangreyamobile.model.LoginResponse;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class AuthApiRepository {

    public interface SessionStore {
        void save(LoginResponse response);

        void clear();
    }

    public interface LoginCallback {
        void onSuccess(LoginResponse response);

        void onHttpError(int statusCode, ResponseBody errorBody);

        void onFailure(Throwable throwable);
    }

    private final ApiService api;
    private final SessionStore sessionStore;

    public AuthApiRepository(ApiService api, SessionStore sessionStore) {
        this.api = api;
        this.sessionStore = sessionStore;
    }

    public void login(LoginRequest request, LoginCallback callback) {
        api.login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    sessionStore.save(response.body());
                    callback.onSuccess(response.body());
                } else {
                    if (response.code() == 401) {
                        sessionStore.clear();
                    }
                    callback.onHttpError(response.code(), response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable throwable) {
                callback.onFailure(throwable);
            }
        });
    }
}
