package io.github.tcdl.benchmarks.bus.retry;

import io.github.tcdl.benchmarks.bus.Route;
import io.github.tcdl.benchmarks.bus.http.BusToHttpService;
import io.github.tcdl.benchmarks.bus.http.HttpClientException;
import io.github.tcdl.msb.api.message.payload.RestPayload;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetryTemplate;

import javax.annotation.PreDestroy;
import java.util.function.Consumer;

public class BusToHttpServiceDecorator implements BusToHttpService {

    private final BusToHttpService busToHttpService;

    private final RetryTemplate retryTemplate;

    private volatile boolean isTerminated = false;

    @Autowired
    public BusToHttpServiceDecorator(BusToHttpService busToHttpService, RetryTemplate retryTemplate) {
        this.busToHttpService = busToHttpService;
        this.retryTemplate = retryTemplate;
    }

    @PreDestroy
    public void terminate() {
        isTerminated = true;
    }

    @Override
    public void send(RestPayload request, Route.Http http, Consumer<HttpResponse> onResponse) throws HttpClientException {
        run(() -> this.busToHttpService.send(request, http, onResponse));
    }

    public void run(Runnable runnable) {
        retryTemplate.execute((RetryContext context) -> {
            try {
                exhaustIfTerminated(context);
                runnable.run();
                return null;
            } catch (Exception e) {
                throw new HttpClientException("Error occurred while sending request" , e);
            }
        });
    }

    private void exhaustIfTerminated(RetryContext retryContext) {
        if(isTerminated) {
            retryContext.setExhaustedOnly();
        }
    }
}

