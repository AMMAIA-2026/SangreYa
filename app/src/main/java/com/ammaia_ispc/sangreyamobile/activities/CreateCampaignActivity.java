package com.ammaia_ispc.sangreyamobile.activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ammaia_ispc.sangreyamobile.R;
import com.ammaia_ispc.sangreyamobile.data.MockCampaignRepository;
import com.ammaia_ispc.sangreyamobile.data.MockHealthCenterRepository;
import com.ammaia_ispc.sangreyamobile.helpers.CampaignHelper;
import com.ammaia_ispc.sangreyamobile.helpers.ExtraKeys;
import com.ammaia_ispc.sangreyamobile.helpers.SessionManager;
import com.ammaia_ispc.sangreyamobile.model.Campaign;
import com.ammaia_ispc.sangreyamobile.model.HealthCenter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CreateCampaignActivity extends AppCompatActivity {
    private EditText nameInput;
    private Spinner healthCenterInput;
    private final List<HealthCenter> healthCenterOptions = new ArrayList<>();
    private EditText addressInput;
    private EditText startDateInput;
    private EditText endDateInput;
    private EditText startTimeInput;
    private EditText endTimeInput;
    private EditText capacityInput;
    private EditText descriptionInput;
    private Campaign editingCampaign;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!SessionManager.requireAdmin(this)) {
            return;
        }
        CampaignHelper.configureSystemBars(this);
        editingCampaign = (Campaign) getIntent().getSerializableExtra(ExtraKeys.EXTRA_CAMPAIGN);
        setContentView(R.layout.activity_create_campaign);
        bindViews();
    }

    private void bindViews() {
        findViewById(R.id.create_campaign_back).setOnClickListener(view -> finish());

        nameInput = findViewById(R.id.create_campaign_name);
        healthCenterInput = findViewById(R.id.create_campaign_health_center);
        setupHealthCenterOptions();
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

        if (editingCampaign != null) {
            prefillForEdit();
        }
    }

    private void setupHealthCenterOptions() {
        healthCenterOptions.clear();
        healthCenterOptions.add(null);

        List<String> labels = new ArrayList<>();
        labels.add(getString(R.string.health_center_none_option));

        for (HealthCenter center : MockHealthCenterRepository.getHealthCenters()) {
            healthCenterOptions.add(center);
            labels.add(center.name + " — " + center.neighborhood + ", " + center.city);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, labels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        healthCenterInput.setAdapter(adapter);
    }

    private HealthCenter selectedHealthCenter() {
        int position = healthCenterInput.getSelectedItemPosition();
        if (position < 0 || position >= healthCenterOptions.size()) {
            return null;
        }
        return healthCenterOptions.get(position);
    }

    private void prefillForEdit() {
        ((TextView) findViewById(R.id.create_campaign_header_title)).setText(R.string.edit_campaign_title);
        ((TextView) findViewById(R.id.create_campaign_header_subtitle)).setText(R.string.edit_campaign_subtitle);
        ((Button) findViewById(R.id.create_campaign_publish)).setText(R.string.btn_save_changes);

        View deleteButton = findViewById(R.id.create_campaign_delete);
        deleteButton.setVisibility(View.VISIBLE);
        deleteButton.setOnClickListener(view -> confirmDeleteCampaign());

        nameInput.setText(editingCampaign.title);
        addressInput.setText(editingCampaign.location);
        descriptionInput.setText(editingCampaign.description);
        startDateInput.setText(CampaignHelper.toDisplayDate(editingCampaign.startDate));
        endDateInput.setText(CampaignHelper.toDisplayDate(editingCampaign.endDate));
        if (editingCampaign.maximumCapacity != null) {
            capacityInput.setText(String.valueOf(editingCampaign.maximumCapacity));
        }

        if (editingCampaign.healthCenter != null) {
            for (int index = 0; index < healthCenterOptions.size(); index++) {
                HealthCenter option = healthCenterOptions.get(index);
                if (option != null && option.id == editingCampaign.healthCenter.id) {
                    healthCenterInput.setSelection(index);
                    break;
                }
            }
        }

        if ("Activa".equals(editingCampaign.calculatedStatus)) {
            findViewById(R.id.create_campaign_active_alert).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.create_campaign_active_alert_text)).setText(
                    getString(R.string.active_campaign_alert, editingCampaign.totalRegistered));
        }
    }

    private void confirmDeleteCampaign() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_campaign_confirm_title)
                .setMessage(R.string.delete_campaign_confirm_message)
                .setPositiveButton(R.string.delete_campaign_confirm_action, (dialog, which) -> deleteCampaign())
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    private void deleteCampaign() {
        MockCampaignRepository.deleteCampaign(editingCampaign.id);
        Toast.makeText(this, R.string.campaign_deleted_message, Toast.LENGTH_SHORT).show();
        finish();
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

        String isoStartDate = CampaignHelper.toIsoDate(startDateInput.getText().toString());
        String isoEndDate = CampaignHelper.toIsoDate(endDateInput.getText().toString());
        HealthCenter selectedCenter = selectedHealthCenter();
        Integer selectedCenterId = selectedCenter != null ? selectedCenter.id : null;

        if (editingCampaign != null) {
            Campaign updated = new Campaign(
                    editingCampaign.id,
                    nameInput.getText().toString().trim(),
                    descriptionInput.getText().toString().trim(),
                    addressInput.getText().toString().trim(),
                    selectedCenterId,
                    selectedCenter,
                    isoStartDate,
                    isoEndDate,
                    parseCapacity(),
                    editingCampaign.totalRegistered,
                    editingCampaign.campaignStatus,
                    editingCampaign.calculatedStatus);
            MockCampaignRepository.updateCampaign(updated);
            Toast.makeText(this, R.string.campaign_updated_message, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Campaign campaign = new Campaign(
                MockCampaignRepository.nextId(),
                nameInput.getText().toString().trim(),
                descriptionInput.getText().toString().trim(),
                addressInput.getText().toString().trim(),
                selectedCenterId,
                selectedCenter,
                isoStartDate,
                isoEndDate,
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

}
