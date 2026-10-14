package com.ammaia_ispc.sangreyamobile.data;

import android.content.Context;

import com.ammaia_ispc.sangreyamobile.helpers.ApiClient;
import com.ammaia_ispc.sangreyamobile.helpers.ApiService;
import com.ammaia_ispc.sangreyamobile.model.Campaign;
import com.ammaia_ispc.sangreyamobile.model.CampaignEnrollments;
import com.ammaia_ispc.sangreyamobile.model.Enrollment;
import com.ammaia_ispc.sangreyamobile.model.EnrollmentUser;
import com.ammaia_ispc.sangreyamobile.model.HealthCenter;
import com.ammaia_ispc.sangreyamobile.model.MyEnrollments;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public final class CampaignApiRepository {
    private static final Gson CAMPAIGN_GSON = new GsonBuilder().serializeNulls().create();

    private CampaignApiRepository() {
    }

    public interface Callback<T> {
        void onSuccess(T value);

        void onError(Exception exception);
    }

    public static final class HttpException extends IOException {
        private final int statusCode;
        private final String body;

        public HttpException(int statusCode, String body) {
            super("HTTP " + statusCode);
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
        getCampaigns(ApiClient.getApiService(context), callback);
    }

    static void getCampaigns(ApiService api, Callback<List<Campaign>> callback) {
        request(api.getCampaigns(), response -> {
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
        enrollInCampaign(ApiClient.getApiService(context), campaignId, callback);
    }

    static void enrollInCampaign(ApiService api, int campaignId, Callback<Integer> callback) {
        request(api.enrollInCampaign(campaignId), response -> JsonParser.parseString(response)
                .getAsJsonObject().get("totalInscriptos").getAsInt(), callback);
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
        createCampaign(ApiClient.getApiService(context), title, description, location,
                healthCenterId, startDate, endDate, maximumCapacity, callback);
    }

    static void createCampaign(
            ApiService api,
            String title,
            String description,
            String location,
            Integer healthCenterId,
            String startDate,
            String endDate,
            Integer maximumCapacity,
            Callback<Campaign> callback) {
        RequestBody body = campaignRequestBody(title, description, location,
                healthCenterId, startDate, endDate, maximumCapacity);
        request(api.createCampaign(body), response ->
                fromJson(new JSONObject(response)), callback);
    }

    public static void updateCampaign(
            Context context,
            int campaignId,
            String title,
            String description,
            String location,
            Integer healthCenterId,
            String startDate,
            String endDate,
            Integer maximumCapacity,
            Callback<Campaign> callback) {
        updateCampaign(ApiClient.getApiService(context), campaignId, title, description, location,
                healthCenterId, startDate, endDate, maximumCapacity, callback);
    }

    static void updateCampaign(
            ApiService api,
            int campaignId,
            String title,
            String description,
            String location,
            Integer healthCenterId,
            String startDate,
            String endDate,
            Integer maximumCapacity,
            Callback<Campaign> callback) {
        RequestBody body = campaignRequestBody(title, description, location,
                healthCenterId, startDate, endDate, maximumCapacity);
        request(api.updateCampaign(campaignId, body), response ->
                fromJson(new JSONObject(response)), callback);
    }

    private static RequestBody campaignRequestBody(
            String title,
            String description,
            String location,
            Integer healthCenterId,
            String startDate,
            String endDate,
            Integer maximumCapacity) {
        Map<String, Object> body = new HashMap<>();
        body.put("titulo", title);
        body.put("descripcion", description);
        body.put("ubicacion", location);
        body.put("centro_salud", healthCenterId);
        body.put("fecha_inicio", startDate);
        body.put("fecha_fin", endDate);
        body.put("cupo_maximo", maximumCapacity);

        // Null must be sent explicitly so editing can clear an existing capacity.
        return RequestBody.create(CAMPAIGN_GSON.toJson(body),
                MediaType.get("application/json; charset=utf-8"));
    }


    public static void deleteCampaign(
            Context context,
            int campaignId,
            Callback<Void> callback) {
        requestVoid(ApiClient.getApiService(context).deleteCampaign(campaignId), callback);
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
