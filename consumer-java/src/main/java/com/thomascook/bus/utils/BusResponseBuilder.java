package com.thomascook.bus.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import io.github.tcdl.msb.api.exception.JsonConversionException;
import io.github.tcdl.msb.api.message.payload.RestPayload;
import io.github.tcdl.msb.support.Utils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class BusResponseBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(BusResponseBuilder.class);

    private ObjectMapper objectMapper;
    private HttpResponse response;

    public BusResponseBuilder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public BusResponseBuilder withResponse(HttpResponse response) {
        this.response = response;
        return this;
    }

    public RestPayload build() {

        Integer statusCode = null;
        String statusMessage = null;
        Map<String, String> headers = new HashMap<>();
        Map body = null;

        if (response != null) {
            statusCode = response.getStatusLine().getStatusCode();
            statusMessage = response.getStatusLine().getReasonPhrase();
            Arrays.asList(response.getAllHeaders()).forEach(h -> headers.put(h.getName(), h.getValue()));
            String rawBody = StringUtils.EMPTY;

            try {
                HttpEntity responseEntity = response.getEntity();
                if (responseEntity != null && responseEntity.getContent() != null) {
                    rawBody = IOUtils.toString(responseEntity.getContent());
                    body = Utils.fromJson(rawBody, Map.class, objectMapper);
                }
            } catch (IOException e) {
                throw new BusResponseBuilderException("Unable to read response body", e);
            } catch (JsonConversionException e) {
                LOG.error("Unable to parse response body", e);
                body = ImmutableMap.of("rawResponse", rawBody);
            }
        }

        return new RestPayload.Builder<>()
                .withStatusCode(statusCode)
                .withStatusMessage(statusMessage)
                .withHeaders(headers)
                .withBody(body)
                .build();
    }
}
