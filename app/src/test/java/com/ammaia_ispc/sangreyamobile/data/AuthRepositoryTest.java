
package com.ammaia_ispc.sangreyamobile.data;

import com.ammaia_ispc.sangreyamobile.helpers.ApiService;
import com.ammaia_ispc.sangreyamobile.model.LoginRequest;
import com.ammaia_ispc.sangreyamobile.model.LoginResponse;
import com.google.gson.Gson;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AuthRepositoryTest {
    // TC-UNIT-13
    @Test
    @SuppressWarnings("unchecked")
    public void persisteTokensDeLogin() {
        // Arrange
        String access = "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjo3fQ.Zml4dHVyZS1hY2Nlc3M";
        String refresh = "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjo3fQ.Zml4dHVyZS1yZWZyZXNo";
        LoginResponse response = new Gson().fromJson("{\"access\":\"" + access
                + "\",\"refresh\":\"" + refresh + "\",\"user\":{\"id\":7}}", LoginResponse.class);
        ApiService api = mock(ApiService.class);
        Call<LoginResponse> call = mock(Call.class);
        AuthApiRepository.SessionStore session = mock(AuthApiRepository.SessionStore.class);
        AuthApiRepository.LoginCallback callback = mock(AuthApiRepository.LoginCallback.class);
        LoginRequest request = new LoginRequest("donante@example.test", "ClaveDePrueba-123");
        when(api.login(request)).thenReturn(call);
        doAnswer(invocation -> {
            Callback<LoginResponse> result = invocation.getArgument(0);
            result.onResponse(call, Response.success(response));
            return null;
        }).when(call).enqueue(any());
        AuthApiRepository repository = new AuthApiRepository(api, session);

        // Act
        repository.login(request, callback);

        // Assert
        ArgumentCaptor<LoginResponse> saved = ArgumentCaptor.forClass(LoginResponse.class);
        verify(session).save(saved.capture());
        assertEquals(access, saved.getValue().getAccess());
        assertEquals(refresh, saved.getValue().getRefresh());
        verify(session, never()).clear();
    }
}
