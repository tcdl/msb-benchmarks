package io.github.tcdl.benchmarks.bus.retry;

import com.typesafe.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryListener;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
public class HttpRetryConfiguration {

    private final static String CONFIG_PREFIX = "retry";

    @Autowired
    private Config config;

    @Bean
    public RetryTemplate getRetryTemplate(RetryListener retryListener) {

        int maxRetries = config.getInt(CONFIG_PREFIX + ".maxRetries");
        int minDelay = config.getInt(CONFIG_PREFIX + ".backoff.min");
        int multiplier = config.getInt(CONFIG_PREFIX + ".backoff.multiplier");

        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(minDelay);
        backOffPolicy.setMultiplier(multiplier);
        backOffPolicy.setMaxInterval(Integer.MAX_VALUE);

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(maxRetries + 1);

        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setBackOffPolicy(backOffPolicy);
        retryTemplate.setRetryPolicy(retryPolicy);
        retryTemplate.registerListener(retryListener);

        return retryTemplate;
    }

    @Bean
    public HttpRetryListener getRetryListener() {
        return new HttpRetryListener();
    }
}