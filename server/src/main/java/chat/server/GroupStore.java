package chat.server;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class GroupStore {
    public static class Group {
        final String id, name, owner;
        final Set<String> members = ConcurrentHashMap.newKeySet();
        final List<String> messages = new CopyOnWriteArrayList<>();
        Group(String id, String name, String owner) {
            this.id = id; this.name = name; this.owner = owner; members.add(owner);
        }
        String json() {
            return "{\"id\":\"" + JsonUtil.escape(id) + "\",\"name\":\"" +
                    JsonUtil.escape(name) + "\",\"owner\":\"" + JsonUtil.escape(owner) + "\"}";
        }
    }

    private final AtomicInteger seq = new AtomicInteger(1000);
    private final Map<String, Group> groups = new ConcurrentHashMap<>();

    public Group create(String name, String owner) {
        Group g = new Group("G" + seq.getAndIncrement(), name, owner);
        groups.put(g.id, g); return g;
    }
    public boolean addMember(String id, String owner, String member) {
        Group g = groups.get(id);
        if (g == null || !g.owner.equals(owner) || member.isBlank()) return false;
        g.members.add(member); return true;
    }
    public String list(String user) {
        List<String> out = new ArrayList<>();
        for (Group g : groups.values()) if (g.members.contains(user)) out.add(g.json());
        return "[" + String.join(",", out) + "]";
    }
    public boolean isMember(String id, String user) {
        Group g = groups.get(id); return g != null && g.members.contains(user);
    }
    public String addMessage(String id, String from, String text) {
        Group g = groups.get(id);
        if (g == null || !g.members.contains(from)) return null;
        String msg = "{\"from\":\"" + JsonUtil.escape(from) + "\",\"text\":\"" +
                JsonUtil.escape(text) + "\",\"time\":\"" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "\"}";
        g.messages.add(msg); return msg;
    }
    public String history(String id, String user) {
        Group g = groups.get(id);
        if (g == null || !g.members.contains(user)) return "[]";
        return "[" + String.join(",", g.messages) + "]";
    }
}
