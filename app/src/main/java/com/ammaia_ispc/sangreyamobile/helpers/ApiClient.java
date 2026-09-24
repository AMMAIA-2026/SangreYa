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
            retrofit = new Retrofit.Builder()
                    .baseUrl(ApiConfig.BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }


    public static ApiService getPlainApiService() {
        if (plainRetrofit == null) {
            plainRetrofit = new Retrofit.Builder()
                    .baseUrl(ApiConfig.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return plainRetrofit.create(ApiService.class);
    }
}
