package com.ammaia_ispc.sangreyamobile.helpers;

import com.google.gson.JsonParser;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TokenAuthenticatorTest {
    private MockWebServer server;
    private OkHttpClient plainClient;
    private OkHttpClient client;
    private MemorySession session;
    private List<IOException> networkFailures;

    @Before
    public void setUp() throws IOException {
        server = new MockWebServer();
        server.start(InetAddress.getByAddress(new byte[]{127, 0, 0, 1}), 0);
        HttpUrl localUrl = server.url("/");
        session = new MemorySession();
        networkFailures = new ArrayList<>();
        plainClient = new OkHttpClient.Builder()
                .proxy(Proxy.NO_PROXY)
                .followRedirects(false)
                .followSslRedirects(false)
                .retryOnConnectionFailure(false)
                .callTimeout(3, TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    HttpUrl url = chain.request().url();
                    if (!localUrl.host().equals(url.host()) || localUrl.port() != url.port()) {
                        throw new IOException("El test solo permite el MockWebServer local");
                    }
                    return chain.proceed(chain.request());
                })
                .build();
        ApiService refreshApi = ApiClient.createApiService(localUrl.toString(), plainClient);
        client = plainClient.newBuilder()
                .authenticator(new TokenAuthenticator(session, () -> refreshApi, networkFailures::add))
                .build();
    }

    @After
    public void tearDown() throws IOException {
        if (plainClient != null) {
            plainClient.dispatcher().cancelAll();
            plainClient.dispatcher().executorService().shutdownNow();
            plainClient.connectionPool().evictAll();
        }
        if (server != null) {
            server.shutdown();
        }
    }

    // TC-AUTH-40; RF-03 / HU-03 / CA-09 / RNF-04. Cliente real, sin Deploy QA.
    @Test
    public void renuevaTokensYReintentaUnaVezConAccessNuevo() throws Exception {
        server.enqueue(json(401, "{\"detail\":\"Token vencido.\"}"));
        server.enqueue(json(200, "{\"access\":\"access-nuevo\",\"refresh\":\"refresh-nuevo\"}"));
        server.enqueue(json(200, "{\"id\":7}"));
        server.enqueue(json(200, "{\"id\":7}"));

        try (Response response = client.newCall(protectedRequest(session.access)).execute()) {
            assertEquals(200, response.code());
        }

        assertEquals("access-nuevo", session.access);
        assertEquals("refresh-nuevo", session.refresh);
        assertEquals(1, session.updates);
        assertEquals(0, session.expirations);
        assertTrue(networkFailures.isEmpty());
        assertProtectedRequest("access-vencido");
        assertRefreshRequest();
        assertProtectedRequest("access-nuevo");

        try (Response response = client.newCall(protectedRequest(session.access)).execute()) {
            assertEquals(200, response.code());
        }
        assertProtectedRequest("access-nuevo");
        assertEquals(4, server.getRequestCount());
        assertEquals(1, session.updates);
    }

    private Request protectedRequest(String access) {
        return new Request.Builder().url(server.url("/usuarios/7/"))
                .header("Authorization", "Bearer " + access).build();
    }

    private static MockResponse json(int code, String body) {
        return new MockResponse().setResponseCode(code)
                .setHeader("Content-Type", "application/json; charset=utf-8").setBody(body);
    }

    private RecordedRequest takeRequest() throws InterruptedException {
        RecordedRequest request = server.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(request);
        return request;
    }

    private void assertProtectedRequest(String access) throws InterruptedException {
        RecordedRequest request = takeRequest();
        assertEquals("GET", request.getMethod());
        assertEquals("/usuarios/7/", request.getPath());
        assertEquals("Bearer " + access, request.getHeader("Authorization"));
    }

    private void assertRefreshRequest() throws InterruptedException {
        RecordedRequest request = takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("/api/token/refresh/", request.getPath());
        assertNull(request.getHeader("Authorization"));
        assertEquals("refresh-vigente", JsonParser.parseString(request.getBody().readUtf8())
                .getAsJsonObject().get("refresh").getAsString());
    }

    // Models the storage boundary only; encrypted preferences and Android navigation remain UI checks.
    private static final class MemorySession implements TokenAuthenticator.SessionStore {
        private String access = "access-vencido";
        private String refresh = "refresh-vigente";
        private int updates;
        private int expirations;

        @Override
        public String getAccessToken() {
            return access;
        }

        @Override
        public String getRefreshToken() {
            return refresh;
        }

        @Override
        public void updateTokens(String access, String refresh) {
            this.access = access;
            if (refresh != null && !refresh.isEmpty()) {
                this.refresh = refresh;
            }
            updates++;
        }

        @Override
        public void expireSession() {
            access = null;
            refresh = null;
            expirations++;
        }
    }
}
