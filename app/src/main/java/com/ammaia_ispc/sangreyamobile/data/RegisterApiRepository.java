package com.ammaia_ispc.sangreyamobile.data;

import android.content.Context;

import androidx.annotation.StringRes;

import com.ammaia_ispc.sangreyamobile.R;
import com.ammaia_ispc.sangreyamobile.helpers.ApiClient;
import com.ammaia_ispc.sangreyamobile.helpers.ApiService;
import com.ammaia_ispc.sangreyamobile.model.RegisterRequest;

import retrofit2.Call;
import retrofit2.Response;
import okhttp3.ResponseBody;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.io.IOException;

public final class RegisterApiRepository {

    public interface RegisterCallback {

        void onSuccess();

        void onValidationError(String field, String message);

        void onError(@StringRes int messageRes);
    }

    private RegisterApiRepository() {
    }

    public static void register(
            Context context,
            RegisterRequest request,
            RegisterCallback callback) {

        ApiService api = ApiClient.getPlainApiService();

        api.register(request).enqueue(new retrofit2.Callback<Void>() {

            @Override
            public void onResponse(
                    Call<Void> call,
                    Response<Void> response) {

                if (response.code() == 400) {

                    ResponseBody errorBody = response.errorBody();

                    if (errorBody != null) {
                        try {
                            JSONObject json =
                                    new JSONObject(errorBody.string());

                            if (json.has("email")) {
                                JSONArray errores =
                                        json.getJSONArray("email");

                                callback.onValidationError(
                                        "email",
                                        errores.getString(0)
                                );

                            } else if (json.has("dni")) {
                                JSONArray errores =
                                        json.getJSONArray("dni");

                                callback.onValidationError(
                                        "dni",
                                        errores.getString(0)
                                );

                            } else if (json.has("username")) {
                                JSONArray errores =
                                        json.getJSONArray("username");

                                callback.onValidationError(
                                        "username",
                                        errores.getString(0)
                                );

                            } else if (json.has("nombre")) {
                                JSONArray errores =
                                        json.getJSONArray("nombre");

                                callback.onValidationError(
                                        "nombre",
                                        errores.getString(0)
                                );

                            } else if (json.has("apellido")) {
                                JSONArray errores =
                                        json.getJSONArray("apellido");

                                callback.onValidationError(
                                        "apellido",
                                        errores.getString(0)
                                );

                            } else if (json.has("fecha_nacimiento")) {
                                JSONArray errores =
                                        json.getJSONArray("fecha_nacimiento");

                                callback.onValidationError(
                                        "fecha_nacimiento",
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
                    callback.onError(R.string.register_error_server);
                    return;
                }

                callback.onSuccess();
            }

            @Override
            public void onFailure(
                    Call<Void> call,
                    Throwable throwable) {

                call.clone().enqueue(new retrofit2.Callback<Void>() {

                    @Override
                    public void onResponse(
                            Call<Void> retryCall,
                            Response<Void> retryResponse) {

                        if (!retryResponse.isSuccessful()) {
                            callback.onError(
                                    R.string.register_error_network
                            );
                            return;
                        }

                        callback.onSuccess();
                    }

                    @Override
                    public void onFailure(
                            Call<Void> retryCall,
                            Throwable retryThrowable) {

                        callback.onError(
                                R.string.register_error_network
                        );
                    }
                });
            }
        });
    }
}
