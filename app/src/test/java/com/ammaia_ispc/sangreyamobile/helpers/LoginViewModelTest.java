package com.ammaia_ispc.sangreyamobile.helpers;

import com.ammaia_ispc.sangreyamobile.data.AuthApiRepository;
import com.ammaia_ispc.sangreyamobile.model.LoginViewModel;
import com.ammaia_ispc.sangreyamobile.model.LoginRequest;
import com.ammaia_ispc.sangreyamobile.model.LoginResponse;
import com.google.gson.Gson;

import okhttp3.MediaType;
import okhttp3.ResponseBody;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginViewModelTest {
    // TC-UNIT-12; RF-02 / HU-02; variante de transporte de TC-AUTH-18.
    @Test
    @SuppressWarnings("unchecked")
    public void loginSinRedNotificaFalloSinModificarSesion() {
        ApiService api = mock(ApiService.class);
        Call<LoginResponse> call = mock(Call.class);
        AuthApiRepository.SessionStore session = mock(AuthApiRepository.SessionStore.class);
        AuthApiRepository.LoginCallback callback = mock(AuthApiRepository.LoginCallback.class);
        IOException failure = new IOException("Sin conexión de prueba");
        when(api.login(any(LoginRequest.class))).thenReturn(call);
        doAnswer(invocation -> {
            Callback<LoginResponse> result = invocation.getArgument(0);
            result.onFailure(call, failure);
            return null;
        }).when(call).enqueue(any());
        LoginViewModel viewModel = new LoginViewModel(new AuthApiRepository(api, session));

        assertTrue(viewModel.login("donante@example.test", "ClaveDePrueba-123", callback));

        verify(callback).onFailure(failure);
        verifyNoMoreInteractions(callback);
        verifyNoInteractions(session);
    }

    // TC-UNIT-12; RF-02 / HU-02 / CA-06 / RNF-04.
    @Test
    @SuppressWarnings("unchecked")
    public void login401LimpiaSesionYNotificaErrorHttp() {
        ApiService api = mock(ApiService.class);
        Call<LoginResponse> call = mock(Call.class);
        AuthApiRepository.SessionStore session = mock(AuthApiRepository.SessionStore.class);
        AuthApiRepository.LoginCallback callback = mock(AuthApiRepository.LoginCallback.class);
        ResponseBody error = ResponseBody.create("{\"detail\":\"Credenciales inválidas.\"}",
                MediaType.get("application/json"));
        when(api.login(any(LoginRequest.class))).thenReturn(call);
        doAnswer(invocation -> {
            Callback<LoginResponse> result = invocation.getArgument(0);
            result.onResponse(call, Response.error(401, error));
            return null;
        }).when(call).enqueue(any());
        LoginViewModel viewModel = new LoginViewModel(new AuthApiRepository(api, session));

        assertTrue(viewModel.login("donante@example.test", "ClaveDePrueba-123", callback));

        verify(session).clear();
        verify(callback).onHttpError(401, error);
        verifyNoMoreInteractions(session, callback);
        error.close();
    }

    // TC-UNIT-12; RF-02 / HU-02 / CA-05 / RNF-04.
    @Test
    @SuppressWarnings("unchecked")
    public void loginExitosoPersisteSesionYNotificaResultado() {
        ApiService api = mock(ApiService.class);
        Call<LoginResponse> call = mock(Call.class);
        AuthApiRepository.SessionStore session = mock(AuthApiRepository.SessionStore.class);
        AuthApiRepository.LoginCallback callback = mock(AuthApiRepository.LoginCallback.class);
        LoginResponse response = new Gson().fromJson(
                "{\"access\":\"access-test\",\"refresh\":\"refresh-test\",\"user\":{\"id\":7}}",
                LoginResponse.class);
        when(api.login(any(LoginRequest.class))).thenReturn(call);
        doAnswer(invocation -> {
            Callback<LoginResponse> result = invocation.getArgument(0);
            result.onResponse(call, Response.success(response));
            return null;
        }).when(call).enqueue(any());
        LoginViewModel viewModel = new LoginViewModel(new AuthApiRepository(api, session));

        assertTrue(viewModel.login("donante@example.test", "ClaveDePrueba-123", callback));

        verify(session).save(response);
        verify(callback).onSuccess(response);
        verifyNoMoreInteractions(session, callback);
    }

    // TC-UNIT-12
    @Test
    public void rechazaCamposVacios() {
        // Arrange
        ApiService api = mock(ApiService.class);
        AuthApiRepository.SessionStore session = mock(AuthApiRepository.SessionStore.class);
        AuthApiRepository.LoginCallback callback = mock(AuthApiRepository.LoginCallback.class);
        LoginViewModel viewModel = new LoginViewModel(new AuthApiRepository(api, session));

        // Act
        boolean acceptsEmptyEmail = viewModel.login("", "ClaveDePrueba-123", callback);
        boolean acceptsEmptyPassword = viewModel.login("donante@example.test", "", callback);
        boolean acceptsBothEmpty = viewModel.login("", "", callback);

        // Assert
        assertFalse(acceptsEmptyEmail || acceptsEmptyPassword || acceptsBothEmpty);
        verifyNoInteractions(api, session, callback);
    }
}

