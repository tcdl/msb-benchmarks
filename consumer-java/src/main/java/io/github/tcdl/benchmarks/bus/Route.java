package io.github.tcdl.benchmarks.bus;

public class Route {

    private Bus bus;
    private Http http;

    private Route(Bus bus, Http http) {
        this.bus = bus;
        this.http = http;
    }

    public static class Bus {

        private String namespace;
        private boolean waitForResponse;

        public Bus(String namespace, boolean waitForResponse) {
            this.namespace = namespace;
            this.waitForResponse = waitForResponse;
          }

        public String getNamespace() {
            return namespace;
        }

        public boolean getWaitForResponse() {
            return waitForResponse;
        }
    }

    public static class Http {

        private String url;
        private String method;
        private String payloadTransformer;
        private String payloadValidator;

        public Http(String url, String method, String payloadTransformer, String payloadValidator) {
            this.url = url;
            this.method = method;
            this.payloadTransformer = payloadTransformer;
            this.payloadValidator = payloadValidator;
        }

        public String getUrl() {
            return url;
        }

        public String getMethod() {
            return method;
        }

        public String getPayloadTransformer() {
            return payloadTransformer;
        }

        public String getPayloadValidator() {
            return payloadValidator;
        }
    }

    public Bus getBus() {
        return bus;
    }

    public Http getHttp() {
        return http;
    }

    public static class Builder {

        private String namespace;
        private boolean waitForResponse;

        private String url;
        private String method;
        private String payloadTransformer;
        private String payloadValidator;

        public Builder withNamespace(String namespace) {
            this.namespace = namespace;
            return this;
        }

        public Builder withWaitForResponse(boolean waitForResponse) {
            this.waitForResponse = waitForResponse;
            return this;
        }

        public Builder withPayloadTransformer(String payloadTransformer) {
            this.payloadTransformer = payloadTransformer;
            return this;
        }

        public Builder withPayloadValidator(String payloadValidator) {
            this.payloadValidator = payloadValidator;
            return this;
        }

        public Builder withUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder withMethod(String method) {
            this.method = method;
            return this;
        }

        public Route build() {
            return new Route(
                    new Bus(namespace, waitForResponse),
                    new Http(url, method, payloadTransformer, payloadValidator));
        }
    }
}
