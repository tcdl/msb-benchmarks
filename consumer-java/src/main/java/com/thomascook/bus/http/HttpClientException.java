package com.thomascook.bus.http;

public class HttpClientException extends RuntimeException {

    public HttpClientException(Integer responseCode) {
        super(String.format("Failed to call external system. Response code: '%d'.", responseCode));
    }

    public HttpClientException(String message, Throwable e) {
        super(message, e);
    }
}