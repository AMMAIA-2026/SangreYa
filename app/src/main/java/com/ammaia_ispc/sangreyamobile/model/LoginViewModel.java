package com.ammaia_ispc.sangreyamobile.model;

import com.ammaia_ispc.sangreyamobile.data.AuthApiRepository;

public final class LoginViewModel {
    private final AuthApiRepository repository;

    public LoginViewModel(AuthApiRepository repository) {
        this.repository = repository;
    }

    public static boolean hasEmptyFields(String email, String password) {
        return email == null || email.isEmpty() || password == null || password.isEmpty();
    }

    public boolean login(String email, String password, AuthApiRepository.LoginCallback callback) {
        if (hasEmptyFields(email, password)) {
            return false;
        }
        repository.login(new LoginRequest(email, password), callback);
        return true;
    }
}
