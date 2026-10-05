package com.ammaia_ispc.sangreyamobile.data;

import com.ammaia_ispc.sangreyamobile.helpers.ApiClient;
import com.ammaia_ispc.sangreyamobile.helpers.ApiService;
import com.ammaia_ispc.sangreyamobile.model.LoginRequest;
import com.ammaia_ispc.sangreyamobile.model.LoginResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okio.BufferedSource;
import okio.Okio;
import retrofit2.Response;

import static org.junit.Assert.assertEquals;

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
        String fixture = readFixture();
        server = new MockWebServer();
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                if ("POST".equals(request.getMethod())
                        && "/api/token/".equals(request.getPath())) {
                    return new MockResponse()
                            .setResponseCode(200)
                            .setHeader("Content-Type", "application/json; charset=utf-8")
                            .setBody(fixture);
                }
                return new MockResponse().setResponseCode(404);
            }
        });
        InetAddress loopback = InetAddress.getByAddress(new byte[]{127, 0, 0, 1});
        server.start(loopback, 0);
        HttpUrl localUrl = server.url("/").newBuilder().host("127.0.0.1").build();

        client = new OkHttpClient.Builder()
                .proxy(Proxy.NO_PROXY)
                .followRedirects(false)
                .callTimeout(3, TimeUnit.SECONDS)
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

    private String readFixture() throws IOException {
        InputStream stream = getClass().getResourceAsStream("/fixtures/login_200.json");
        if (stream == null) {
            throw new IOException("Fixture de login no encontrado");
        }
        try (BufferedSource source = Okio.buffer(Okio.source(stream))) {
            return source.readUtf8();
        }
    }
}
