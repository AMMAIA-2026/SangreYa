package com.ammaia_ispc.sangreyamobile.data;

import com.ammaia_ispc.sangreyamobile.helpers.ApiClient;
import com.ammaia_ispc.sangreyamobile.helpers.ApiService;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class EnrollmentApiTest {
    private MockWebServer server;
    private OkHttpClient client;
    private ApiService api;
    private volatile String responseContentType;

    @Before
    public void setUp() throws IOException {
        server = new MockWebServer();
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                if ("POST".equals(request.getMethod())) {
                    if ("/inscripciones/campanias/7/".equals(request.getPath())) {
                        return new MockResponse().setResponseCode(201)
                                .setHeader("Content-Type", "application/json; charset=utf-8")
                                .setBody("{\"totalInscriptos\":3}");
                    }
                    if ("/inscripciones/campanias/8/".equals(request.getPath())) {
                        return new MockResponse().setResponseCode(200)
                                .setHeader("Content-Type", "application/json; charset=utf-8")
                                .setBody("");
                    }
                }
                return new MockResponse().setResponseCode(404);
            }
        });
        server.start(InetAddress.getByAddress(new byte[]{127, 0, 0, 1}), 0);
        HttpUrl localUrl = server.url("/").newBuilder().host("127.0.0.1").build();
        client = new OkHttpClient.Builder()
                .proxy(Proxy.NO_PROXY)
                .followRedirects(false)
                .followSslRedirects(false)
                .retryOnConnectionFailure(false)
                .callTimeout(3, TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    HttpUrl url = chain.request().url();
                    if (!"127.0.0.1".equals(url.host()) || url.port() != localUrl.port()) {
                        throw new IOException("El test solo permite el MockWebServer local");
                    }
                    okhttp3.Response response = chain.proceed(chain.request());
                    responseContentType = response.header("Content-Type");
                    return response;
                })
                .build();
        api = ApiClient.createApiService(localUrl.toString(), client);
    }

    @After
    public void tearDown() throws IOException {
        if (client != null) {
            client.dispatcher().cancelAll();
            client.dispatcher().executorService().shutdownNow();
            client.connectionPool().evictAll();
        }
        if (server != null) {
            server.shutdown();
        }
    }

    // TC-NET-05
    @Test
    @SuppressWarnings("unchecked")
    public void bodyVacioYContentType() throws Exception {
        // Arrange
        CampaignApiRepository.Callback<Integer> callback = mock(CampaignApiRepository.Callback.class);

        // Act: inscripción con POST sin cuerpo y respuesta JSON.
        CampaignApiRepository.enrollInCampaign(api, 7, callback);

        // Assert
        verify(callback, timeout(5000)).onSuccess(3);
        assertEquals("application/json; charset=utf-8", responseContentType);
        RecordedRequest request = server.takeRequest(1, TimeUnit.SECONDS);
        assertEquals("POST", request.getMethod());
        assertEquals("/inscripciones/campanias/7/", request.getPath());
        assertEquals(0L, request.getBodySize());

        // Act: una respuesta vacía debe llegar al callback de error sin crash.
        CampaignApiRepository.enrollInCampaign(api, 8, callback);

        // Assert
        verify(callback, timeout(5000)).onError(any(Exception.class));
        verifyNoMoreInteractions(callback);
        assertEquals(2, server.getRequestCount());
    }
}
