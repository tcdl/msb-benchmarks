package com.thomascook.bus.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tcdl.msb.api.exception.JsonSchemaValidationException;
import io.github.tcdl.msb.api.message.payload.RestPayload;
import io.github.tcdl.msb.support.JsonValidator;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.springframework.web.util.UriComponentsBuilder;
import sun.misc.BASE64Encoder;

import java.net.URI;
import java.util.Collections;
import java.util.Map;

import static io.github.tcdl.msb.support.Utils.convert;
import static io.github.tcdl.msb.support.Utils.ifNull;
import static io.github.tcdl.msb.support.Utils.toJson;

public class HttpRequestBuilder {

    private ObjectMapper objectMapper;
    private JsonValidator validator;

    private RestPayload<Map, Map, Map, Map> request;
    private String payloadTransformer;
    private String payloadValidator;
    private String urlPattern;
    private String method;

    public HttpRequestBuilder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.validator = new JsonValidator();
    }

    @SuppressWarnings("unchecked")
    public HttpRequestBuilder withRequest(RestPayload request) {
        this.request = request;
        return this;
    }

    public HttpRequestBuilder withPayloadTransformer(String payloadTransformer) {
        this.payloadTransformer = payloadTransformer;
        return this;
    }

    public HttpRequestBuilder withPayloadValidator(String payloadValidator) {
        this.payloadValidator = payloadValidator;
        return this;
    }

    public HttpRequestBuilder withUrlPattern(String urlPattern) {
        this.urlPattern = urlPattern;
        return this;
    }

    public HttpRequestBuilder withMethod(String method) {
        this.method = method;
        return this;
    }

    @SuppressWarnings("unchecked")
    public HttpRequest build() {

        // perform payload transformations
        if (payloadTransformer != null) {
            Map requestAsMap = convert(request, Map.class, objectMapper);
            requestAsMap = JsonTransformer.transform(requestAsMap, payloadTransformer);
            request = ifNull(convert(requestAsMap, RestPayload.class, objectMapper), new RestPayload.Builder<>().build());
        }

        // validate
        if (payloadValidator != null) {
            try {
                String requestAsJson = toJson(request, objectMapper);
                validator.validate(requestAsJson, payloadValidator);
            } catch (JsonSchemaValidationException e) {
                throw new HttpRequestBuilderException(e.getMessage(), e);
            }
        }

        Map<String, String> headers = request.getHeaders();
        Map<String, String> query = request.getQuery();
        Map<String, String> params = request.getParams();
        Map body = request.getBody();
        byte[] bodyBuffer = request.getBodyBuffer();

        // substitute rest params if any
        URI url = UriComponentsBuilder
                .fromHttpUrl(urlPattern)
                .buildAndExpand(ifNull(params, Collections.EMPTY_MAP)).toUri();
        RequestBuilder requestBuilder = RequestBuilder.create(method).setUri(url);

        // put query params
        if (query != null) {
            query.forEach(requestBuilder::addParameter);
        }

        // put headers from bus request
        if (headers != null) {
            headers.forEach(requestBuilder::addHeader);
        }

        // put body
        if (body != null) {
            requestBuilder.setEntity(new ByteArrayEntity(toJson(body, objectMapper).getBytes()));
        } else if (bodyBuffer != null) {
            requestBuilder.setEntity(new ByteArrayEntity(new BASE64Encoder().encode(bodyBuffer).getBytes()));
        }

        return requestBuilder.build();
    }
}
