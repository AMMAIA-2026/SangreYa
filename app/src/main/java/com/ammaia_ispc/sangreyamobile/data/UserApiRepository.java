package com.ammaia_ispc.sangreyamobile.data;

import android.content.Context;

import com.ammaia_ispc.sangreyamobile.helpers.ApiClient;
import com.ammaia_ispc.sangreyamobile.helpers.ApiService;
import com.ammaia_ispc.sangreyamobile.model.AuthUser;
import com.ammaia_ispc.sangreyamobile.model.UserUpdateRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class UserApiRepository {

    public interface UpdateUserCallback {
        void onSuccess(AuthUser updatedUser);

        void onError(String message);
    }

    private UserApiRepository() {
    }

    public static void updateUser(
            Context context,
            int userId,
            String username,
            String email,
            String dni,
            String nombre,
            String apellido,
            String fechaNacimiento,
            UpdateUserCallback callback) {

        ApiService api = ApiClient.getApiService(context);

        UserUpdateRequest request = new UserUpdateRequest(
                username, email, dni, nombre, apellido, fechaNacimiento);

        api.updateUserProfile(userId, request).enqueue(new Callback<AuthUser>() {
            @Override
            public void onResponse(Call<AuthUser> call, Response<AuthUser> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onError("No se pudo actualizar el usuario.");
                    return;
                }
                callback.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call<AuthUser> call, Throwable throwable) {
                callback.onError("No se pudo conectar con el servidor.");
            }
        });
    }
}