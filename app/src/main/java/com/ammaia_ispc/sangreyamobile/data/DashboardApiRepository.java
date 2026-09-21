package com.ammaia_ispc.sangreyamobile.data;

import android.os.Handler;
import android.os.Looper;

import com.ammaia_ispc.sangreyamobile.model.AdminDashboardData;
import com.ammaia_ispc.sangreyamobile.model.Campaign;
import com.ammaia_ispc.sangreyamobile.model.DashboardCampaignStatus;
import com.ammaia_ispc.sangreyamobile.model.DashboardMonthlyDonors;

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

public final class DashboardApiRepository {
    private static final String DASHBOARD_URL =
            "https://sangreyaispc.pythonanywhere.com/dashboard/";
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    private DashboardApiRepository() {
    }

    public static void getDashboard(
            String accessToken,
            CampaignApiRepository.Callback<AdminDashboardData> callback) {
        new Thread(() -> {
            try {
                String response = executeGet(accessToken);
                AdminDashboardData dashboard = fromJson(new JSONObject(response));
                MAIN_HANDLER.post(() -> callback.onSuccess(dashboard));
            } catch (Exception exception) {
                MAIN_HANDLER.post(() -> callback.onError(exception));
            }
        }).start();
    }

    private static String executeGet(String accessToken) throws Exception {
        HttpURLConnection connection = null;

        try {
            connection = (HttpURLConnection) new URL(DASHBOARD_URL).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);

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

    private static AdminDashboardData fromJson(JSONObject json) {
        return new AdminDashboardData(
                json.optInt("total_campanias"),
                json.optInt("total_inscripciones"),
                json.optInt("total_donantes"),
                parseMonthlyDonors(json.optJSONArray("donantes_por_mes")),
                parseCampaignStatuses(json.optJSONArray("campanias_por_estado")),
                parseCampaigns(json.optJSONArray("campanias_recientes")));
    }

    private static List<DashboardMonthlyDonors> parseMonthlyDonors(JSONArray jsonArray) {
        List<DashboardMonthlyDonors> donors = new ArrayList<>();
        if (jsonArray == null) {
            return donors;
        }

        for (int index = 0; index < jsonArray.length(); index++) {
            JSONObject item = jsonArray.optJSONObject(index);
            if (item != null) {
                donors.add(new DashboardMonthlyDonors(
                        item.optInt("anio"),
                        item.optInt("mes"),
                        item.optInt("cantidad")));
            }
        }
        return donors;
    }

    private static List<DashboardCampaignStatus> parseCampaignStatuses(JSONArray jsonArray) {
        List<DashboardCampaignStatus> statuses = new ArrayList<>();
        if (jsonArray == null) {
            return statuses;
        }

        for (int index = 0; index < jsonArray.length(); index++) {
            JSONObject item = jsonArray.optJSONObject(index);
            if (item != null) {
                statuses.add(new DashboardCampaignStatus(
                        normalizeStatus(item.optString("estado", "")),
                        item.optInt("cantidad")));
            }
        }
        return statuses;
    }

    private static List<Campaign> parseCampaigns(JSONArray jsonArray) {
        List<Campaign> campaigns = new ArrayList<>();
        if (jsonArray == null) {
            return campaigns;
        }

        for (int index = 0; index < jsonArray.length(); index++) {
            JSONObject item = jsonArray.optJSONObject(index);
            if (item != null) {
                campaigns.add(CampaignApiRepository.fromJson(item));
            }
        }
        return campaigns;
    }

    private static String normalizeStatus(String status) {
        if ("Próximamente".equalsIgnoreCase(status)
                || "Proximamente".equalsIgnoreCase(status)) {
            return "Proximamente";
        }
        if ("En curso".equalsIgnoreCase(status)) {
            return "Activa";
        }
        return status;
    }
}
