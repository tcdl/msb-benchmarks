package com.thomascook.bus.lifecycle;

import com.thomascook.bus.Bus2HttpApplication;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;

public class Bus2HttpApplicationEventListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(Bus2HttpApplicationEventListener.class);

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        LOG.info("Starting Http2Bus-microservice...");

        // Parse config
        Config maskedConfig = Bus2HttpApplication.CONFIG
                .withValue("msbConfig.brokerConfig.password", ConfigValueFactory.fromAnyRef("xxx"));

        LOG.info("Using configuration: {}", maskedConfig.root().render());
    }
}
