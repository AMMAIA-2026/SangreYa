package com.ammaia_ispc.sangreyamobile.data;

import android.content.Context;

import androidx.annotation.StringRes;

import com.ammaia_ispc.sangreyamobile.R;
import com.ammaia_ispc.sangreyamobile.helpers.ApiClient;
import com.ammaia_ispc.sangreyamobile.helpers.ApiService;
import com.ammaia_ispc.sangreyamobile.model.HealthCenter;
import com.ammaia_ispc.sangreyamobile.model.HealthCenterResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public final class HealthCenterApiRepository {

    public interface HealthCentersCallback {
        void onSuccess(List<HealthCenter> centers);

        void onError(@StringRes int messageRes);
    }

    private HealthCenterApiRepository() {
    }

    public static void loadHealthCenters(Context context, HealthCentersCallback callback) {
        ApiService api = ApiClient.getApiService(context);

        api.getHealthCenters().enqueue(new retrofit2.Callback<List<HealthCenterResponse>>() {
            @Override
            public void onResponse(Call<List<HealthCenterResponse>> call,
                                   Response<List<HealthCenterResponse>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onError(R.string.health_center_load_error_server);
                    return;
                }
                callback.onSuccess(toSortedCenters(response.body()));
            }

            @Override
            public void onFailure(Call<List<HealthCenterResponse>> call, Throwable throwable) {
                callback.onError(R.string.health_center_load_error_network);
            }
        });
    }

    private static List<HealthCenter> toSortedCenters(List<HealthCenterResponse> responses) {
        List<HealthCenter> centers = new ArrayList<>();
        for (HealthCenterResponse item : responses) {
            centers.add(new HealthCenter(
                    item.id,
                    item.name,
                    item.address,
                    item.neighborhood,
                    item.city,
                    item.phone,
                    item.website,
                    item.latitude,
                    item.longitude));
        }
        Collections.sort(centers, Comparator
                .comparing((HealthCenter center) -> center.neighborhood, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(center -> center.name, String.CASE_INSENSITIVE_ORDER));
        return centers;
    }
}