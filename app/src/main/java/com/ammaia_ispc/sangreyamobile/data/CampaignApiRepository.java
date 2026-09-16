package com.ammaia_ispc.sangreyamobile.data;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.ammaia_ispc.sangreyamobile.model.Campaign;
import com.ammaia_ispc.sangreyamobile.model.HealthCenter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class CampaignApiRepository {
    private static final String BASE_URL =
            "https://sangreyaispc.pythonanywhere.com/";
    private static final String CAMPAIGNS_PATH = "campanias/";
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    private CampaignApiRepository() {
    }

    public interface Callback<T> {
        void onSuccess(T value);

        void onError(Exception exception);
    }

    public static void getCampaigns(Callback<List<Campaign>> callback) {
        request(CAMPAIGNS_PATH, response -> {
            JSONArray jsonArray = new JSONArray(response);
            List<Campaign> campaigns = new ArrayList<>();

            for (int index = 0; index < jsonArray.length(); index++) {
                campaigns.add(fromJson(jsonArray.getJSONObject(index)));
            }

            return campaigns;
        }, callback);
    }

    public static void getCampaign(int campaignId, Callback<Campaign> callback) {
        request(CAMPAIGNS_PATH + campaignId + "/", response ->
                fromJson(new JSONObject(response)), callback);
    }

    private interface Parser<T> {
        T parse(String response) throws Exception;
    }

    private static <T> void request(
            String path,
            Parser<T> parser,
            Callback<T> callback) {
        new Thread(() -> {
            try {
                String response = executeGet(path);
                T value = parser.parse(response);
                MAIN_HANDLER.post(() -> callback.onSuccess(value));
            } catch (Exception exception) {
                Log.e(
                        "CampaignApiRepository",
                        "Error al consultar https://sangreyaispc.pythonanywhere.com/" + path,
                        exception);
                MAIN_HANDLER.post(() -> callback.onError(exception));
            }
        }).start();
    }

    private static String executeGet(String path) throws Exception {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(BASE_URL + path);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("Accept", "application/json");

            int statusCode = connection.getResponseCode();
            InputStream stream = statusCode >= 200 && statusCode < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream();
            String body = readBody(stream);

            if (statusCode < 200 || statusCode >= 300) {
                throw new IllegalStateException(
                        "HTTP " + statusCode + (body.isEmpty() ? "" : ": " + body));
            }

            return body;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static String readBody(InputStream stream) throws Exception {
        if (stream == null) {
            return "";
        }

        StringBuilder body = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }
        }
        return body.toString();
    }

    static Campaign fromJson(JSONObject json) {
        String campaignStatus = normalizeStatus(
                json.optString("estado_campania", ""));
        String calculatedStatus = normalizeStatus(
                json.optString("estado_calculado", campaignStatus));

        JSONObject healthCenterJson = json.optJSONObject("centro_salud_detalle");
        HealthCenter healthCenter = fromHealthCenter(healthCenterJson);
        Integer healthCenterId = null;
        if (healthCenter != null) {
            healthCenterId = healthCenter.id;
        } else {
            healthCenterId = nullableInteger(json, "centro_salud");
        }

        Integer maximumCapacity = null;
        if (json.has("cupo_maximo") && !json.isNull("cupo_maximo")) {
            maximumCapacity = Integer.valueOf(json.optInt("cupo_maximo"));
        }

        int totalRegistered = json.optInt("total_inscriptos", 0);

        return new Campaign(
                json.optInt("id"),
                json.optString("titulo", ""),
                json.optString("descripcion", ""),
                json.optString("ubicacion", ""),
                healthCenterId,
                healthCenter,
                json.optString("fecha_inicio", ""),
                json.optString("fecha_fin", ""),
                maximumCapacity,
                totalRegistered,
                campaignStatus,
                calculatedStatus);
    }

    private static HealthCenter fromHealthCenter(JSONObject json) {
        if (json == null) {
            return null;
        }

        return new HealthCenter(
                json.optInt("id"),
                json.optString("nombre", ""),
                json.optString("direccion", ""),
                json.optString("barrio", ""),
                json.optString("ciudad", json.optString("localidad", "")),
                json.optString("telefono", ""),
                json.optString("sitio_web", ""),
                json.optString("latitud", ""),
                json.optString("longitud", ""));
    }

    private static Integer nullableInteger(JSONObject json, String key) {
        if (!json.has(key) || json.isNull(key)) {
            return null;
        }

        return json.optInt(key);
    }

    private static String normalizeStatus(String status) {
        if (status == null) {
            return "";
        }

        if (status.equalsIgnoreCase("Próximamente")
                || status.equalsIgnoreCase("Proximamente")) {
            return "Proximamente";
        }

        if (status.equalsIgnoreCase("En curso")
                || status.equalsIgnoreCase("Activa")) {
            return "Activa";
        }

        if (status.equalsIgnoreCase("Finalizada")) {
            return "Finalizada";
        }

        return status;
    }
}
