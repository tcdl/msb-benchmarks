package io.github.tcdl.benchmarks.bus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.typesafe.config.Config;
import io.github.tcdl.msb.api.MsbContext;
import io.github.tcdl.msb.api.RequestOptions;
import io.github.tcdl.msb.api.message.payload.RestPayload;
import io.github.tcdl.msb.mock.adapterfactory.TestMsbStorageForAdapterFactory;
import io.github.tcdl.msb.support.Utils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.StringBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URL;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(Bus2HttpApplication.class)
public class Bus2HttpApplicationITest {

    @Autowired
    ApplicationConfig appConfig;
    @Autowired
    MsbContext msbContext;
    @Autowired
    ObjectMapper objectMapper;

    String namespace;
    URL url;
    String method;
    RestPayload<Map, Map, Map, Map> lastResponse;
    ClientAndServer webServer;

    @Before
    public void setUp() throws Exception {
        Config routeConfig = appConfig.getConfig().getConfigList("routes").get(0);
        url = new URL(routeConfig.getString("http.url"));
        method = routeConfig.getString("http.method");
        namespace = routeConfig.getString("bus.namespace");

        TestMsbStorageForAdapterFactory.extract(msbContext);
        webServer = ClientAndServer.startClientAndServer(url.getPort());
    }

    @After
    public void tearDown() {
        webServer.reset();
        webServer.stop(true);
        appConfig.getMsbContext().shutdown();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSuccess() throws Exception {
        respondWith(HttpStatus.OK.value(),
                toJson(ImmutableMap.of("result", "ok")),
                "token", "uuid");

        RestPayload request = new RestPayload.Builder<>().build();
        publish(namespace, request);

        assertNotNull(lastResponse);
        assertEquals(HttpStatus.OK.value(), lastResponse.getStatusCode().intValue());
        assertEquals("ok", lastResponse.getBody().get("result"));
        assertEquals("uuid", lastResponse.getHeaders().get("token"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSuccessWithRetry() throws Exception {
        respondWith(HttpStatus.INTERNAL_SERVER_ERROR.value(), null);
        respondWith(HttpStatus.BAD_REQUEST.value(), null);
        respondWith(HttpStatus.OK.value(), toJson(ImmutableMap.of("result", "ok")));

        RestPayload request = new RestPayload.Builder<>().build();
        publish(namespace, request);

        assertNotNull(lastResponse);
        assertEquals(HttpStatus.OK.value(), lastResponse.getStatusCode().intValue());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testNotFound() throws Exception {
        RestPayload request = new RestPayload.Builder<>().build();
        publish(namespace, request);

        assertNull(lastResponse);
    }

    private RestPayload publish(String namespace, RestPayload request) throws InterruptedException {
        RequestOptions requestOptions = new RequestOptions.Builder()
                .withWaitForResponses(1).build();

        lastResponse = null;
        CountDownLatch await = new CountDownLatch(1);
        msbContext.getObjectFactory().createRequester(namespace, requestOptions, RestPayload.class)
                .onResponse((response, ackHandler) -> {
                    lastResponse = response;
                    await.countDown();
                }).publish(request);

        await.await(2000, TimeUnit.MILLISECONDS);

        return lastResponse;
    }

    private void respondWith(int statusCode, String responseBody) {
        respondWith(statusCode, responseBody, null, null);
    }

    private void respondWith(int statusCode, String responseBody, String headerName, String headerValue) {
        HttpRequest httpReq = HttpRequest.request()
                .withMethod(String.valueOf(method))
                .withPath(url.getPath());

        HttpResponse httpResp = HttpResponse.response()
                .withStatusCode(statusCode);

        if (headerName != null) {
            httpResp.withHeader(headerName, headerValue);
        }

        if (responseBody != null) {
            httpResp.withBody(new StringBody(responseBody));
        }

        webServer.when(httpReq).respond(httpResp);
    }

    private String toJson(Object object) {
        return  Utils.toJson(object, objectMapper);
    }
}