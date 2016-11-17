package com.thomascook.bus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thomascook.bus.http.BusToHttpService;
import io.github.tcdl.msb.api.MsbContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class BusController {

    private static final Logger LOG = LoggerFactory.getLogger(BusController.class);

    @Autowired
    private MsbContext msbContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BusToHttpService busToHttpService;

    @Resource(name = "routes")
    private List<Route> routes;

    @PostConstruct
    public void init() throws IOException {
        List<String> namespaces = new ArrayList<>();
        for (Route route : routes) {
            new BusRouteHandler(busToHttpService, msbContext, objectMapper)
                    .withRoute(route)
                    .start();
            namespaces.add(route.getBus().getNamespace());
        }

        LOG.info("Bus2Http-microservice started for namespaces: {}", namespaces);
    }
}
