package com.ammaia_ispc.sangreyamobile.model;

public class LoginResponse {

    private String access;
    private String refresh;
    private AuthUser user;

    public String getAccess() {
        return access;
    }

    public String getRefresh() {
        return refresh;
    }

    public AuthUser getUser() {
        return user;
    }
}
