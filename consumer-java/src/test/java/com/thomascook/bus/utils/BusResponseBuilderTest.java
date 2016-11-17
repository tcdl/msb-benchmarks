package com.thomascook.bus.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.ImmutableMap;
import io.github.tcdl.msb.api.message.payload.RestPayload;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.util.Map;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class BusResponseBuilderTest {

    BusResponseBuilder busResponseBuilder;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public  void setUp() {
        busResponseBuilder = new BusResponseBuilder(new ObjectMapper()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL));
    }

    @Test
    public void testOkStatus() {
        HttpResponse httpResponse = new BasicHttpResponse(status(HttpStatus.SC_OK, "ok"));
        RestPayload busResponse = busResponseBuilder.withResponse(httpResponse).build();

        assertEquals(HttpStatus.SC_OK, busResponse.getStatusCode().intValue());
        assertEquals("ok", busResponse.getStatusMessage());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testHeaders() {
        HttpResponse httpResponse = new BasicHttpResponse(status(HttpStatus.SC_OK, "ok"));
        httpResponse.addHeader("header", "value");
        RestPayload<Object, Map, Object, Object> busResponse = busResponseBuilder.withResponse(httpResponse).build();

        assertEquals(1, busResponse.getHeaders().size());
        assertEquals("value", busResponse.getHeaders().get("header"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testJsonBody() throws Exception {
        String jsonBody = "{\"result\" : \"ok\"}";
        HttpResponse httpResponse = new BasicHttpResponse(status(HttpStatus.SC_OK, "ok"));
        httpResponse.setEntity(new StringEntity(jsonBody));
        RestPayload<Object, Object, Object, Map> busResponse = busResponseBuilder.withResponse(httpResponse).build();

        assertEquals("ok", busResponse.getBody().get("result"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRawBody() throws Exception {
        String body = "body content";
        HttpResponse httpResponse = new BasicHttpResponse(status(HttpStatus.SC_OK, "ok"));
        httpResponse.setEntity(new StringEntity(body));
        RestPayload<Object, Object, Object, Map> busResponse = busResponseBuilder.withResponse(httpResponse).build();

        assertEquals(ImmutableMap.of("rawResponse", body), busResponse.getBody());
    }

    @Test
    public void testReadResponseBodyError() throws IOException {
        thrown.expect(BusResponseBuilderException.class);
        thrown.expectMessage(containsString("Unable to read response body"));

        HttpEntity httpEntityMock = mock(HttpEntity.class);
        when(httpEntityMock.getContent()).thenThrow(new IOException());

        HttpResponse httpResponse = new BasicHttpResponse(status(HttpStatus.SC_OK, "ok"));
        httpResponse.setEntity(httpEntityMock);
        busResponseBuilder.withResponse(httpResponse).build();
    }

    private StatusLine status(int status, String reason) {
        return new BasicStatusLine(new ProtocolVersion("http", 1, 1), status, reason);
    }
}
