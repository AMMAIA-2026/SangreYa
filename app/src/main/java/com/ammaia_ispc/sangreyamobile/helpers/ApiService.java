package com.ammaia_ispc.sangreyamobile.helpers;

import com.ammaia_ispc.sangreyamobile.model.LoginRequest;
import com.ammaia_ispc.sangreyamobile.model.LoginResponse;
import com.ammaia_ispc.sangreyamobile.model.RefreshRequest;
import com.ammaia_ispc.sangreyamobile.model.RefreshResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    @POST("api/token/")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("api/token/refresh/")
    Call<RefreshResponse> refreshToken(@Body RefreshRequest request);
}
