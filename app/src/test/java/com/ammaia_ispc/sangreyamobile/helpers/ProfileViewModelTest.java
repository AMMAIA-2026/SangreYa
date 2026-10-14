package com.ammaia_ispc.sangreyamobile.helpers;

import com.ammaia_ispc.sangreyamobile.data.UserApiRepository;
import com.ammaia_ispc.sangreyamobile.model.AuthUser;
import com.ammaia_ispc.sangreyamobile.model.ProfileViewModel;
import com.ammaia_ispc.sangreyamobile.model.UserUpdateRequest;

import org.junit.Test;

import retrofit2.Callback;
import retrofit2.Response;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ProfileViewModelTest {
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
