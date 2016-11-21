package io.github.tcdl.benchmarks.bus.retry;

import io.github.tcdl.benchmarks.bus.Route;
import io.github.tcdl.benchmarks.bus.http.BusToHttpService;
import io.github.tcdl.msb.api.message.payload.RestPayload;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class BusToHttpServiceDecoratorTest {

    @Mock
    BusToHttpService service;

    RetryTemplate retryTemplate;

    BusToHttpServiceDecorator decorator;

    RestPayload request = new RestPayload.Builder<>().build();
    Route.Http to = new Route.Http("http://localhost/post", "POST", null, null);

    @Before
    public void setUp() {
        ExponentialBackOffPolicy result = new ExponentialBackOffPolicy();
        result.setInitialInterval(1);
        result.setMultiplier(1.1);
        retryTemplate = new RetryTemplate();
        retryTemplate.setBackOffPolicy(result);
        decorator = new BusToHttpServiceDecorator(service, retryTemplate);
    }

    @Test
    public void testSend() {
        doThrow(RuntimeException.class)
                .doNothing()
                .when(service)
                .send(request, to, null);
        decorator.send(request, to, null);
        verify(service, times(2)).send(request, to, null);
    }

    @Test(expected = RuntimeException.class)
    public void testSendFailAfterTerminated() {
        doThrow(RuntimeException.class)
                .doNothing()
                .when(service)
                .send(request, to, null);
        decorator.terminate();
        decorator.send(request, to, null);
    }

    @Test
    public void testSendSuccessAfterTerminated() {
        doNothing()
                .when(service)
                .send(request, to, null);
        decorator.terminate();
        decorator.send(request, to, null);
        verify(service).send(request, to, null);
    }
}