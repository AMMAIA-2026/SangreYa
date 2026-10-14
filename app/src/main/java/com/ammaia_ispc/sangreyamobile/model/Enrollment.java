package com.ammaia_ispc.sangreyamobile.model;

import java.io.Serializable;

public class Enrollment implements Serializable {
    private static final long serialVersionUID = 1L;

    public final int id;
    public final Campaign campaign;

    public Enrollment(int id, Campaign campaign) {
        this.id = id;
        this.campaign = campaign;
    }
}
