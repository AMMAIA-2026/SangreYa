package com.ammaia_ispc.sangreyamobile.helpers;

import com.ammaia_ispc.sangreyamobile.model.LoginRequest;
import com.ammaia_ispc.sangreyamobile.model.LoginResponse;
import com.ammaia_ispc.sangreyamobile.model.RefreshRequest;
import com.ammaia_ispc.sangreyamobile.model.RefreshResponse;
import com.ammaia_ispc.sangreyamobile.model.AuthUser;
import com.ammaia_ispc.sangreyamobile.model.UserUpdateRequest;
import com.ammaia_ispc.sangreyamobile.model.HealthCenterResponse;
import com.ammaia_ispc.sangreyamobile.model.ContactRequest;
import com.ammaia_ispc.sangreyamobile.model.PasswordRecoveryRequest;
import com.ammaia_ispc.sangreyamobile.model.PasswordRecoveryResponse;
import com.ammaia_ispc.sangreyamobile.model.ContactMessage;
import com.ammaia_ispc.sangreyamobile.model.ContactTrackedRequest;
import com.ammaia_ispc.sangreyamobile.model.RegisterRequest;

import java.util.List;

import retrofit2.Call;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface ApiService {

    @POST("api/token/")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("usuarios/registro/")
    Call<Void> register(@Body RegisterRequest request);

    @POST("api/token/refresh/")
    Call<RefreshResponse> refreshToken(@Body RefreshRequest request);

    @GET("usuarios/{usuarioId}/")
    Call<AuthUser> getUserProfile(@Path("usuarioId") int userId);

    @GET("usuarios/")
    Call<List<AuthUser>> getUsers();

    @PUT("usuarios/{usuarioId}/")
    Call<AuthUser> updateUserProfile(
            @Path("usuarioId") int userId,
            @Body UserUpdateRequest request);


    @GET("centros-salud/")
    Call<List<HealthCenterResponse>> getHealthCenters();


    @POST("contactos/")
    Call<Void> sendContact(@Body ContactRequest request);

    @GET("contactos/")
    Call<List<ContactMessage>> getContacts();

    @PUT("contactos/{contactoId}/")
    Call<ContactMessage> updateContactTracked(
            @Path("contactoId") int contactId,
            @Body ContactTrackedRequest request);


    @POST("usuarios/recuperar-password/")
    Call<PasswordRecoveryResponse> recoverPassword(
            @Body PasswordRecoveryRequest request);

    @GET("campanias/")
    Call<ResponseBody> getCampaigns();

    @GET("campanias/{campaignId}/")
    Call<ResponseBody> getCampaign(@Path("campaignId") int campaignId);

    @POST("inscripciones/campanias/{campaignId}/")
    Call<ResponseBody> enrollInCampaign(@Path("campaignId") int campaignId);

    @GET("inscripciones/mis-inscripciones/")
    Call<ResponseBody> getMyEnrollments();

    @GET("inscripciones/campanias/{campaignId}/")
    Call<ResponseBody> getCampaignEnrollments(@Path("campaignId") int campaignId);

    @DELETE("inscripciones/{enrollmentId}/")
    Call<Void> cancelEnrollment(@Path("enrollmentId") int enrollmentId);

    @GET("dashboard/")
    Call<ResponseBody> getDashboard();
}
