package com.ammaia_ispc.sangreyamobile.helpers;

import com.ammaia_ispc.sangreyamobile.data.UserApiRepository;
import com.ammaia_ispc.sangreyamobile.model.AuthUser;
import com.ammaia_ispc.sangreyamobile.model.ProfileViewModel;
import com.ammaia_ispc.sangreyamobile.model.UserUpdateRequest;

import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Callback;
import retrofit2.Response;
import okhttp3.MediaType;
import okhttp3.ResponseBody;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

public class ProfileViewModelTest {
    // TC-UNIT-05; RF-15 / HU-11; estados de carga, fallo de transporte y reintento.
    @Test
    public void errorDeRedConservaFormularioYPermiteReintentar() {
        UserApiRepository repository = mock(UserApiRepository.class);
        ProfileViewModel.SessionStore session = mock(ProfileViewModel.SessionStore.class);
        when(session.getUserId()).thenReturn(7);
        UserUpdateRequest form = new UserUpdateRequest("donante", "donante@example.test",
                "23456789", "Ana", "Prueba", "1995-04-12");
        IOException failure = new IOException("Sin conexión de prueba");
        AuthUser updated = mock(AuthUser.class);
        when(updated.getDisplayName()).thenReturn("Ana");
        doAnswer(invocation -> {
            Callback<AuthUser> callback = invocation.getArgument(2);
            callback.onFailure(null, failure);
            return null;
        }).doAnswer(invocation -> {
            Callback<AuthUser> callback = invocation.getArgument(2);
            callback.onResponse(null, Response.success(updated));
            return null;
        }).when(repository).updateProfile(eq(7), same(form), any());
        ProfileViewModel viewModel = new ProfileViewModel(repository, session);
        List<ProfileViewModel.State> states = new ArrayList<>();

        viewModel.save(form, states::add);

        assertEquals(2, states.size());
        assertTrue(states.get(0).loading);
        assertFalse(viewModel.getState().loading);
        assertFalse(viewModel.getState().isSuccess());
        assertSame(form, viewModel.getState().form);
        assertSame(failure, viewModel.getState().failure);
        assertNull(viewModel.getState().response);
        verify(session, never()).updateUserName(any());

        viewModel.save(viewModel.getState().form, states::add);

        assertEquals(4, states.size());
        assertTrue(states.get(2).loading);
        assertTrue(viewModel.getState().isSuccess());
        assertFalse(viewModel.getState().loading);
        assertNull(viewModel.getState().failure);
        verify(session).updateUserName("Ana");
    }

    // TC-UNIT-05; RF-15 / HU-11; respuesta de validación, no error de transporte.
    @Test
    public void error400ConservaFormularioSinActualizarSesion() {
        UserApiRepository repository = mock(UserApiRepository.class);
        ProfileViewModel.SessionStore session = mock(ProfileViewModel.SessionStore.class);
        when(session.getUserId()).thenReturn(7);
        UserUpdateRequest form = new UserUpdateRequest("donante", "invalido",
                "23456789", "Ana", "Prueba", "1995-04-12");
        ResponseBody body = ResponseBody.create("{\"email\":[\"Email inválido.\"]}",
                MediaType.get("application/json"));
        Response<AuthUser> error = Response.error(400, body);
        doAnswer(invocation -> {
            Callback<AuthUser> callback = invocation.getArgument(2);
            callback.onResponse(null, error);
            return null;
        }).when(repository).updateProfile(eq(7), same(form), any());
        ProfileViewModel viewModel = new ProfileViewModel(repository, session);
        List<ProfileViewModel.State> states = new ArrayList<>();

        viewModel.save(form, states::add);

        assertEquals(2, states.size());
        assertTrue(states.get(0).loading);
        assertFalse(viewModel.getState().loading);
        assertFalse(viewModel.getState().isSuccess());
        assertSame(form, viewModel.getState().form);
        assertSame(error, viewModel.getState().response);
        assertEquals(400, viewModel.getState().response.code());
        assertNull(viewModel.getState().failure);
        verify(session, never()).updateUserName(any());
        body.close();
    }

    // TC-UNIT-05
    @Test
    public void actualizaPerfilPropio() {
        // Arrange
        UserApiRepository repository = mock(UserApiRepository.class);
        ProfileViewModel.SessionStore session = mock(ProfileViewModel.SessionStore.class);
        when(session.getUserId()).thenReturn(7);
        UserUpdateRequest form = new UserUpdateRequest("donante.editado", "editado@example.test",
                "23456789", "Ana Maria", "Prueba", "1995-04-12");
        AuthUser updated = mock(AuthUser.class);
        when(updated.getDisplayName()).thenReturn("Ana Maria");
        doAnswer(invocation -> {
            Callback<AuthUser> callback = invocation.getArgument(2);
            callback.onResponse(null, Response.success(updated));
            return null;
        }).when(repository).updateProfile(eq(7), same(form), any());
        ProfileViewModel viewModel = new ProfileViewModel(repository, session);

        // Act
        viewModel.save(form, state -> { });

        // Assert
        verify(repository).updateProfile(eq(7), same(form), any());
        verify(session).updateUserName("Ana Maria");
        assertTrue(viewModel.getState().isSuccess());
        assertFalse(viewModel.getState().loading);
        assertSame(updated, viewModel.getState().response.body());
    }
}
