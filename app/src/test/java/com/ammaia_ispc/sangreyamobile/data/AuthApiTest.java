package com.ammaia_ispc.sangreyamobile.data;

import com.ammaia_ispc.sangreyamobile.helpers.ApiClient;
import com.ammaia_ispc.sangreyamobile.helpers.ApiService;
import com.ammaia_ispc.sangreyamobile.model.LoginRequest;
import com.ammaia_ispc.sangreyamobile.model.LoginResponse;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okio.BufferedSource;
import okio.Okio;
import retrofit2.Response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class AuthApiTest {

    private static final String EMAIL = "donante@example.test";
    private static final String PASSWORD = "ClaveSoloDePrueba-123";
    private static final String ACCESS = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9."
            + "eyJ1c2VyX2lkIjo3LCJ0b2tlbl90eXBlIjoiYWNjZXNzIn0.Zml4dHVyZS1hY2Nlc3M";
    private static final String REFRESH = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9."
            + "eyJ1c2VyX2lkIjo3LCJ0b2tlbl90eXBlIjoicmVmcmVzaCJ9.Zml4dHVyZS1yZWZyZXNo";

    private MockWebServer server;
    private OkHttpClient client;
    private ApiService api;

    @Before
    public void setUp() throws IOException {
        server = new MockWebServer();
        server.setDispatcher(loginDispatcher(200, readFixture("login_200.json")));
        InetAddress loopback = InetAddress.getByAddress(new byte[]{127, 0, 0, 1});
        server.start(loopback, 0);
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

    // TC-NET-01: Retrofit y Gson reales contra HTTP local; sin backend externo.
    @Test
    public void login200ParseaTokens() throws IOException {
        Response<LoginResponse> response = api.login(new LoginRequest(EMAIL, PASSWORD)).execute();

        assertEquals(200, response.code());
        assertEquals("application/json; charset=utf-8", response.headers().get("Content-Type"));
        assertEquals(ACCESS, response.body().getAccess());
        assertEquals(REFRESH, response.body().getRefresh());
    }

    // TC-NET-02
    @Test
    public void login401NoPersisteSesion() throws Exception {
        // Arrange
        server.setDispatcher(loginDispatcher(401, readFixture("login_401.json")));
        AuthApiRepository.SessionStore sessionStore = mock(AuthApiRepository.SessionStore.class);
        AuthApiRepository.LoginCallback callback = mock(AuthApiRepository.LoginCallback.class);
        AuthApiRepository repository = new AuthApiRepository(api, sessionStore);
        ArgumentCaptor<ResponseBody> errorBody = ArgumentCaptor.forClass(ResponseBody.class);

        // Act
        repository.login(new LoginRequest(EMAIL, PASSWORD), callback);

        // Assert: espera acotada al callback HTTP; no éxito ni fallo de transporte.
        verify(callback, timeout(5000)).onHttpError(eq(401), errorBody.capture());
        verify(callback, never()).onSuccess(any(LoginResponse.class));
        verify(callback, never()).onFailure(any(Throwable.class));
        verify(sessionStore).clear();
        verify(sessionStore, never()).save(any(LoginResponse.class));
        verifyNoMoreInteractions(sessionStore);

        assertNotNull(errorBody.getValue());
        JsonObject error;
        try (ResponseBody body = errorBody.getValue()) {
            error = new JsonParser().parse(body.string()).getAsJsonObject();
        }
        assertEquals("Credenciales inválidas.",
                error.getAsJsonArray("non_field_errors").get(0).getAsString());
        assertFalse(error.has("access"));
        assertFalse(error.has("refresh"));

        RecordedRequest request = server.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(request);
        assertEquals("POST", request.getMethod());
        assertEquals("/api/token/", request.getPath());
        assertEquals("127.0.0.1:" + server.getPort(), request.getHeader("Host"));
        assertNull(request.getHeader("Authorization"));
        JsonObject payload = new JsonParser().parse(request.getBody().readUtf8()).getAsJsonObject();
        assertEquals(EMAIL, payload.get("email").getAsString());
        assertEquals(PASSWORD, payload.get("password").getAsString());
        assertEquals(1, server.getRequestCount());
    }

    private Dispatcher loginDispatcher(int statusCode, String fixture) {
        return new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                if ("POST".equals(request.getMethod())
                        && "/api/token/".equals(request.getPath())) {
                    return new MockResponse()
                            .setResponseCode(statusCode)
                            .setHeader("Content-Type", "application/json; charset=utf-8")
                            .setBody(fixture);
                }
                return new MockResponse().setResponseCode(404);
            }
        };
    }

    private String readFixture(String name) throws IOException {
        InputStream stream = getClass().getResourceAsStream("/fixtures/" + name);
        if (stream == null) {
            throw new IOException("Fixture de login no encontrado: " + name);
        }
        try (BufferedSource source = Okio.buffer(Okio.source(stream))) {
            return source.readUtf8();
        }
    }
}
