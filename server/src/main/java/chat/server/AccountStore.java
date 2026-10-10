package chat.server;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AccountStore {
    private final Path file;
    private final Map<String, String> users = new ConcurrentHashMap<>();

    public AccountStore(Path file) { this.file = file; load(); }

    private void load() {
        try {
            if (!Files.exists(file)) return;
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                if (line.isBlank() || !line.contains("|")) continue;
                String[] p = line.split("\\|", 2);
                users.put(p[0], p[1]);
            }
        } catch (IOException e) { throw new RuntimeException(e); }
    }

    public synchronized boolean register(String username, String password) {
        if (username == null || password == null ||
                !username.matches("[A-Za-z0-9_]{3,24}") ||
                password.length() < 4 || users.containsKey(username)) return false;
        users.put(username, sha256(password));
        save();
        return true;
    }

    public boolean verify(String username, String password) {
        return users.containsKey(username) && users.get(username).equals(sha256(password));
    }

    public Set<String> allUsers() { return new TreeSet<>(users.keySet()); }

    private void save() {
        try {
            StringBuilder sb = new StringBuilder();
            for (var e : new TreeMap<>(users).entrySet())
                sb.append(e.getKey()).append('|').append(e.getValue()).append('\n');
            Files.writeString(file, sb.toString(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) { throw new RuntimeException(e); }
    }

    private static String sha256(String s) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256")
                    .digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
