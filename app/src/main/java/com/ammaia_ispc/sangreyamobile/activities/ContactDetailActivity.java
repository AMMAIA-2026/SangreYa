package com.ammaia_ispc.sangreyamobile.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ammaia_ispc.sangreyamobile.R;
import com.ammaia_ispc.sangreyamobile.helpers.ApiClient;
import com.ammaia_ispc.sangreyamobile.helpers.NavigationHelper;
import com.ammaia_ispc.sangreyamobile.helpers.UiHelper;
import com.ammaia_ispc.sangreyamobile.model.ContactMessage;
import com.ammaia_ispc.sangreyamobile.model.ContactTrackedRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContactDetailActivity extends AppCompatActivity {

    private int contactId;
    private boolean tracked;
    private Button trackedButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_detail);
        NavigationHelper.configureBackButton(this, R.id.contact_detail_back_button);

        contactId = getIntent().getIntExtra("contact_id", -1);

        if (contactId == -1) {
            finish();
            return;
        }

        trackedButton = findViewById(R.id.contact_detail_tracked_button);
        trackedButton.setOnClickListener(view -> updateTracked());

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

                            tracked = contact.isTracked();

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

                            updateButton();

                        } else {
                            UiHelper.showToast(
                                    ContactDetailActivity.this,
                                    "No se pudo cargar el contacto.",
                                    Toast.LENGTH_SHORT
                            );
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ContactMessage> call,
                            Throwable t) {

                        UiHelper.showToast(
                                ContactDetailActivity.this,
                                "No se pudo conectar con el servidor.",
                                Toast.LENGTH_SHORT
                        );
                        finish();
                    }
                });
    }

    private void updateTracked() {
        boolean newTracked = !tracked;

        ApiClient.getApiService(this)
                .updateContactTracked(
                        contactId,
                        new ContactTrackedRequest(newTracked)
                )
                .enqueue(new Callback<ContactMessage>() {

                    @Override
                    public void onResponse(
                            Call<ContactMessage> call,
                            Response<ContactMessage> response) {

                        if (response.isSuccessful() && response.body() != null) {
                            tracked = response.body().isTracked();
                            updateButton();
                        } else {
                            UiHelper.showToast(
                                    ContactDetailActivity.this,
                                    "No se pudo actualizar el seguimiento.",
                                    Toast.LENGTH_SHORT
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ContactMessage> call,
                            Throwable t) {

                        UiHelper.showToast(
                                ContactDetailActivity.this,
                                "No se pudo conectar con el servidor.",
                                Toast.LENGTH_SHORT
                        );
                    }
                });
    }

    private void updateButton() {
        trackedButton.setText(
                tracked ? "Desmarcar como seguido" : "Marcar como seguido"
        );
    }
}
