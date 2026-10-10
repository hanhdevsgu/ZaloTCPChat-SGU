package chat.server;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class Protocol {
    private Protocol() {}

    public static String enc(String s) {
        return Base64.getEncoder().encodeToString(
                (s == null ? "" : s).getBytes(StandardCharsets.UTF_8));
    }

    public static String dec(String s) {
        try {
            return new String(Base64.getDecoder().decode(s), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "";
        }
    }

    public static String line(String command, String... values) {
        StringBuilder sb = new StringBuilder(command);
        for (String value : values) sb.append('|').append(enc(value));
        return sb.toString();
    }
}
