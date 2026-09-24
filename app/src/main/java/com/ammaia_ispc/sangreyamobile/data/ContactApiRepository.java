package com.ammaia_ispc.sangreyamobile.data;

import android.content.Context;

import androidx.annotation.StringRes;

import com.ammaia_ispc.sangreyamobile.R;
import com.ammaia_ispc.sangreyamobile.helpers.ApiClient;
import com.ammaia_ispc.sangreyamobile.helpers.ApiService;
import com.ammaia_ispc.sangreyamobile.model.ContactRequest;

import retrofit2.Call;
import retrofit2.Response;
import okhttp3.ResponseBody;
import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

public final class ContactApiRepository {

    public interface ContactCallback {
        void onSuccess();

        void onValidationError(String field, String message);

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
                if (response.code() == 400) {
                    ResponseBody errorBody = response.errorBody();

                    if (errorBody != null) {
                        try {
                            JSONObject json = new JSONObject(errorBody.string());

                            if (json.has("correo_electronico")) {
                                JSONArray errores = json.getJSONArray("correo_electronico");
                                callback.onValidationError(
                                        "correo_electronico",
                                        errores.getString(0)
                                );
                            } else if (json.has("nombre_completo")) {
                                JSONArray errores = json.getJSONArray("nombre_completo");
                                callback.onValidationError(
                                        "nombre_completo",
                                        errores.getString(0)
                                );
                            } else if (json.has("motivo")) {
                                JSONArray errores = json.getJSONArray("motivo");
                                callback.onValidationError(
                                        "motivo",
                                        errores.getString(0)
                                );
                            } else if (json.has("mensaje")) {
                                JSONArray errores = json.getJSONArray("mensaje");
                                callback.onValidationError(
                                        "mensaje",
                                        errores.getString(0)
                                );
                            } else {
                                callback.onValidationError(
                                        "",
                                        "Los datos ingresados no son válidos."
                                );
                            }

                        } catch (IOException | JSONException e) {
                            callback.onValidationError(
                                    "",
                                    "Los datos ingresados no son válidos."
                            );
                        }
                    } else {
                        callback.onValidationError(
                                "",
                                "Los datos ingresados no son válidos."
                        );
                    }

                    return;
                }

                if (!response.isSuccessful()) {
                    callback.onError(R.string.contact_send_error_server);
                    return;
                }

                callback.onSuccess();
            }


            @Override
            public void onFailure(Call<Void> call, Throwable throwable) {
                call.clone().enqueue(new retrofit2.Callback<Void>() {

                    @Override
                    public void onResponse(Call<Void> retryCall, Response<Void> retryResponse) {
                        if (!retryResponse.isSuccessful()) {
                            callback.onError(R.string.contact_send_error_network);
                            return;
                        }

                        callback.onSuccess();
                    }

                    @Override
                    public void onFailure(Call<Void> retryCall, Throwable retryThrowable) {
                        callback.onError(R.string.contact_send_error_network);
                    }
                });
            }
        });
    }
}
