package com.ammaia_ispc.sangreyamobile.data;

import com.ammaia_ispc.sangreyamobile.helpers.ApiService;
import com.ammaia_ispc.sangreyamobile.model.AuthUser;
import com.ammaia_ispc.sangreyamobile.model.UserUpdateRequest;

import retrofit2.Callback;

public class UserApiRepository {
    private final ApiService api;

    public UserApiRepository(ApiService api) {
        this.api = api;
    }

    public void updateProfile(int userId, UserUpdateRequest request, Callback<AuthUser> callback) {
        api.updateUserProfile(userId, request).enqueue(callback);
    }
}
