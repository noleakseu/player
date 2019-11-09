package trudy;

import java.util.List;

public final class Response {
    private final String uri; // matching
    private final String name;
    private final int status;
    private final List<Header> headers;
    private final byte[] body;

    public Response(String name, String uri, int status, List<Header> headers, byte[] body) {
        this.name = name;
        this.uri = uri;
        this.status = status;
        this.headers = headers;
        this.body = body;
    }

    public String getName() {
        return name;
    }

    public String getUri() {
        return uri;
    }

    public int getStatus() {
        return status;
    }

    public byte[] getBody() {
        return body;
    }

    public List<Header> getHeaders() {
        return headers;
    }
}
