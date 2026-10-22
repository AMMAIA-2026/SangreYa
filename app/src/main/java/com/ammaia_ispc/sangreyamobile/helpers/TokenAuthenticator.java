package com.ammaia_ispc.sangreyamobile.helpers;

import android.content.Context;
import android.util.Log;

import com.ammaia_ispc.sangreyamobile.model.RefreshRequest;
import com.ammaia_ispc.sangreyamobile.model.RefreshResponse;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import retrofit2.Call;

public class TokenAuthenticator implements Authenticator {

    private static final String TAG = "TokenAuthenticator";

    interface SessionStore {
        String getAccessToken();

        String getRefreshToken();

        void updateTokens(String access, String refresh);

        void expireSession();
    }

    private final SessionStore sessionStore;
    private final Supplier<ApiService> refreshApi;
    private final Consumer<IOException> networkFailure;

    public TokenAuthenticator(Context context) {
        this(new AndroidSessionStore(context), ApiClient::getPlainApiService,
                error -> Log.w(TAG, "Token refresh failed due to a network error", error));
    }

    // Package-private: tests exercise the real authenticator without Android storage/navigation.
    TokenAuthenticator(SessionStore sessionStore, Supplier<ApiService> refreshApi,
                       Consumer<IOException> networkFailure) {
        this.sessionStore = sessionStore;
        this.refreshApi = refreshApi;
        this.networkFailure = networkFailure;
    }

    @Override
    public synchronized Request authenticate(Route route, Response response) throws IOException {

        if (responseCount(response) >= 2) {
            return null;
        }

        String failedAuthHeader = response.request().header("Authorization");
        String currentAccessToken = sessionStore.getAccessToken();
        String currentAuthHeader = currentAccessToken != null ? "Bearer " + currentAccessToken : null;

        if (currentAuthHeader != null && !Objects.equals(currentAuthHeader, failedAuthHeader)) {
            return response.request().newBuilder()
                    .header("Authorization", currentAuthHeader)
                    .build();
        }

        String refreshToken = sessionStore.getRefreshToken();
        if (refreshToken == null) {
            return null;
        }

        try {
            Call<RefreshResponse> call = refreshApi.get().refreshToken(new RefreshRequest(refreshToken));
            retrofit2.Response<RefreshResponse> refreshResponse = call.execute();
            if (refreshResponse.isSuccessful() && refreshResponse.body() != null) {
                RefreshResponse body = refreshResponse.body();
                sessionStore.updateTokens(body.getAccess(), body.getRefresh());
                return response.request().newBuilder()
                        .header("Authorization", "Bearer " + body.getAccess())
                        .build();
            }
            sessionStore.expireSession();
        } catch (IOException e) {
            networkFailure.accept(e);
        }
        return null;
    }

    static int responseCount(Response response) {
        int count = 1;
        while ((response = response.priorResponse()) != null) {
            count++;
        }
        return count;
    }

    private static final class AndroidSessionStore implements SessionStore {
        private final Context context;

        private AndroidSessionStore(Context context) {
            this.context = context.getApplicationContext();
        }

        @Override
        public String getAccessToken() {
            return SessionManager.getAccessToken(context);
        }

        @Override
        public String getRefreshToken() {
            return SessionManager.getRefreshToken(context);
        }

        @Override
        public void updateTokens(String access, String refresh) {
            SessionManager.updateTokens(context, access, refresh);
        }

        @Override
        public void expireSession() {
            SessionManager.expireSession(context);
        }
    }
}
