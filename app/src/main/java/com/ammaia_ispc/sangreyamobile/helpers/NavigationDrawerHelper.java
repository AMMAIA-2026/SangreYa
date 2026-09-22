package com.ammaia_ispc.sangreyamobile.helpers;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.ammaia_ispc.sangreyamobile.activities.AdminCampaignListActivity;
import com.google.android.material.navigation.NavigationView;

import com.ammaia_ispc.sangreyamobile.R;
import com.ammaia_ispc.sangreyamobile.activities.AboutUsActivity;
import com.ammaia_ispc.sangreyamobile.activities.LoginActivity;
import com.ammaia_ispc.sangreyamobile.activities.RegisterActivity;
import com.ammaia_ispc.sangreyamobile.activities.ContactActivity;

public final class NavigationDrawerHelper {
    private NavigationDrawerHelper() {
    }

    public static void configure(
            Activity activity,
            DrawerLayout drawerLayout,
            NavigationView navigationView,
            boolean standardUser,
            String user,
            String role) {
        TextView greeting = activity.findViewById(R.id.header_greeting);
        TextView subtitle = activity.findViewById(R.id.header_subtitle);
        boolean admin = ExtraKeys.ROLE_ADMIN.equals(role);
        if ((standardUser) && !TextUtils.isEmpty(user)) {
            greeting.setText(activity.getString(R.string.logged_user_greeting, user));
            subtitle.setText(R.string.campaign_subtitle);
        } else if((admin) && !TextUtils.isEmpty(user)) {
            greeting.setText(activity.getString(R.string.logged_user_greeting, user));
        } else {
            greeting.setText(R.string.guest_greeting);
            subtitle.setText(R.string.guest_subtitle);
        }

        activity.findViewById(R.id.menu_button).setOnClickListener(view ->
                drawerLayout.openDrawer(GravityCompat.START));

        navigationView.getMenu().findItem(R.id.nav_login).setVisible(!standardUser && !admin);
        navigationView.getMenu().findItem(R.id.nav_register).setVisible(!standardUser && !admin);
        navigationView.getMenu().findItem(R.id.nav_logout).setVisible(standardUser || admin);
        navigationView.getMenu().findItem(R.id.nav_campaigns).setVisible(admin);

        navigationView.setNavigationItemSelectedListener(item -> {
            Intent intent;
            if (item.getItemId() == R.id.nav_about_us) {
                intent = new Intent(activity, AboutUsActivity.class);
                activity.startActivity(intent);
            } else  if (item.getItemId() == R.id.nav_campaigns) {
                intent = new Intent(activity, AdminCampaignListActivity.class);
                intent.putExtra(ExtraKeys.EXTRA_USER, user);
                activity.startActivity(intent);
            } else if (item.getItemId() == R.id.nav_contact) {
                intent = new Intent(activity, ContactActivity.class);
                activity.startActivity(intent);
            } else if (item.getItemId() == R.id.nav_login) {
                intent = new Intent(activity, LoginActivity.class);
                activity.startActivity(intent);
            } else if (item.getItemId() == R.id.nav_register) {
                intent = new Intent(activity, RegisterActivity.class);
                activity.startActivity(intent);
            } else if (item.getItemId() == R.id.nav_logout) {
                SessionManager.logout(activity);
            } else {
                return false;
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }
}
