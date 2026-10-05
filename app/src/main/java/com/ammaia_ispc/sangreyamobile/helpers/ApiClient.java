package com.ammaia_ispc.sangreyamobile.helpers;

import android.content.Context;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClient {

    private static Retrofit retrofit;
    private static Retrofit plainRetrofit;

    private ApiClient() {
    }

    public static ApiService getApiService(Context context) {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new AuthInterceptor(context))
                    .authenticator(new TokenAuthenticator(context))
                    .build();
            retrofit = createRetrofit(ApiConfig.BASE_URL, client);
        }
        return retrofit.create(ApiService.class);
    }


    public static ApiService getPlainApiService() {
        if (plainRetrofit == null) {
            plainRetrofit = createRetrofit(ApiConfig.BASE_URL, new OkHttpClient());
        }
        return plainRetrofit.create(ApiService.class);
    }

    public static ApiService createApiService(String baseUrl, OkHttpClient client) {
        return createRetrofit(baseUrl, client).create(ApiService.class);
    }

    private static Retrofit createRetrofit(String baseUrl, OkHttpClient client) {
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}
