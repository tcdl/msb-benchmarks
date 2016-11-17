package com.thomascook.bus.retry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;

public class HttpRetryListener implements RetryListener {

    private static final Logger LOG = LoggerFactory.getLogger(HttpRetryListener.class);

    @Override
    public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
        return true;
    }

    @Override
    public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
        if (throwable != null) {
            LOG.warn("[RETRY] The final retry attempt was not successful", throwable);
        }
    }

    @Override
    public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
        if (throwable != null) {
            LOG.warn("[RETRY] Error when trying to perform a request, a retry attempt is expected (if not exhausted).", throwable);
        }
    }
}
