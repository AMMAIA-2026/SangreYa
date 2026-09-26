package com.ammaia_ispc.sangreyamobile.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.ammaia_ispc.sangreyamobile.R;

import android.widget.TextView;
import android.widget.Toast;

import com.ammaia_ispc.sangreyamobile.helpers.ApiClient;
import com.ammaia_ispc.sangreyamobile.model.ContactMessage;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContactDetailActivity extends AppCompatActivity {

    private int contactId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_detail);

        contactId = getIntent().getIntExtra("contact_id", -1);

        if (contactId == -1) {
            finish();
            return;
        }

        loadContact();
    }

    private void loadContact() {
        ApiClient.getApiService(this)
                .getContact(contactId)
                .enqueue(new Callback<ContactMessage>() {

                    @Override
                    public void onResponse(
                            Call<ContactMessage> call,
                            Response<ContactMessage> response) {

                        if (response.isSuccessful() && response.body() != null) {
                            ContactMessage contact = response.body();

                            ((TextView) findViewById(R.id.contact_detail_name))
                                    .setText(contact.getName());

                            ((TextView) findViewById(R.id.contact_detail_email))
                                    .setText(contact.getEmail());

                            ((TextView) findViewById(R.id.contact_detail_reason))
                                    .setText(contact.getReason());

                            ((TextView) findViewById(R.id.contact_detail_message))
                                    .setText(contact.getMessage());

                            ((TextView) findViewById(R.id.contact_detail_date))
                                    .setText(contact.getCreatedAt());

                        } else {
                            Toast.makeText(
                                    ContactDetailActivity.this,
                                    "No se pudo cargar el contacto.",
                                    Toast.LENGTH_SHORT
                            ).show();
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ContactMessage> call,
                            Throwable t) {

                        Toast.makeText(
                                ContactDetailActivity.this,
                                "No se pudo conectar con el servidor.",
                                Toast.LENGTH_SHORT
                        ).show();
                        finish();
                    }
                });
    }