package com.ammaia_ispc.sangreyamobile.model;

import java.util.List;

public class MyEnrollments {
    public final List<Enrollment> current;
    public final List<Enrollment> historical;

    public MyEnrollments(List<Enrollment> current, List<Enrollment> historical) {
        this.current = current;
        this.historical = historical;
    }
}
