package com.ammaia_ispc.sangreyamobile.helpers;

import android.content.Context;
import android.util.Log;

import com.ammaia_ispc.sangreyamobile.model.RefreshRequest;
import com.ammaia_ispc.sangreyamobile.model.RefreshResponse;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import retrofit2.Call;

public class TokenAuthenticator implements Authenticator {

    private static final String TAG = "TokenAuthenticator";

    private final Context context;

    public TokenAuthenticator(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public synchronized Request authenticate(Route route, Response response) throws IOException {

        if (responseCount(response) >= 2) {
            return null;
        }

        String failedAuthHeader = response.request().header("Authorization");
        String currentAccessToken = SessionManager.getAccessToken(context);
        String currentAuthHeader = currentAccessToken != null ? "Bearer " + currentAccessToken : null;

        if (currentAuthHeader != null && !Objects.equals(currentAuthHeader, failedAuthHeader)) {
            return response.request().newBuilder()
                    .header("Authorization", currentAuthHeader)
                    .build();
        }

        String refreshToken = SessionManager.getRefreshToken(context);
        if (refreshToken == null) {
            return null;
        }

        try {
            Call<RefreshResponse> call = ApiClient.getPlainApiService().refreshToken(new RefreshRequest(refreshToken));
            retrofit2.Response<RefreshResponse> refreshResponse = call.execute();
            if (refreshResponse.isSuccessful() && refreshResponse.body() != null) {
                RefreshResponse body = refreshResponse.body();
                SessionManager.updateTokens(context, body.getAccess(), body.getRefresh());
                return response.request().newBuilder()
                        .header("Authorization", "Bearer " + body.getAccess())
                        .build();
            }
            SessionManager.expireSession(context);
        } catch (IOException e) {
            Log.w(TAG, "Token refresh failed due to a network error", e);
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
}
