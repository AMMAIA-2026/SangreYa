package com.ammaia_ispc.sangreyamobile.helpers;

import android.app.Activity;
import android.widget.ImageButton;

public class NavigationHelper {

    public static void configureBackButton(Activity activity, int buttonId) {
        ImageButton btnBack = activity.findViewById(buttonId);

        btnBack.setOnClickListener(v -> activity.finish());
    }
}