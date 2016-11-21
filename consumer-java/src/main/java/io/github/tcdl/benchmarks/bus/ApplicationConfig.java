package io.github.tcdl.benchmarks.bus;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.github.tcdl.benchmarks.bus.http.BusToHttpService;
import io.github.tcdl.benchmarks.bus.http.BusToHttpServiceImpl;
import io.github.tcdl.benchmarks.bus.retry.BusToHttpServiceDecorator;
import com.typesafe.config.Config;
import io.github.tcdl.msb.api.MsbContext;
import io.github.tcdl.msb.api.MsbContextBuilder;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.support.RetryTemplate;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.github.tcdl.msb.config.ConfigurationUtil.getBoolean;
import static io.github.tcdl.msb.config.ConfigurationUtil.getOptionalString;
import static io.github.tcdl.msb.config.ConfigurationUtil.getString;

@Configuration
public class ApplicationConfig {

    public static final String ROUTES_CONFIG = "routes";
    public static final String BUS_CONFIG = "bus";
    public static final String HTTP_CONFIG = "http";

    @Autowired
    private ApplicationContext context;

    @Autowired
    private Config config;

    @Autowired
    private MsbContext msbContext;

    @PreDestroy
    public void stop() {
        msbContext.shutdown();
    }

    @Bean
    public Config getConfig() {
        return Bus2HttpApplication.CONFIG;
    }

    @Bean
    public MsbContext getMsbContext() {
        return new MsbContextBuilder()
                .enableShutdownHook(true)
                .withConfig(config)
                .build();
    }

    @Bean
    public ObjectMapper getObjectMapper() {
        return new ObjectMapper()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Bean
    public BusToHttpService getBusToHttpService(ObjectMapper objectMapper, RetryTemplate retryTemplate) {
        return new BusToHttpServiceDecorator(new BusToHttpServiceImpl(objectMapper), retryTemplate);
    }

    @Bean(name = "routes")
    public List<Route> getRoutes() {

        List<? extends Config> routeConfigs = config.getConfigList(ROUTES_CONFIG);
        List<Route> routes = new ArrayList<>();

        for (Config routeConfig : routeConfigs) {
            Route.Builder routeBuilder = new Route.Builder()
                .withNamespace(getString(routeConfig, BUS_CONFIG + ".namespace"))
                .withWaitForResponse(getBoolean(routeConfig, BUS_CONFIG + ".waitForResponse"))
                .withUrl(getString(routeConfig, HTTP_CONFIG + ".url"))
                .withMethod(getString(routeConfig, HTTP_CONFIG + ".method"));

            Optional<String> payloadTransformer = getOptionalString(routeConfig, HTTP_CONFIG + ".payloadTransformer");
            if (payloadTransformer.isPresent() && StringUtils.isNotEmpty(payloadTransformer.get())) {
                routeBuilder.withPayloadTransformer(loadResource(payloadTransformer.get()));
            }

            Optional<String> payloadValidator = getOptionalString(routeConfig, HTTP_CONFIG + ".payloadValidator");
            if (payloadValidator.isPresent() && StringUtils.isNotEmpty(payloadValidator.get())) {
                routeBuilder.withPayloadValidator(loadResource(payloadValidator.get()));
            }

            routes.add(routeBuilder.build());
        }

        return routes;
    }

    private String loadResource(String url) {
        try {
            return IOUtils.toString(context.getResource(url).getInputStream());
        } catch (Exception e) {
            throw new BeanCreationException(e.getMessage(), e);
        }
    }
}
