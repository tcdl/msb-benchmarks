package com.thomascook.bus.http;

import com.thomascook.bus.Route;
import io.github.tcdl.msb.api.message.payload.RestPayload;
import org.apache.http.HttpResponse;

import java.util.function.Consumer;

public interface BusToHttpService {

    void send(RestPayload request, Route.Http http, Consumer<HttpResponse> onResponse) throws HttpClientException;
}
