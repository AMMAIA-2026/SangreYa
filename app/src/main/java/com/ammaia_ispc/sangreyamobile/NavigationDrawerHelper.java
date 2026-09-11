package com.ammaia_ispc.sangreyamobile;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public final class NavigationDrawerHelper {
    private NavigationDrawerHelper() {
    }

    public static void configure(
            Activity activity,
            DrawerLayout drawerLayout,
            NavigationView navigationView,
            boolean standardUser,
            String user) {
        TextView greeting = activity.findViewById(R.id.header_greeting);
        TextView subtitle = activity.findViewById(R.id.header_subtitle);
        if (standardUser && !TextUtils.isEmpty(user)) {
            greeting.setText(activity.getString(R.string.logged_user_greeting, user));
            subtitle.setText(R.string.campaign_subtitle);
        } else {
            greeting.setText(R.string.guest_greeting);
            subtitle.setText(R.string.guest_subtitle);
        }

        activity.findViewById(R.id.menu_button).setOnClickListener(view ->
                drawerLayout.openDrawer(GravityCompat.START));

        navigationView.getMenu().findItem(R.id.nav_login).setVisible(!standardUser);
        navigationView.getMenu().findItem(R.id.nav_register).setVisible(!standardUser);
        navigationView.getMenu().findItem(R.id.nav_logout).setVisible(standardUser);

        navigationView.setNavigationItemSelectedListener(item -> {
            Intent intent;
            if (item.getItemId() == R.id.nav_about_us) {
                intent = new Intent(activity, AboutUsActivity.class);
                activity.startActivity(intent);
            } else if (item.getItemId() == R.id.nav_login) {
                intent = new Intent(activity, LoginActivity.class);
                activity.startActivity(intent);
            } else if (item.getItemId() == R.id.nav_register) {
                intent = new Intent(activity, RegisterActivity.class);
                activity.startActivity(intent);
            } else if (item.getItemId() == R.id.nav_logout) {
                intent = new Intent(activity, MainActivity.class);
                intent.putExtra(ExtraKeys.EXTRA_STANDARD_USER, false);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                activity.startActivity(intent);
            } else {
                return false;
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }
}
