package com.ammaia_ispc.sangreyamobile.support;

import android.os.Bundle;
import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;

import com.ammaia_ispc.sangreyamobile.helpers.ApiConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public final class TestFixtures implements AutoCloseable {
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final OkHttpClient HTTP = new OkHttpClient.Builder()
            .callTimeout(30, TimeUnit.SECONDS).build();
    // Only authentication is reused; each test owns different mutable resources.
    private static String adminAccess;
    private final List<Account> accounts = new ArrayList<>();
    private final List<String> campaignTitles = new ArrayList<>();
    private final String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    public final String adminEmail;
    public final String adminPassword;

    public TestFixtures() throws Exception {
        Bundle arguments = InstrumentationRegistry.getArguments();
        adminEmail = required(arguments, "adminEmail");
        adminPassword = required(arguments, "adminPassword");
        if (adminAccess == null) {
            JSONObject credentials = new JSONObject()
                    .put("email", adminEmail).put("password", adminPassword);
            JSONObject session = new JSONObject(request("POST", "api/token/", credentials, null, 200));
            if (!"Administrador".equals(session.getJSONObject("user").getString("rol"))) {
                throw new IllegalStateException("La cuenta semilla debe tener rol Administrador.");
            }
            adminAccess = session.getString("access");
        }
    }

    public Account newAccount(String caseId) throws Exception {
        JSONArray users = users();
        String dni;
        do {
            dni = String.valueOf(90000000 + new java.security.SecureRandom().nextInt(10000000));
        } while (containsDni(users, dni));
        Account account = new Account(caseId.toLowerCase() + "_" + suffix, dni);
        accounts.add(account);
        return account;
    }

    public Account seedStandardAccount(String caseId) throws Exception {
        Account account = newAccount(caseId);
        JSONObject body = new JSONObject().put("username", account.username)
                .put("email", account.email).put("password", account.password)
                .put("dni", account.dni).put("nombre", account.name)
                .put("apellido", account.lastName).put("fecha_nacimiento", account.birthDate);
        request("POST", "usuarios/registro/", body, null, 201);
        return account;
    }

    public int accountCount(Account account) throws Exception {
        JSONArray users = users();
        int count = 0;
        for (int index = 0; index < users.length(); index++) {
            if (account.email.equals(users.getJSONObject(index).optString("email"))) {
                count++;
            }
        }
        return count;
    }

    public CampaignData newCampaign(String caseId) {
        CampaignData data = new CampaignData("QA UI " + caseId + " " + suffix,
                "Campaña descartable de prueba", "Centro QA - Córdoba", 10);
        campaignTitles.add(data.title);
        return data;
    }

    public CampaignData editedCampaign(CampaignData original) {
        CampaignData data = new CampaignData(original.title + " editada",
                "Descripción actualizada desde Android", "Sede QA actualizada", 12);
        campaignTitles.add(data.title);
        return data;
    }

    public int seedCampaign(CampaignData data) throws Exception {
        JSONObject created = new JSONObject(request("POST", "campanias/", data.json(), adminAccess, 201));
        return created.getInt("id");
    }

    public JSONObject campaignNamed(String title) throws Exception {
        JSONArray campaigns = new JSONArray(request("GET", "campanias/", null, adminAccess, 200));
        JSONObject found = null;
        for (int index = 0; index < campaigns.length(); index++) {
            JSONObject campaign = campaigns.getJSONObject(index);
            if (title.equals(campaign.optString("titulo"))) {
                if (found != null) {
                    throw new IllegalStateException("Hay más de una campaña con el nombre de prueba.");
                }
                found = campaign;
            }
        }
        if (found == null) {
            throw new IllegalStateException("No se encontró la campaña de esta ejecución: " + title);
        }
        return campaign(found.getInt("id"));
    }

    public JSONObject campaign(int id) throws Exception {
        return new JSONObject(request("GET", "campanias/" + id + "/", null, adminAccess, 200));
    }

    public int campaignStatus(int id) throws IOException {
        try (Response response = HTTP.newCall(buildRequest("GET", "campanias/" + id + "/",
                null, adminAccess)).execute()) {
            return response.code();
        }
    }

    @Override
    public void close() throws Exception {
        List<Exception> failures = new ArrayList<>();
        // Query by exact names, including edited names, to recover IDs after UI failures.
        if (!campaignTitles.isEmpty()) {
            try {
                JSONArray campaigns = new JSONArray(request("GET", "campanias/", null, adminAccess, 200));
                for (int index = 0; index < campaigns.length(); index++) {
                    JSONObject campaign = campaigns.getJSONObject(index);
                    if (campaignTitles.contains(campaign.optString("titulo"))) {
                        try {
                            request("DELETE", "campanias/" + campaign.getInt("id") + "/", null,
                                    adminAccess, 204, 404);
                        } catch (Exception exception) {
                            failures.add(exception);
                        }
                    }
                }
            } catch (Exception exception) {
                failures.add(exception);
            }
        }
        if (!accounts.isEmpty()) {
            try {
                JSONArray users = users();
                for (int index = 0; index < users.length(); index++) {
                    JSONObject user = users.getJSONObject(index);
                    for (Account account : accounts) {
                        if (account.email.equals(user.optString("email"))) {
                            try {
                                request("DELETE", "usuarios/" + user.getInt("id") + "/", null,
                                        adminAccess, 204, 404);
                            } catch (Exception exception) {
                                failures.add(exception);
                            }
                        }
                    }
                }
            } catch (Exception exception) {
                failures.add(exception);
            }
        }
        if (!failures.isEmpty()) {
            Exception failure = new IOException("Falló la limpieza de los datos propios de UI: " + suffix);
            for (Exception exception : failures) {
                failure.addSuppressed(exception);
            }
            throw failure;
        }
        Log.i("CriticalFlows", "Limpieza completada: " + suffix);
    }

    private JSONArray users() throws Exception {
        return new JSONArray(request("GET", "usuarios/", null, adminAccess, 200));
    }

    private static boolean containsDni(JSONArray users, String dni) throws Exception {
        for (int index = 0; index < users.length(); index++) {
            if (dni.equals(users.getJSONObject(index).optString("dni"))) {
                return true;
            }
        }
        return false;
    }

    private static String required(Bundle arguments, String key) {
        String value = arguments.getString(key, "");
        if (value.isEmpty()) {
            throw new IllegalArgumentException("Falta el argumento instrumentado " + key
                    + ". Configurar SANGREYA_UI_ADMIN_EMAIL y SANGREYA_UI_ADMIN_PASSWORD.");
        }
        return value;
    }

    private static String date(int field, int offset) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(field, offset);
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.getTime());
    }

    private static Request buildRequest(String method, String path, JSONObject body, String token) {
        Request.Builder request = new Request.Builder().url(ApiConfig.BASE_URL + path);
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }
        return request.method(method, body == null ? null : RequestBody.create(body.toString(), JSON)).build();
    }

    private static String request(String method, String path, JSONObject body, String token,
                                  int... expectedCodes) throws IOException {
        try (Response response = HTTP.newCall(buildRequest(method, path, body, token)).execute()) {
            for (int code : expectedCodes) {
                if (response.code() == code) {
                    return response.body() == null ? "" : response.body().string();
                }
            }
            // Never include credentials, tokens or request/response bodies in diagnostics.
            String retryAfter = response.header("Retry-After");
            throw new IOException("Fixture: " + method + " " + path + " devolvió HTTP " + response.code()
                    + (retryAfter == null ? "" : "; Retry-After=" + retryAfter + "s"));
        }
    }

    public static final class Account {
        public final String username;
        public final String email;
        public final String password = "Ui9!" + UUID.randomUUID().toString().replace("-", "");
        public final String dni;
        public final String name = "Prueba";
        public final String lastName = "Instrumentada";
        public final String birthDate = date(Calendar.YEAR, -30);

        private Account(String username, String dni) {
            this.username = username;
            this.email = username + "@example.test";
            this.dni = dni;
        }
    }

    public static final class CampaignData {
        public final String title;
        public final String description;
        public final String location;
        public final int capacity;
        public final String startDate = date(Calendar.DAY_OF_MONTH, 0);
        public final String endDate = date(Calendar.DAY_OF_MONTH, 7);

        private CampaignData(String title, String description, String location, int capacity) {
            this.title = title;
            this.description = description;
            this.location = location;
            this.capacity = capacity;
        }

        public JSONObject json() throws Exception {
            return new JSONObject().put("titulo", title).put("descripcion", description)
                    .put("ubicacion", location).put("cupo_maximo", capacity)
                    .put("fecha_inicio", startDate).put("fecha_fin", endDate)
                    .put("centro_salud", JSONObject.NULL);
        }
    }
}
