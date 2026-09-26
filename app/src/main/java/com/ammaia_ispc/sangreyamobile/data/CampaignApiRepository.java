package com.ammaia_ispc.sangreyamobile.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.ammaia_ispc.sangreyamobile.helpers.ApiClient;
import com.ammaia_ispc.sangreyamobile.model.Campaign;
import com.ammaia_ispc.sangreyamobile.model.CampaignEnrollments;
import com.ammaia_ispc.sangreyamobile.model.Enrollment;
import com.ammaia_ispc.sangreyamobile.model.EnrollmentUser;
import com.ammaia_ispc.sangreyamobile.model.HealthCenter;
import com.ammaia_ispc.sangreyamobile.model.MyEnrollments;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public final class CampaignApiRepository {
    private static final String BASE_URL =
            "https://sangreyaispc.pythonanywhere.com/";
    private static final String CAMPAIGNS_PATH = "campanias/";

    private CampaignApiRepository() {
    }

    private static Handler mainHandler() {
        return new Handler(Looper.getMainLooper());
    }

    public interface Callback<T> {
        void onSuccess(T value);

        void onError(Exception exception);
    }

    public static final class HttpException extends IOException {
        private final int statusCode;
        private final String body;

        public HttpException(int statusCode, String body) {
            super("HTTP " + statusCode
                    + (body == null || body.isEmpty() ? "" : ": " + body));
            this.statusCode = statusCode;
            this.body = body == null ? "" : body;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public String getBody() {
            return body;
        }
    }

    public static boolean isNotFound(Exception exception) {
        return exception instanceof HttpException
                && ((HttpException) exception).getStatusCode() == 404;
    }

    public static boolean isUnauthorized(Exception exception) {
        if (!(exception instanceof HttpException)) {
            return false;
        }

        int statusCode = ((HttpException) exception).getStatusCode();
        return statusCode == 401 || statusCode == 403;
    }

    public static boolean isNetworkError(Exception exception) {
        return exception instanceof IOException && !(exception instanceof HttpException);
    }

    public static String getErrorCode(Exception exception) {
        if (!(exception instanceof HttpException)) {
            return null;
        }

        try {
            String code = new JSONObject(((HttpException) exception).getBody())
                    .optString("codigo", "");
            return code.isEmpty() ? null : code;
        } catch (Exception ignored) {
            return null;
        }
    }

    public static void getCampaigns(
            Context context,
            Callback<List<Campaign>> callback) {
        request(ApiClient.getApiService(context).getCampaigns(), response -> {
            JSONArray jsonArray = new JSONArray(response);
            List<Campaign> campaigns = new ArrayList<>();

            for (int index = 0; index < jsonArray.length(); index++) {
                campaigns.add(fromJson(jsonArray.getJSONObject(index)));
            }

            return campaigns;
        }, callback);
    }

    public static void getCampaign(
            Context context,
            int campaignId,
            Callback<Campaign> callback) {
        request(ApiClient.getApiService(context).getCampaign(campaignId), response ->
                fromJson(new JSONObject(response)), callback);
    }

    public static void enrollInCampaign(
            Context context,
            int campaignId,
            Callback<Integer> callback) {
        request(ApiClient.getApiService(context).enrollInCampaign(campaignId), response ->
                new JSONObject(response).getInt("totalInscriptos"), callback);
    }

    public static void getMyEnrollments(
            Context context,
            Callback<MyEnrollments> callback) {
        request(ApiClient.getApiService(context).getMyEnrollments(), response -> {
            JSONObject json = new JSONObject(response);
            return new MyEnrollments(
                    parseEnrollments(json.optJSONArray("actuales")),
                    parseEnrollments(json.optJSONArray("historicas")));
        }, callback);
    }

    public static void getCampaignEnrollments(
            Context context,
            int campaignId,
            Callback<CampaignEnrollments> callback) {
        request(ApiClient.getApiService(context).getCampaignEnrollments(campaignId), response -> {
            JSONObject json = new JSONObject(response);
            List<EnrollmentUser> users = new ArrayList<>();
            JSONArray usersJson = json.optJSONArray("usuarios");
            if (usersJson != null) {
                for (int index = 0; index < usersJson.length(); index++) {
                    JSONObject user = usersJson.optJSONObject(index);
                    if (user != null) {
                        users.add(new EnrollmentUser(
                                user.optInt("id"),
                                user.optString("nombre", ""),
                                user.optString("apellido", ""),
                                user.optString("dni", ""),
                                user.optString("email", "")));
                    }
                }
            }

            return new CampaignEnrollments(
                    fromJson(json.optJSONObject("campania")),
                    json.optInt("total_inscriptos", users.size()),
                    users);
        }, callback);
    }

    public static void cancelEnrollment(
            Context context,
            int enrollmentId,
            Callback<Void> callback) {
        requestVoid(ApiClient.getApiService(context).cancelEnrollment(enrollmentId), callback);
    }

    public static void createCampaign(
            Context context,
            String title,
            String description,
            String location,
            Integer healthCenterId,
            String startDate,
            String endDate,
            Integer maximumCapacity,
            Callback<Campaign> callback) {
        Map<String, Object> body = new HashMap<>();
        body.put("titulo", title);
        body.put("descripcion", description);
        body.put("ubicacion", location);
        body.put("centro_salud", healthCenterId);
        body.put("fecha_inicio", startDate);
        body.put("fecha_fin", endDate);
        body.put("cupo_maximo", maximumCapacity);

        request(ApiClient.getApiService(context).createCampaign(body), response ->
                fromJson(new JSONObject(response)), callback);
    }


    public static void deleteCampaign(
            int campaignId,
            String accessToken,
            Callback<Void> callback) {
        new Thread(() -> {
            try {
                executeDelete(
                        CAMPAIGNS_PATH + campaignId + "/",
                        accessToken);

                mainHandler().post(() -> callback.onSuccess(null));

            } catch (Exception exception) {
                Log.e(
                        "CampaignApiRepository",
                        "Error al eliminar la campaña " + campaignId,
                        exception);

                mainHandler().post(() -> callback.onError(exception));
            }
        }).start();
    }

    private interface Parser<T> {
        T parse(String response) throws Exception;
    }

    private static <T> void request(
            Call<ResponseBody> call,
            Parser<T> parser,
            Callback<T> callback) {
        call.enqueue(new retrofit2.Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
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
                    callback.onSuccess(parser.parse(body.string()));
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

    private static void requestVoid(Call<Void> call, Callback<Void> callback) {
        call.enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError(httpException(response));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable throwable) {
                callback.onError(asException(throwable));
            }
        });
    }

    private static void executeDelete(String path, String accessToken) throws Exception {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(BASE_URL + path);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("DELETE");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("Accept", "application/json");
            if (accessToken != null && !accessToken.trim().isEmpty()) {
                connection.setRequestProperty("Authorization", "Bearer " + accessToken);
            }

            int statusCode = connection.getResponseCode();
            InputStream stream = statusCode >= 200 && statusCode < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream();
            String body = readBody(stream);

            if (statusCode < 200 || statusCode >= 300) {
                throw new HttpException(statusCode, body);
            }
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

    private static HttpException httpException(Response<?> response) {
        String body = "";
        ResponseBody errorBody = response.errorBody();
        if (errorBody != null) {
            try {
                body = errorBody.string();
            } catch (IOException ignored) {
                // Keep the status code when the error body cannot be read.
            }
        }
        return new HttpException(response.code(), body);
    }

    private static Exception asException(Throwable throwable) {
        return throwable instanceof Exception
                ? (Exception) throwable
                : new IOException(throwable);
    }

    static Campaign fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }

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

    private static List<Enrollment> parseEnrollments(JSONArray jsonArray) {
        List<Enrollment> enrollments = new ArrayList<>();
        if (jsonArray == null) {
            return enrollments;
        }

        for (int index = 0; index < jsonArray.length(); index++) {
            JSONObject item = jsonArray.optJSONObject(index);
            if (item != null) {
                enrollments.add(new Enrollment(
                        item.optInt("id"),
                        fromJson(item.optJSONObject("campania"))));
            }
        }
        return enrollments;
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
