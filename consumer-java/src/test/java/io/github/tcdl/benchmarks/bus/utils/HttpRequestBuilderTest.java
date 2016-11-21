package io.github.tcdl.benchmarks.bus.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.ImmutableMap;
import io.github.tcdl.msb.api.message.payload.RestPayload;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import sun.misc.BASE64Encoder;

import java.io.IOException;
import java.net.URI;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;

public class HttpRequestBuilderTest {

    public static String BASE_URL = "http://localhost:8080";
    private final static String PAYLOAD_TRANSFORMER =
            "[\n"
            + "\t{\n"
            + "\t\t\"operation\": \"shift\",\n"
            + "\t\t\"spec\": {\n"
            + "\t\t\t\"body\": {\n"
            + "\t\t\t\t\"field\": \"body.newfield\"\n"
            + "\t\t\t}\n"
            + "\t\t}\n"
            + "\t}\n"
            + "]";
    private final static String VALIDATION_SCHEMA =
            "{\n"
            + "\"type\" : \"object\",\n"
            + "  \"properties\" : {\n"
            + "    \"query\" : {\n"
            + "      \"type\" : \"object\",\n"
            + "      \"properties\" : {\n"
            + "        \"id\" : {\"type\" : \"string\"}\n"
            + "      },\n"
            + "      \"required\" : [\"id\"]\n"
            + "    }   \n"
            + "  }\n"
            + "}";

    HttpRequestBuilder httpRequestBuilder;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public  void setUp() {
        httpRequestBuilder = new HttpRequestBuilder(new ObjectMapper()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL));
    }

    @Test
    public void testUrl() throws Exception {
        RestPayload busRequest = new RestPayload.Builder<>().build();
        HttpRequestWrapper httpRequest = HttpRequestWrapper.wrap(createBasicRequestBuilder(busRequest, "", "GET").build());

        assertEquals(new URI(BASE_URL), httpRequest.getURI());
        assertEquals("GET", httpRequest.getMethod());
    }

    @Test
    public void testQueryParams() {
        RestPayload busRequest = new RestPayload.Builder<>()
                .withQuery(ImmutableMap.of("param", "value"))
                .build();

        HttpRequestWrapper httpRequest = HttpRequestWrapper.wrap(createBasicRequestBuilder(busRequest, "/get", "GET").build());

        String queryString = HttpRequestWrapper.wrap(httpRequest).getURI().getQuery();

        assertEquals("param=value", queryString);
    }

    @Test
    public void testQueryParamsWithValidation() throws IOException {
        RestPayload busRequest = new RestPayload.Builder<>()
                .withQuery(ImmutableMap.of("id", "1"))
                .build();

        HttpRequest httpRequest = HttpRequestWrapper.wrap(createBasicRequestBuilder(busRequest, "/get", "GET")
                .withPayloadValidator(VALIDATION_SCHEMA)
                .build());

        String queryString = HttpRequestWrapper.wrap(httpRequest).getURI().getQuery();

        assertEquals("id=1", queryString);
    }

    @Test
    public void testRestParams() {
        RestPayload busRequest = new RestPayload.Builder<>()
                .withParams(ImmutableMap.of("user-id", "1"))
                .build();

        HttpRequestWrapper httpRequest = HttpRequestWrapper.wrap(createBasicRequestBuilder(busRequest, "/get/{user-id}", "GET").build());
        String restPath = HttpRequestWrapper.wrap(httpRequest).getURI().getPath();

        assertEquals("/get/1", restPath);
    }

    @Test
    public void testHeaders() {
        String headerName = "msb-request-id";
        RestPayload busRequest = new RestPayload.Builder<>()
                .withHeaders(ImmutableMap.of(headerName, "uuid"))
                .build();

        HttpRequestWrapper httpRequest = HttpRequestWrapper.wrap(createBasicRequestBuilder(busRequest, "/get", "GET").build());
        Header header = HttpRequestWrapper.wrap(httpRequest).getLastHeader(headerName);

        assertEquals(headerName, header.getName());
        assertEquals("uuid", header.getValue());
    }

    @Test
    public void testBody() throws IOException {
        RestPayload busRequest = new RestPayload.Builder<>()
                .withBody(ImmutableMap.of("field", "value")).build();

        HttpRequest httpRequest = HttpRequestWrapper.wrap(createBasicRequestBuilder(busRequest, "/get", "GET").build());
        HttpEntity httpEntity = ((HttpEntityEnclosingRequest) httpRequest).getEntity();

        assertEquals("{\"field\":\"value\"}", EntityUtils.toString(httpEntity));
    }

    @Test
    public void testBodyBuffer() throws IOException {
        String content = "base64 content";
        RestPayload busRequest = new RestPayload.Builder<>()
                .withBodyBuffer(content.getBytes()).build();

        HttpRequest httpRequest = HttpRequestWrapper.wrap(createBasicRequestBuilder(busRequest, "/get", "GET").build());
        HttpEntity httpEntity = ((HttpEntityEnclosingRequest) httpRequest).getEntity();

        assertEquals(new BASE64Encoder().encode(content.getBytes()), EntityUtils.toString(httpEntity));
    }

    @Test
    public void testBodyWithTransformation() throws IOException {
        RestPayload busRequest = new RestPayload.Builder<>()
                .withBody(ImmutableMap.of("field", "value")).build();

        HttpRequest httpRequest = HttpRequestWrapper.wrap(createBasicRequestBuilder(busRequest, "/get", "GET")
                .withPayloadTransformer(PAYLOAD_TRANSFORMER)
                .build());

        HttpEntity httpEntity = ((HttpEntityEnclosingRequest) httpRequest).getEntity();

        assertEquals("{\"newfield\":\"value\"}", EntityUtils.toString(httpEntity));
    }

    @Test
    public void testValidationError() throws IOException {
        thrown.expect(HttpRequestBuilderException.class);
        thrown.expectMessage(containsString("object has missing required properties ([\"id\"])"));

        RestPayload busRequest = new RestPayload.Builder<>()
                .withQuery(ImmutableMap.of("param", "value"))
                .build();

        createBasicRequestBuilder(busRequest, "/get", "GET")
                .withPayloadValidator(VALIDATION_SCHEMA)
                .build();
    }

    private HttpRequestBuilder createBasicRequestBuilder(RestPayload busRequest, String path, String method) {
        return httpRequestBuilder
                .withRequest(busRequest)
                .withUrlPattern(BASE_URL + path)
                .withMethod(method);
    }
}
