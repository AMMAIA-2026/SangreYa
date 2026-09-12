package com.ammaia_ispc.sangreyamobile.model;

public class ContactMessage {
    public String name;
    public String time;
    public String reason;
    public String message;
    public boolean tracked;

    public ContactMessage(String name, String time, String reason, String message, boolean tracked) {
        this.name = name;
        this.time = time;
        this.reason = reason;
        this.message = message;
        this.tracked = tracked;
    }
}