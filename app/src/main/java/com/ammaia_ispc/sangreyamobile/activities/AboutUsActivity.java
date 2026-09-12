package com.ammaia_ispc.sangreyamobile.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.ammaia_ispc.sangreyamobile.R;
import com.ammaia_ispc.sangreyamobile.helpers.NavigationHelper;


public class AboutUsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        NavigationHelper.configureBackButton(this, R.id.btnBack);

        findViewById(R.id.tvContactanos).setOnClickListener(v ->
                startActivity(new Intent(this, ContactActivity.class)));
    }
}
