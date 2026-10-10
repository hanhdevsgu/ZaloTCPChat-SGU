package chat.server;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class HttpResponse {
    private final int status;
    private final String statusText;
    private final String contentType;
    private final byte[] body;

    public HttpResponse(int status, String statusText, String contentType, byte[] body) {
        this.status = status;
        this.statusText = statusText;
        this.contentType = contentType;
        this.body = body;
    }

    public static HttpResponse bytes(int status, String type, byte[] body) {
        String statusText = switch (status) {
            case 200 -> "OK";
            case 201 -> "Created";
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 404 -> "Not Found";
            case 415 -> "Unsupported Media Type";
            default -> "OK";
        };
        return new HttpResponse(status, statusText, type, body);
    }

    public static HttpResponse text(int status, String text) {
        return bytes(status, "text/plain; charset=UTF-8", text.getBytes(StandardCharsets.UTF_8));
    }

    public static HttpResponse json(int status, String json) {
        return bytes(status, "application/json; charset=UTF-8", json.getBytes(StandardCharsets.UTF_8));
    }

    public void write(OutputStream out) throws IOException {
        String headers = "HTTP/1.1 " + status + " " + statusText + "\r\n" +
                "Content-Type: " + contentType + "\r\n" +
                "Content-Length: " + body.length + "\r\n" +
                "Connection: close\r\n" +
                "Cache-Control: no-store\r\n" +
                "Access-Control-Allow-Origin: *\r\n" +
                "\r\n";
        out.write(headers.getBytes(StandardCharsets.UTF_8));
        out.write(body);
        out.flush();
    }
}
