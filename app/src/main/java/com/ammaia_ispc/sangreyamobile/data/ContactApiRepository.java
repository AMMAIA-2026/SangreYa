package com.ammaia_ispc.sangreyamobile.data;

import android.content.Context;

import androidx.annotation.StringRes;

import com.ammaia_ispc.sangreyamobile.R;
import com.ammaia_ispc.sangreyamobile.helpers.ApiClient;
import com.ammaia_ispc.sangreyamobile.helpers.ApiService;
import com.ammaia_ispc.sangreyamobile.model.ContactRequest;

import retrofit2.Call;
import retrofit2.Response;

public final class ContactApiRepository {

    public interface ContactCallback {
        void onSuccess();

        void onError(@StringRes int messageRes);
    }

    private ContactApiRepository() {
    }

    public static void sendContact(
            Context context,
            ContactRequest request,
            ContactCallback callback) {

        ApiService api = ApiClient.getApiService(context);

        api.sendContact(request).enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!response.isSuccessful()) {
                    callback.onError(R.string.contact_send_error_server);
                    return;
                }

                callback.onSuccess();
            }


            @Override
            public void onFailure(Call<Void> call, Throwable throwable) {
                callback.onError(R.string.contact_send_error_network);
            }
        });
    }
}
