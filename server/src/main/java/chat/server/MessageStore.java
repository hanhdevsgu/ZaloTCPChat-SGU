package chat.server;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

public class MessageStore {
    private final Map<String, List<String>> privateMessages = new ConcurrentHashMap<>();

    public String add(String from, String to, String text) {
        String msg = messageJson(from, text);
        privateMessages.computeIfAbsent(key(from, to), k -> new CopyOnWriteArrayList<>()).add(msg);
        return msg;
    }

    public String history(String a, String b) {
        return "[" + String.join(",", privateMessages.getOrDefault(key(a, b), List.of())) + "]";
    }

    private String key(String a, String b) { return a.compareTo(b) < 0 ? a + "::" + b : b + "::" + a; }

    public static String messageJson(String from, String text) {
        return "{\"from\":\"" + JsonUtil.escape(from) + "\",\"text\":\"" +
                JsonUtil.escape(text) + "\",\"time\":\"" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "\"}";
    }
}
