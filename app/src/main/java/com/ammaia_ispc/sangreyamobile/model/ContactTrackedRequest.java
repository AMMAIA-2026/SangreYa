package com.ammaia_ispc.sangreyamobile.model;

public class ContactTrackedRequest {

    private final boolean tracked;

    public ContactTrackedRequest(boolean tracked) {
        this.tracked = tracked;
    }

    public boolean isTracked() {
        return tracked;
    }
}