package trudy;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;

import java.util.Date;
import java.util.List;
import java.util.Map;

public final class Exchange {
    private final FullHttpResponse response;
    private final String uri;
    private final String method;
    private final String request;
    private List<Map.Entry<String, String>> responseHeaders;
    private byte[] responseBody;
    private int status;
    private String name;
    private String matchingUri;
    private final Date timestamp;

    public Exchange(FullHttpRequest request, FullHttpResponse response) {
        this.response = response;
        this.timestamp = new Date();
        this.uri = request.getUri();
        this.method = request.getMethod().toString();
        this.status = response.getStatus().code();
        this.responseBody = response.content().copy().array();
        this.responseHeaders = response.headers().entries();

        StringBuilder sb = new StringBuilder();
        sb.append(this.method).append(" ").append(this.uri).append("\n");
        for (Map.Entry<String, String> item : request.headers()) {
            sb.append(item.getKey()).append(": ").append(item.getValue()).append("\n");
        }
        this.request = sb.toString();
    }

    public HttpObject replace(Response response) {
        if (null != response) {
            HttpResponse result = Util.getResponse(response);
            this.responseHeaders = result.headers().entries();
            this.status = response.getStatus();
            this.name = response.getName();
            this.matchingUri = response.getUri();
            this.responseBody = response.getBody();
            return result;
        }
        return replace();
    }

    public void update(Response response) {
        if (null != response) {
            this.responseHeaders = Util.toMap(response.getHeaders());
            this.status = response.getStatus();
            this.name = response.getName();
            this.matchingUri = response.getUri();
            this.responseBody = response.getBody();
        }
    }

    public Date getTimestamp() {
        return this.timestamp;
    }

    public String getUri() {
        return this.uri;
    }

    public String getMethod() {
        return this.method;
    }

    public byte[] getResponseBody() {
        return this.responseBody;
    }

    public HttpResponse replace() {
        return response;
    }

    public int getStatus() {
        return this.status;
    }

    public String getMatchingUri() {
        return matchingUri;
    }

    public String getName() {
        return this.name;
    }

    public String getRequest() {
        return this.request;
    }

    public List<Map.Entry<String, String>> getResponseHeaders() {
        return this.responseHeaders;
    }

    @Override
    public String toString() {
        return this.method + " " + this.getUri() + (null != this.name ? " [responded '" + this.name + "', matching '" + this.matchingUri + "']" : "");
    }
}
