package com.ammaia_ispc.sangreyamobile.data;

import android.content.Context;

import com.ammaia_ispc.sangreyamobile.helpers.ApiClient;
import com.ammaia_ispc.sangreyamobile.model.AdminDashboardData;
import com.ammaia_ispc.sangreyamobile.model.Campaign;
import com.ammaia_ispc.sangreyamobile.model.DashboardCampaignStatus;
import com.ammaia_ispc.sangreyamobile.model.DashboardMonthlyDonors;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public final class DashboardApiRepository {
    private DashboardApiRepository() {
    }

    public static void getDashboard(
            Context context,
            CampaignApiRepository.Callback<AdminDashboardData> callback) {
        ApiClient.getApiService(context).getDashboard().enqueue(
                new retrofit2.Callback<ResponseBody>() {
                    @Override
                    public void onResponse(
                            Call<ResponseBody> call,
                            Response<ResponseBody> response) {
                        if (!response.isSuccessful()) {
                            callback.onError(httpException(response));
                            return;
                        }

                        ResponseBody body = response.body();
                        if (body == null) {
                            callback.onError(new IOException("Respuesta vacía del servidor"));
                            return;
                        }

                        try {
                            callback.onSuccess(fromJson(new JSONObject(body.string())));
                        } catch (Exception exception) {
                            callback.onError(exception);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                        callback.onError(asException(throwable));
                    }
                });
    }

    private static CampaignApiRepository.HttpException httpException(
            Response<?> response) {
        String body = "";
        ResponseBody errorBody = response.errorBody();
        if (errorBody != null) {
            try {
                body = errorBody.string();
            } catch (IOException ignored) {
                // Keep the status code when the error body cannot be read.
            }
        }
        return new CampaignApiRepository.HttpException(response.code(), body);
    }

    private static Exception asException(Throwable throwable) {
        return throwable instanceof Exception
                ? (Exception) throwable
                : new IOException(throwable);
    }

    private static AdminDashboardData fromJson(JSONObject json) {
        return new AdminDashboardData(
                json.optInt("total_campanias"),
                json.optInt("total_inscripciones"),
                json.optInt("total_donantes"),
                parseMonthlySeries(json.optJSONArray("donantes_por_mes")),
                parseMonthlySeries(json.optJSONArray("inscripciones_por_mes")),
                parseCampaignStatuses(json.optJSONArray("campanias_por_estado")),
                parseCampaigns(json.optJSONArray("campanias_recientes")));
    }

    private static List<DashboardMonthlyDonors> parseMonthlySeries(JSONArray jsonArray) {
        List<DashboardMonthlyDonors> series = new ArrayList<>();
        if (jsonArray == null) {
            return series;
        }

        for (int index = 0; index < jsonArray.length(); index++) {
            JSONObject item = jsonArray.optJSONObject(index);
            if (item != null) {
                series.add(new DashboardMonthlyDonors(
                        item.optInt("anio"),
                        item.optInt("mes"),
                        item.optInt("cantidad")));
            }
        }
        return series;
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
