package com.ammaia_ispc.sangreyamobile.activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.ammaia_ispc.sangreyamobile.R;
import com.ammaia_ispc.sangreyamobile.helpers.ApiClient;
import com.ammaia_ispc.sangreyamobile.helpers.CampaignHelper;
import com.ammaia_ispc.sangreyamobile.helpers.NavigationDrawerHelper;
import com.ammaia_ispc.sangreyamobile.helpers.SessionManager;
import com.ammaia_ispc.sangreyamobile.model.AuthUser;
import com.ammaia_ispc.sangreyamobile.model.UserUpdateRequest;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {
    private static final Pattern USERNAME_PATTERN = Pattern.compile(
            "^[\\p{L}\\p{N}_.@+\\-]+$");
    private static final Pattern NAME_PATTERN = Pattern.compile(
            "^[A-Za-zÁÉÍÓÚáéíóúÑñÜü]+(?: [A-Za-zÁÉÍÓÚáéíóúÑñÜü]+)*$");
    private static final String API_DATE_FORMAT = "yyyy-MM-dd";
    private static final String DISPLAY_DATE_FORMAT = "dd/MM/yyyy";

    private EditText usernameInput;
    private EditText emailInput;
    private EditText dniInput;
    private EditText nameInput;
    private EditText lastNameInput;
    private EditText birthDateInput;
    private Button saveButton;
    private ProgressBar loadingIndicator;
    private LinearLayout profileForm;
    private TextView messageView;
    private String birthDate;
    private String originalBirthDate;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CampaignHelper.configureSystemBars(this);
        setContentView(R.layout.activity_profile);

        bindViews();
        configureNavigation();
        birthDateInput.setOnTouchListener((view, event) -> {
            boolean onCalendarIcon = event.getX()
                    >= birthDateInput.getWidth() - birthDateInput.getCompoundPaddingRight();
            if (onCalendarIcon) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    syncBirthDateFromInput();
                    openDatePicker();
                }
                return true;
            }
            return false;
        });
        saveButton.setOnClickListener(view -> saveProfile());
        loadProfile();
    }

    private void bindViews() {
        usernameInput = findViewById(R.id.profile_username);
        emailInput = findViewById(R.id.profile_email);
        dniInput = findViewById(R.id.profile_dni);
        nameInput = findViewById(R.id.profile_name);
        lastNameInput = findViewById(R.id.profile_last_name);
        birthDateInput = findViewById(R.id.profile_birth_date);
        saveButton = findViewById(R.id.profile_save);
        loadingIndicator = findViewById(R.id.profile_loading);
        profileForm = findViewById(R.id.profile_form);
        messageView = findViewById(R.id.profile_message);
    }

    private void configureNavigation() {
        DrawerLayout drawerLayout = findViewById(R.id.profile_drawer);
        NavigationView navigationView = findViewById(R.id.profile_navigation_view);
        boolean standardUser = !SessionManager.isAdmin(this);
        NavigationDrawerHelper.configure(
                this,
                drawerLayout,
                navigationView,
                standardUser,
                SessionManager.getUserName(this),
                SessionManager.getUserRole(this));
    }

    private void loadProfile() {
        userId = SessionManager.getUserId(this);
        if (userId < 1) {
            showMessage(R.string.profile_session_error);
            return;
        }

        setLoading(true);
        ApiClient.getApiService(this).getUserProfile(userId).enqueue(new Callback<AuthUser>() {
            @Override
            public void onResponse(Call<AuthUser> call, Response<AuthUser> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    populateProfile(response.body());
                    clearMessage();
                } else {
                    handleResponseError(response, false);
                }
            }

            @Override
            public void onFailure(Call<AuthUser> call, Throwable throwable) {
                setLoading(false);
                showMessage(R.string.profile_load_error);
            }
        });
    }

    private void populateProfile(AuthUser user) {
        usernameInput.setText(safeValue(user.getUsername()));
        emailInput.setText(safeValue(user.getEmail()));
        dniInput.setText(safeValue(user.getDni()));
        nameInput.setText(safeValue(user.getNombre()));
        lastNameInput.setText(safeValue(user.getApellido()));
        setBirthDate(user.getFechaNacimiento());
        originalBirthDate = birthDate;
    }

    private void setBirthDate(String isoDate) {
        birthDate = isoDate;
        if (isValidIsoDate(isoDate)) {
            birthDateInput.setText(formatDate(isoDate, API_DATE_FORMAT, DISPLAY_DATE_FORMAT));
        } else {
            birthDateInput.setText("");
        }
    }

    private void openDatePicker() {
        Calendar selectedDate = Calendar.getInstance();
        Date parsedDate = parseDate(birthDate, API_DATE_FORMAT);
        if (parsedDate != null) {
            selectedDate.setTime(parsedDate);
        }

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (DatePicker view, int year, int month, int dayOfMonth) -> {
                    birthDate = String.format(
                            Locale.US,
                            "%04d-%02d-%02d",
                            year,
                            month + 1,
                            dayOfMonth);
                    birthDateInput.setText(String.format(
                            Locale.getDefault(),
                            "%02d/%02d/%04d",
                            dayOfMonth,
                            month + 1,
                            year));
                    birthDateInput.setError(null);
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void saveProfile() {
        syncBirthDateFromInput();
        clearFieldErrors();
        clearMessage();

        if (!validateForm()) {
            return;
        }

        UserUpdateRequest request = new UserUpdateRequest(
                valueOf(usernameInput),
                valueOf(emailInput),
                valueOf(dniInput),
                valueOf(nameInput),
                valueOf(lastNameInput),
                birthDate);

        if (!TextUtils.equals(birthDate, originalBirthDate)
                && !isEnrollmentAgeAllowed(birthDate)) {
            showAgeWarning(request);
            return;
        }

        submitProfile(request);
    }

    private void showAgeWarning(UserUpdateRequest request) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.profile_age_warning_title)
                .setMessage(R.string.profile_age_warning_message)
                .setNegativeButton(R.string.profile_age_warning_cancel, null)
                .setPositiveButton(
                        R.string.profile_age_warning_confirm,
                        (dialog, which) -> submitProfile(request))
                .show();
    }

    private void submitProfile(UserUpdateRequest request) {
        setLoading(true);

        ApiClient.getApiService(this).updateUserProfile(userId, request)
                .enqueue(new Callback<AuthUser>() {
                    @Override
                    public void onResponse(Call<AuthUser> call, Response<AuthUser> response) {
                        setLoading(false);
                        if (response.isSuccessful() && response.body() != null) {
                            populateProfile(response.body());
                            SessionManager.updateUserName(
                                    ProfileActivity.this,
                                    response.body().getDisplayName());
                            configureNavigation();
                            Toast.makeText(
                                    ProfileActivity.this,
                                    R.string.profile_updated_message,
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            handleResponseError(response, true);
                        }
                    }

                    @Override
                    public void onFailure(Call<AuthUser> call, Throwable throwable) {
                        setLoading(false);
                        showMessage(R.string.profile_update_error);
                    }
                });
    }

    private boolean validateForm() {
        String username = valueOf(usernameInput);
        if (TextUtils.isEmpty(username)) {
            return showFieldError(usernameInput, R.string.profile_required_field);
        }
        if (username.length() > 150 || !USERNAME_PATTERN.matcher(username).matches()) {
            return showFieldError(usernameInput, R.string.profile_username_invalid);
        }

        String email = valueOf(emailInput);
        if (TextUtils.isEmpty(email)) {
            return showFieldError(emailInput, R.string.profile_required_field);
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return showFieldError(emailInput, R.string.profile_email_invalid);
        }

        String dni = valueOf(dniInput);
        if (TextUtils.isEmpty(dni)) {
            return showFieldError(dniInput, R.string.profile_required_field);
        }
        if (!dni.matches("[0-9]{7,8}")) {
            return showFieldError(dniInput, R.string.profile_dni_invalid);
        }

        String name = valueOf(nameInput);
        if (TextUtils.isEmpty(name)) {
            return showFieldError(nameInput, R.string.profile_required_field);
        }
        if (name.length() > 25 || !NAME_PATTERN.matcher(name).matches()) {
            return showFieldError(nameInput, R.string.profile_name_invalid);
        }

        String lastName = valueOf(lastNameInput);
        if (TextUtils.isEmpty(lastName)) {
            return showFieldError(lastNameInput, R.string.profile_required_field);
        }
        if (lastName.length() > 25 || !NAME_PATTERN.matcher(lastName).matches()) {
            return showFieldError(lastNameInput, R.string.profile_name_invalid);
        }

        if (!isValidIsoDate(birthDate)) {
            return showFieldError(birthDateInput, R.string.profile_birth_date_invalid);
        }
        return true;
    }

    private boolean showFieldError(EditText field, int messageResId) {
        field.setError(getString(messageResId));
        field.requestFocus();
        return false;
    }

    private void handleResponseError(Response<?> response, boolean update) {
        int statusCode = response.code();
        if (statusCode == 401) {
            SessionManager.expireSession(this);
            return;
        }
        if (statusCode == 403) {
            showMessage(R.string.profile_unauthorized_error);
            return;
        }
        if (statusCode == 404) {
            showMessage(R.string.profile_not_found_error);
            return;
        }
        if (statusCode == 400 && applyServerErrors(response.errorBody())) {
            showMessage(R.string.profile_generic_validation_error);
            return;
        }
        showMessage(update ? R.string.profile_update_error : R.string.profile_load_error);
    }

    private boolean applyServerErrors(ResponseBody errorBody) {
        if (errorBody == null) {
            return false;
        }

        try {
            JSONObject errors = new JSONObject(errorBody.string());
            boolean handled = false;
            handled |= applyServerError(errors, "username", usernameInput);
            handled |= applyServerError(errors, "email", emailInput);
            handled |= applyServerError(errors, "dni", dniInput);
            handled |= applyServerError(errors, "nombre", nameInput);
            handled |= applyServerError(errors, "apellido", lastNameInput);
            handled |= applyServerError(errors, "fecha_nacimiento", birthDateInput);
            return handled;
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean applyServerError(JSONObject errors, String key, EditText field) {
        if (!errors.has(key)) {
            return false;
        }

        String detail = firstError(errors.opt(key));
        int messageResId = serverMessageFor(key, detail);
        field.setError(getString(messageResId));
        field.requestFocus();
        return true;
    }

    private int serverMessageFor(String key, String detail) {
        String normalized = detail.toLowerCase(Locale.ROOT);
        if (normalized.contains("required")
                || normalized.contains("blank")
                || normalized.contains("obligatorio")) {
            return R.string.profile_required_field;
        }
        if ("username".equals(key)) {
            return normalized.contains("unique") || normalized.contains("exist")
                    ? R.string.profile_username_taken
                    : R.string.profile_username_invalid;
        }
        if ("email".equals(key)) {
            return normalized.contains("unique") || normalized.contains("exist")
                    ? R.string.profile_email_taken
                    : R.string.profile_email_invalid;
        }
        if ("dni".equals(key)) {
            return normalized.contains("unique") || normalized.contains("exist")
                    ? R.string.profile_dni_taken
                    : R.string.profile_dni_invalid;
        }
        if ("nombre".equals(key) || "apellido".equals(key)) {
            return R.string.profile_name_invalid;
        }
        return R.string.profile_birth_date_invalid;
    }

    private String firstError(Object value) {
        if (value instanceof JSONArray) {
            JSONArray errors = (JSONArray) value;
            return errors.length() == 0 ? "" : errors.optString(0, "");
        }
        return value == null ? "" : String.valueOf(value);
    }

    private void setLoading(boolean loading) {
        loadingIndicator.setVisibility(loading ? View.VISIBLE : View.GONE);
        profileForm.setVisibility(loading ? View.INVISIBLE : View.VISIBLE);
        saveButton.setEnabled(!loading);
    }

    private void clearFieldErrors() {
        usernameInput.setError(null);
        emailInput.setError(null);
        dniInput.setError(null);
        nameInput.setError(null);
        lastNameInput.setError(null);
        birthDateInput.setError(null);
    }

    private void showMessage(int messageResId) {
        messageView.setText(messageResId);
        messageView.setVisibility(View.VISIBLE);
    }

    private void clearMessage() {
        messageView.setText("");
        messageView.setVisibility(View.GONE);
    }

    private String valueOf(EditText field) {
        return field.getText().toString().trim();
    }

    private String safeValue(String value) {
        return value == null ? "" : value;
    }

    private boolean isValidIsoDate(String value) {
        return parseDate(value, API_DATE_FORMAT) != null;
    }

    private void syncBirthDateFromInput() {
        String displayDate = valueOf(birthDateInput);
        if (TextUtils.isEmpty(displayDate)) {
            birthDate = null;
            return;
        }

        Date parsedDate = parseDate(displayDate, DISPLAY_DATE_FORMAT);
        birthDate = parsedDate == null
                ? null
                : new SimpleDateFormat(API_DATE_FORMAT, Locale.US).format(parsedDate);
    }

    private boolean isEnrollmentAgeAllowed(String value) {
        Date birth = parseDate(value, API_DATE_FORMAT);
        if (birth == null) {
            return true;
        }

        Calendar birthCalendar = Calendar.getInstance();
        birthCalendar.setTime(birth);
        Calendar today = Calendar.getInstance();
        int age = today.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR);
        if (today.get(Calendar.MONTH) < birthCalendar.get(Calendar.MONTH)
                || (today.get(Calendar.MONTH) == birthCalendar.get(Calendar.MONTH)
                && today.get(Calendar.DAY_OF_MONTH) < birthCalendar.get(Calendar.DAY_OF_MONTH))) {
            age--;
        }
        return age >= 18 && age < 65;
    }

    private Date parseDate(String value, String pattern) {
        if (TextUtils.isEmpty(value)) {
            return null;
        }
        SimpleDateFormat formatter = new SimpleDateFormat(pattern, Locale.US);
        formatter.setLenient(false);
        try {
            return formatter.parse(value);
        } catch (ParseException ignored) {
            return null;
        }
    }

    private String formatDate(String value, String inputPattern, String outputPattern) {
        Date date = parseDate(value, inputPattern);
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat(outputPattern, Locale.getDefault()).format(date);
    }
}
