package com.ammaia_ispc.sangreyamobile.helpers;

import static org.junit.Assert.assertEquals;

import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.Test;

public class TokenAuthenticatorTest {

    private static Response fake401(Response prior) {
        Request request = new Request.Builder()
                .url("https://sangreyaispc.pythonanywhere.com/usuarios/")
                .build();
        Response.Builder builder = new Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(401)
                .message("Unauthorized");
        if (prior != null) {
            builder.priorResponse(prior);
        }
        return builder.build();
    }

    @Test
    public void firstFailureCountsAsOne() {
        Response firstFailure = fake401(null);
        assertEquals(1, TokenAuthenticator.responseCount(firstFailure));
    }

    @Test
    public void retriedRequestFailingAgainCountsAsTwo() {
        Response firstFailure = fake401(null);
        Response secondFailure = fake401(firstFailure);
        assertEquals(2, TokenAuthenticator.responseCount(secondFailure));
    }
}
