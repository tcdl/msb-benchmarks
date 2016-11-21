package io.github.tcdl.benchmarks.bus.http;

import io.github.tcdl.benchmarks.bus.Route;
import io.github.tcdl.msb.api.message.payload.RestPayload;
import org.apache.http.HttpResponse;

import java.util.function.Consumer;

public interface BusToHttpService {

    void send(RestPayload request, Route.Http http, Consumer<HttpResponse> onResponse) throws HttpClientException;
}
