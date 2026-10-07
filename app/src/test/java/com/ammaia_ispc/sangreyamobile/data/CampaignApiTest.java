package com.ammaia_ispc.sangreyamobile.data;

import com.ammaia_ispc.sangreyamobile.helpers.ApiClient;
import com.ammaia_ispc.sangreyamobile.helpers.ApiService;
import com.ammaia_ispc.sangreyamobile.model.Campaign;
import com.google.gson.JsonParser;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Proxy;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class CampaignApiTest {
    private MockWebServer server;
    private OkHttpClient client;
    private ApiService api;

    @Before
    public void setUp() throws IOException {
        server = new MockWebServer();
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                if ("GET".equals(request.getMethod()) && "/campanias/".equals(request.getPath())) {
                    return new MockResponse().setResponseCode(500)
                            .setHeader("Content-Type", "application/json; charset=utf-8")
                            .setBody("{\"codigo\":\"error_sintetico\",\"detalle\":\"stack trace de prueba\"}");
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
                    return chain.proceed(chain.request());
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

    // TC-NET-04: Java no admite un nombre de método que comience con 500.
    @Test
    @SuppressWarnings("unchecked")
    public void http500MapeaErrorSeguro() throws Exception {
        // Arrange
        CampaignApiRepository.Callback<List<Campaign>> callback =
                mock(CampaignApiRepository.Callback.class);
        ArgumentCaptor<Exception> failure = ArgumentCaptor.forClass(Exception.class);

        // Act
        CampaignApiRepository.getCampaigns(api, callback);

        // Assert
        verify(callback, timeout(5000)).onError(failure.capture());
        verifyNoMoreInteractions(callback);
        CampaignApiRepository.HttpException error =
                (CampaignApiRepository.HttpException) failure.getValue();
        assertEquals(500, error.getStatusCode());
        assertEquals("HTTP 500", error.getMessage());
        assertEquals("error_sintetico", JsonParser.parseString(error.getBody())
                .getAsJsonObject().get("codigo").getAsString());
        RecordedRequest request = server.takeRequest(1, TimeUnit.SECONDS);
        assertEquals("GET", request.getMethod());
        assertEquals("/campanias/", request.getPath());
        assertEquals(1, server.getRequestCount());
    }
}

