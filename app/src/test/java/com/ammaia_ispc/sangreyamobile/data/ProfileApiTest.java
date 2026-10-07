package com.ammaia_ispc.sangreyamobile.data;

import com.ammaia_ispc.sangreyamobile.helpers.ApiClient;
import com.ammaia_ispc.sangreyamobile.helpers.ApiService;
import com.ammaia_ispc.sangreyamobile.model.ProfileViewModel;
import com.ammaia_ispc.sangreyamobile.model.UserUpdateRequest;
import com.google.gson.Gson;
import com.google.gson.JsonParser;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okhttp3.mockwebserver.SocketPolicy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProfileApiTest {
    private MockWebServer server;
    private OkHttpClient client;
    private ApiService api;

    @Before
    public void setUp() throws IOException {
        server = new MockWebServer();
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                if ("PUT".equals(request.getMethod())
                        && "/usuarios/7/".equals(request.getPath())) {
                    return new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE);
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
                .followSslRedirects(false)
                .retryOnConnectionFailure(false)
                .readTimeout(500, TimeUnit.MILLISECONDS)
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

    // TC-NET-03: transporte real contra MockWebServer local.
    @Test
    public void timeoutMuestraErrorSinPerderDatos() throws Exception {
        // Arrange
        ProfileViewModel.SessionStore session = mock(ProfileViewModel.SessionStore.class);
        when(session.getUserId()).thenReturn(7);
        ProfileViewModel viewModel = new ProfileViewModel(new UserApiRepository(api), session);
        UserUpdateRequest form = new UserUpdateRequest("donante.editado", "editado@example.test",
                "23456789", "Ana Maria", "Prueba", "1995-04-12");
        CountDownLatch completed = new CountDownLatch(1);

        // Act
        viewModel.save(form, state -> {
            if (!state.loading) {
                completed.countDown();
            }
        });

        // Assert
        assertTrue("El timeout debe llegar al estado UI", completed.await(5, TimeUnit.SECONDS));
        ProfileViewModel.State failed = viewModel.getState();
        assertTrue(failed.failure instanceof SocketTimeoutException);
        assertFalse(failed.loading);
        assertSame(form, failed.form);

        RecordedRequest request = server.takeRequest(1, TimeUnit.SECONDS);
        assertEquals("PUT", request.getMethod());
        assertEquals("/usuarios/7/", request.getPath());
        assertEquals(new Gson().toJsonTree(form), JsonParser.parseString(request.getBody().readUtf8()));
        assertEquals(1, server.getRequestCount());
    }
}
