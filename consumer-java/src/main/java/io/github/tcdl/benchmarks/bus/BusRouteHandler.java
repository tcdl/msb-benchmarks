package io.github.tcdl.benchmarks.bus;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tcdl.benchmarks.bus.http.BusToHttpService;
import io.github.tcdl.benchmarks.bus.utils.BusResponseBuilder;
import io.github.tcdl.msb.api.MessageTemplate;
import io.github.tcdl.msb.api.MsbContext;
import io.github.tcdl.msb.api.ResponderContext;
import io.github.tcdl.msb.api.message.Message;
import io.github.tcdl.msb.api.message.payload.RestPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BusRouteHandler {

    private static final Logger LOG = LoggerFactory.getLogger(BusRouteHandler.class);

    private MsbContext msbContext;
    private ObjectMapper objectMapper;
    private BusToHttpService busToHttpService;
    private Route route;

    public BusRouteHandler(BusToHttpService busToHttpService, MsbContext msbContext, ObjectMapper objectMapper) {
        this.busToHttpService = busToHttpService;
        this.msbContext = msbContext;
        this.objectMapper = objectMapper;
    }

    public BusRouteHandler withRoute(Route route) {
        this.route = route;
        return this;
    }

    public void start() {
        msbContext.getObjectFactory()
                .createResponderServer(
                        route.getBus().getNamespace(),
                        new MessageTemplate(),
                        this::onRequest, RestPayload.class)
                .listen();
    }

    void onRequest(RestPayload request, ResponderContext responderContext) {
        return;
    }
}
