package chat.server;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class HttpRequest {
    public String method = "";
    public String target = "";
    public String path = "";
    public String body = "";
    public final Map<String, String> headers = new HashMap<>();
    public final Map<String, String> query = new HashMap<>();

    public static HttpRequest parse(InputStream input) throws IOException {
        ByteArrayOutputStream headerBuffer = new ByteArrayOutputStream();
        int b;
        int state = 0; // 1: \r, 2: \r\n, 3: \r\n\r, 4: \r\n\r\n
        while ((b = input.read()) != -1) {
            headerBuffer.write(b);
            if (b == '\r') {
                if (state == 0 || state == 2) state++; else state = 1;
            } else if (b == '\n') {
                if (state == 1) state = 2;
                else if (state == 3) {
                    state = 4;
                    break;
                } else state = 0;
            } else {
                state = 0;
            }
        }
        if (headerBuffer.size() == 0) return null;

        byte[] headerBytes = headerBuffer.toByteArray();
        String headerStr = new String(headerBytes, StandardCharsets.UTF_8);
        String[] lines = headerStr.split("\r\n|\n");
        if (lines.length == 0 || lines[0].isBlank()) return null;

        String[] parts = lines[0].split(" ");
        if (parts.length < 2) throw new IOException("HTTP request line khong hop le");

        HttpRequest r = new HttpRequest();
        r.method = parts[0].toUpperCase(Locale.ROOT);
        r.target = parts[1];

        int q = r.target.indexOf('?');
        r.path = q >= 0 ? r.target.substring(0, q) : r.target;
        if (q >= 0) parseQuery(r.target.substring(q + 1), r.query);

        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            if (line.isEmpty()) break;
            int colon = line.indexOf(':');
            if (colon <= 0) continue;
            r.headers.put(line.substring(0, colon).trim().toLowerCase(Locale.ROOT),
                    line.substring(colon + 1).trim());
        }

        int contentLength = 0;
        try {
            contentLength = Integer.parseInt(r.headers.getOrDefault("content-length", "0"));
        } catch (NumberFormatException ignored) {}

        if (contentLength > 0) {
            byte[] bodyBytes = input.readNBytes(contentLength);
            r.body = new String(bodyBytes, StandardCharsets.UTF_8);
        }
        return r;
    }

    private static void parseQuery(String raw, Map<String, String> map) {
        if (raw.isBlank()) return;
        for (String pair : raw.split("&")) {
            String[] kv = pair.split("=", 2);
            String key = decode(kv[0]);
            String value = kv.length > 1 ? decode(kv[1]) : "";
            map.put(key, value);
        }
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
