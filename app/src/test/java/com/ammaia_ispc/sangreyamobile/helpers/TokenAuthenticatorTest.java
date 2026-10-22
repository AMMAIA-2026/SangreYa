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
import static org.junit.Assert.assertSame;
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

    // TC-AUTH-40: un segundo 401 corta el ciclo, sin otro refresh ni tercera petición protegida.
    @Test
    public void segundo401DetieneReintentos() throws Exception {
        server.enqueue(json(401, "{}"));
        server.enqueue(json(200, "{\"access\":\"access-nuevo\",\"refresh\":\"refresh-nuevo\"}"));
        server.enqueue(json(401, "{\"detail\":\"Token rechazado.\"}"));

        try (Response response = client.newCall(protectedRequest(session.access)).execute()) {
            assertEquals(401, response.code());
        }

        assertProtectedRequest("access-vencido");
        assertRefreshRequest();
        assertProtectedRequest("access-nuevo");
        assertEquals(3, server.getRequestCount());
        assertEquals(1, session.updates);
        assertEquals(0, session.expirations);
        assertTrue(networkFailures.isEmpty());
    }

    // TC-AUTH-41; RF-03 / HU-03 / CA-10 / RNF-04. Verifica la orden de expiración.
    @Test
    public void refresh401SolicitaExpiracionSinReintentarRecurso() throws Exception {
        assertRefreshRejected(401);
    }

    // TC-AUTH-41: 400 del endpoint de refresh, no 400 de validación del recurso protegido.
    @Test
    public void refresh400SolicitaExpiracionSinReintentarRecurso() throws Exception {
        assertRefreshRejected(400);
    }

    // TC-AUTH-40/41: un fallo de transporte no equivale a un rechazo del refresh.
    @Test
    public void errorDeRedEnRefreshConservaSesion() throws Exception {
        server.enqueue(json(401, "{}"));
        IOException failure = new IOException("Fallo de red controlado");
        OkHttpClient failingClient = plainClient.newBuilder()
                .addInterceptor(chain -> { throw failure; }).build();
        ApiService failingApi = ApiClient.createApiService(server.url("/").toString(), failingClient);
        client = client.newBuilder().authenticator(new TokenAuthenticator(
                session, () -> failingApi, networkFailures::add)).build();

        try (Response response = client.newCall(protectedRequest(session.access)).execute()) {
            assertEquals(401, response.code());
        }

        assertProtectedRequest("access-vencido");
        assertEquals(1, server.getRequestCount());
        assertEquals(1, networkFailures.size());
        assertSame(failure, networkFailures.get(0));
        assertEquals("access-vencido", session.access);
        assertEquals("refresh-vigente", session.refresh);
        assertEquals(0, session.expirations);
        assertEquals(0, session.updates);
    }

    // TC-AUTH-40: sin refresh no se envía una renovación vacía ni se repite la petición.
    @Test
    public void sinRefreshDevuelve401SinRenovar() throws Exception {
        session.refresh = null;
        server.enqueue(json(401, "{}"));

        try (Response response = client.newCall(protectedRequest(session.access)).execute()) {
            assertEquals(401, response.code());
        }

        assertProtectedRequest("access-vencido");
        assertEquals(1, server.getRequestCount());
        assertEquals(0, session.updates);
        assertEquals(0, session.expirations);
        assertTrue(networkFailures.isEmpty());
    }

    private void assertRefreshRejected(int status) throws Exception {
        server.enqueue(json(401, "{}"));
        server.enqueue(json(status, "{\"detail\":\"Refresh inválido.\"}"));

        try (Response response = client.newCall(protectedRequest(session.access)).execute()) {
            assertEquals(401, response.code());
        }

        assertProtectedRequest("access-vencido");
        assertRefreshRequest();
        assertEquals(2, server.getRequestCount());
        assertEquals(1, session.expirations);
        assertEquals(0, session.updates);
        assertNull(session.access);
        assertNull(session.refresh);
        assertTrue(networkFailures.isEmpty());
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
