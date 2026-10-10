package chat.server;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatGroup {
    public final String id;
    public final String name;
    public final String owner;
    public final Set<String> members = ConcurrentHashMap.newKeySet();
    public final List<String> history = new CopyOnWriteArrayList<>();

    public ChatGroup(String id, String name, String owner) {
        this.id = id;
        this.name = name;
        this.owner = owner;
        members.add(owner);
    }
}
