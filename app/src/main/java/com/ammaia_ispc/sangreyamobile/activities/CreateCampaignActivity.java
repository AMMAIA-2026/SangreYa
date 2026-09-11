package com.ammaia_ispc.sangreyamobile.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ammaia_ispc.sangreyamobile.R;
import com.ammaia_ispc.sangreyamobile.data.MockCampaignRepository;
import com.ammaia_ispc.sangreyamobile.helpers.CampaignHelper;
import com.ammaia_ispc.sangreyamobile.helpers.ExtraKeys;
import com.ammaia_ispc.sangreyamobile.model.Campaign;

import java.util.Calendar;
import java.util.Locale;

public class CreateCampaignActivity extends AppCompatActivity {
    private EditText nameInput;
    private EditText institutionInput;
    private EditText addressInput;
    private EditText startDateInput;
    private EditText endDateInput;
    private EditText startTimeInput;
    private EditText endTimeInput;
    private EditText capacityInput;
    private EditText descriptionInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CampaignHelper.configureSystemBars(this);
        setContentView(R.layout.activity_create_campaign);
        bindViews();
    }

    private void bindViews() {
        findViewById(R.id.create_campaign_back).setOnClickListener(view -> finish());

        nameInput = findViewById(R.id.create_campaign_name);
        institutionInput = findViewById(R.id.create_campaign_institution);
        addressInput = findViewById(R.id.create_campaign_address);
        startDateInput = findViewById(R.id.create_campaign_start_date);
        endDateInput = findViewById(R.id.create_campaign_end_date);
        startTimeInput = findViewById(R.id.create_campaign_start_time);
        endTimeInput = findViewById(R.id.create_campaign_end_time);
        capacityInput = findViewById(R.id.create_campaign_capacity);
        descriptionInput = findViewById(R.id.create_campaign_description);

        startDateInput.setOnClickListener(view -> showDatePicker(startDateInput));
        endDateInput.setOnClickListener(view -> showDatePicker(endDateInput));
        startTimeInput.setOnClickListener(view -> showTimePicker(startTimeInput));
        endTimeInput.setOnClickListener(view -> showTimePicker(endTimeInput));

        findViewById(R.id.create_campaign_publish).setOnClickListener(view -> publishCampaign());
    }

    private void showDatePicker(EditText target) {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(
                this,
                (picker, year, month, dayOfMonth) -> target.setText(
                        String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year)),
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void showTimePicker(EditText target) {
        Calendar calendar = Calendar.getInstance();
        new TimePickerDialog(
                this,
                (picker, hourOfDay, minute) -> target.setText(
                        String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        ).show();
    }

    private void publishCampaign() {
        if (!validateRequiredFields()) {
            return;
        }
        Campaign campaign = new Campaign(
                MockCampaignRepository.nextId(),
                nameInput.getText().toString().trim(),
                descriptionInput.getText().toString().trim(),
                addressInput.getText().toString().trim(),
                null,
                null,
                toIsoDate(startDateInput.getText().toString()),
                toIsoDate(endDateInput.getText().toString()),
                parseCapacity(),
                0,
                "Proximamente",
                "Proximamente");
        MockCampaignRepository.addCampaign(campaign);
        Toast.makeText(this, R.string.campaign_published_message, Toast.LENGTH_SHORT).show();
        finish();
    }

    private void saveDraft() {
        Toast.makeText(this, R.string.campaign_draft_saved_message, Toast.LENGTH_SHORT).show();
        finish();
    }

    private boolean validateRequiredFields() {
        if (TextUtils.isEmpty(nameInput.getText())
                || TextUtils.isEmpty(addressInput.getText())
                || TextUtils.isEmpty(startDateInput.getText())
                || TextUtils.isEmpty(endDateInput.getText())
                || TextUtils.isEmpty(capacityInput.getText())) {
            Toast.makeText(this, R.string.campaign_required_fields_message, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private Integer parseCapacity() {
        try {
            return Integer.parseInt(capacityInput.getText().toString().trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String toIsoDate(String displayDate) {
        String[] parts = displayDate.split("/");
        return parts[2] + "-" + parts[1] + "-" + parts[0];
    }
}
