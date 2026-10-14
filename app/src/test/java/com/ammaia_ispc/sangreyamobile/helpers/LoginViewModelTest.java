package com.ammaia_ispc.sangreyamobile.helpers;

import com.ammaia_ispc.sangreyamobile.data.AuthApiRepository;
import com.ammaia_ispc.sangreyamobile.model.LoginViewModel;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

public class LoginViewModelTest {
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

