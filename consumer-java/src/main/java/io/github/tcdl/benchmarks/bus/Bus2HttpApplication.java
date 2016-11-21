package io.github.tcdl.benchmarks.bus;

import io.github.tcdl.benchmarks.bus.lifecycle.Bus2HttpApplicationEventListener;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class Bus2HttpApplication {

    public static final Config CONFIG = ConfigFactory.load();

    public static final SpringApplication APP =
            new SpringApplicationBuilder()
                    .bannerMode(Banner.Mode.OFF)
                    .sources(Bus2HttpApplication.class)
                    .listeners(new Bus2HttpApplicationEventListener())
                    .build();

    public static void main(String[] args) {
        APP.run(args);
    }
}