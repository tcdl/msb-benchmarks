package io.github.tcdl.benchmarks.bus.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tcdl.benchmarks.bus.Route;
import io.github.tcdl.benchmarks.bus.utils.HttpRequestBuilder;
import io.github.tcdl.msb.api.message.payload.RestPayload;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Consumer;

public class BusToHttpServiceImpl implements  BusToHttpService {

    private ObjectMapper objectMapper;
    private CloseableHttpClient httpClient;

    public BusToHttpServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = createHttpClient();
    }

    public void send(RestPayload request, Route.Http http, Consumer<HttpResponse> onResponse) throws HttpClientException {

        HttpRequest httpRequest = new HttpRequestBuilder(objectMapper)
                .withRequest(request)
                .withPayloadTransformer(http.getPayloadTransformer())
                .withPayloadValidator(http.getPayloadValidator())
                .withUrlPattern(http.getUrl())
                .withMethod(http.getMethod())
                .build();

        try {
            URI url = new URI(httpRequest.getRequestLine().getUri());
            HttpHost host = new HttpHost(url.getHost(), url.getPort(), url.getScheme());

            this.httpClient.execute(host, httpRequest, response -> {

                if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                    throw new HttpClientException(response.getStatusLine().getStatusCode());
                }

                if (onResponse != null) {
                    onResponse.accept(response);
                }
                return null;

            }, createHttpClientContext());
        } catch (URISyntaxException | IOException e) {
            throw new HttpClientException(e.getMessage(), e);
        }
    }

    private CloseableHttpClient createHttpClient() {
        return HttpClientBuilder.create()
                .disableCookieManagement()
                .disableAutomaticRetries()
                .build();
    }

    private HttpClientContext createHttpClientContext() {
        return HttpClientContext.create();
    }
}
