package chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TcpChatServer {
    public static final int PORT = 5000;

    private final AccountStore accounts = new AccountStore(Path.of("accounts.db"));
    private final Map<String, ClientHandler> online = new ConcurrentHashMap<>();
    private final Map<String, ChatGroup> groups = new ConcurrentHashMap<>();
    private final Map<String, List<String>> privateHistory = new ConcurrentHashMap<>();
    private final AtomicInteger groupSeq = new AtomicInteger(1000);
    private final ExecutorService pool = Executors.newCachedThreadPool();

    public static void main(String[] args) throws Exception {
        new TcpChatServer().start();
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("==========================================");
            System.out.println(" TCP CHAT SERVER - PORT " + PORT);
            System.out.println(" Cho client ket noi den IP cua may server.");
            System.out.println("==========================================");

            while (true) {
                Socket socket = serverSocket.accept();
                socket.setKeepAlive(true);
                System.out.println("Client TCP ket noi: " + socket.getRemoteSocketAddress());
                pool.submit(new ClientHandler(this, socket));
            }
        }
    }

    public void handle(ClientHandler c, String line) {
        String[] p = line.split("\\|", -1);
        String cmd = p[0];

        switch (cmd) {
            case "REGISTER" -> register(c, p);
            case "LOGIN" -> login(c, p);
            case "LIST_USERS" -> sendUserList(c);
            case "HISTORY_PRIVATE" -> historyPrivate(c, p);
            case "PRIVATE" -> privateMessage(c, p);
            case "CREATE_GROUP" -> createGroup(c, p);
            case "ADD_MEMBER" -> addMember(c, p);
            case "LIST_GROUPS" -> sendGroupList(c);
            case "HISTORY_GROUP" -> historyGroup(c, p);
            case "GROUP" -> groupMessage(c, p);
            case "LOGOUT" -> c.close();
            default -> c.send(Protocol.line("ERROR", "Lenh khong hop le: " + cmd));
        }
    }

    private void register(ClientHandler c, String[] p) {
        if (p.length < 3) return;
        String u = Protocol.dec(p[1]).trim();
        String pw = Protocol.dec(p[2]);

        if (accounts.register(u, pw))
            c.send(Protocol.line("REGISTER_OK", "Dang ky thanh cong. Hay dang nhap."));
        else
            c.send(Protocol.line("REGISTER_FAIL", "Username da ton tai hoac khong hop le (3-24 ky tu, mat khau >= 4)."));
    }

    private void login(ClientHandler c, String[] p) {
        if (p.length < 3) return;
        String u = Protocol.dec(p[1]).trim();
        String pw = Protocol.dec(p[2]);

        if (!accounts.verify(u, pw)) {
            c.send(Protocol.line("LOGIN_FAIL", "Sai username hoac password."));
            return;
        }
        if (online.containsKey(u)) {
            c.send(Protocol.line("LOGIN_FAIL", "Tai khoan dang dang nhap o noi khac."));
            return;
        }

        c.setUsername(u);
        online.put(u, c);
        c.send(Protocol.line("LOGIN_OK", u));
        broadcastUserList();
        sendGroupList(c);
        System.out.println(u + " dang nhap.");
    }

    private void sendUserList(ClientHandler c) {
        c.send(Protocol.line("USER_LIST", buildUserJson()));
    }

    private void broadcastUserList() {
        String json = buildUserJson();
        online.values().forEach(c -> c.send(Protocol.line("USER_LIST", json)));
    }

    private String buildUserJson() {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (String u : new TreeSet<>(online.keySet())) {
            if (!first) sb.append(',');
            first = false;
            sb.append("{\"username\":\"").append(json(u)).append("\",\"online\":true}");
        }
        return sb.append(']').toString();
    }

    private void historyPrivate(ClientHandler c, String[] p) {
        if (!logged(c) || p.length < 2) return;
        String other = Protocol.dec(p[1]);
        String key = privateKey(c.getUsername(), other);
        List<String> list = privateHistory.getOrDefault(key, List.of());
        c.send(Protocol.line("PRIVATE_HISTORY", toJsonArray(list)));
    }

    private void privateMessage(ClientHandler c, String[] p) {
        if (!logged(c) || p.length < 3) return;
        String to = Protocol.dec(p[1]);
        String message = Protocol.dec(p[2]);
        if (message.isBlank()) return;

        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String record = "{\"from\":\"" + json(c.getUsername()) + "\",\"to\":\"" +
                json(to) + "\",\"text\":\"" + json(message) + "\",\"time\":\"" + time + "\"}";

        privateHistory.computeIfAbsent(privateKey(c.getUsername(), to), k -> new CopyOnWriteArrayList<>()).add(record);

        ClientHandler target = online.get(to);
        if (target != null) target.send(Protocol.line("PRIVATE_MESSAGE", record));
        c.send(Protocol.line("PRIVATE_MESSAGE", record));
    }

    private void createGroup(ClientHandler c, String[] p) {
        if (!logged(c) || p.length < 2) return;
        String name = Protocol.dec(p[1]).trim();
        if (name.isBlank()) return;

        String id = "G" + groupSeq.getAndIncrement();
        ChatGroup g = new ChatGroup(id, name, c.getUsername());
        groups.put(id, g);

        c.send(Protocol.line("GROUP_CREATED", id, name, c.getUsername()));
        sendGroupList(c);
    }

    private void addMember(ClientHandler c, String[] p) {
        if (!logged(c) || p.length < 3) return;
        String groupId = Protocol.dec(p[1]);
        String member = Protocol.dec(p[2]);

        ChatGroup g = groups.get(groupId);
        if (g == null) {
            c.send(Protocol.line("ERROR", "Khong tim thay nhom."));
            return;
        }
        if (!g.owner.equals(c.getUsername())) {
            c.send(Protocol.line("ERROR", "Chi chu nhom moi duoc them thanh vien."));
            return;
        }
        if (!accounts.verify(member, "__dummy__") && !accountsExist(member)) {
            c.send(Protocol.line("ERROR", "Username khong ton tai."));
            return;
        }

        g.members.add(member);
        notifyGroupMembers(g);
    }

    // Separate existence check avoids needing to expose passwords from AccountStore.
    private boolean accountsExist(String username) {
        try {
            var f = java.nio.file.Files.readAllLines(Path.of("accounts.db"));
            for (String line : f) if (line.startsWith(username + "|")) return true;
        } catch (Exception ignored) {}
        return false;
    }

    private void sendGroupList(ClientHandler c) {
        if (!logged(c)) return;
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (ChatGroup g : groups.values()) {
            if (!g.members.contains(c.getUsername())) continue;
            if (!first) sb.append(',');
            first = false;
            sb.append("{\"id\":\"").append(json(g.id)).append("\",\"name\":\"")
              .append(json(g.name)).append("\",\"owner\":\"").append(json(g.owner))
              .append("\",\"members\":").append(stringArray(g.members)).append("}");
        }
        sb.append(']');
        c.send(Protocol.line("GROUP_LIST", sb.toString()));
    }

    private void notifyGroupMembers(ChatGroup g) {
        for (String member : g.members) {
            ClientHandler c = online.get(member);
            if (c != null) sendGroupList(c);
        }
    }

    private void historyGroup(ClientHandler c, String[] p) {
        if (!logged(c) || p.length < 2) return;
        ChatGroup g = groups.get(Protocol.dec(p[1]));
        if (g == null || !g.members.contains(c.getUsername())) return;
        c.send(Protocol.line("GROUP_HISTORY", toJsonArray(g.history)));
    }

    private void groupMessage(ClientHandler c, String[] p) {
        if (!logged(c) || p.length < 3) return;
        String groupId = Protocol.dec(p[1]);
        String message = Protocol.dec(p[2]);
        ChatGroup g = groups.get(groupId);

        if (g == null || !g.members.contains(c.getUsername()) || message.isBlank()) return;

        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String record = "{\"groupId\":\"" + json(groupId) + "\",\"from\":\"" +
                json(c.getUsername()) + "\",\"text\":\"" + json(message) + "\",\"time\":\"" + time + "\"}";
        g.history.add(record);

        for (String member : g.members) {
            ClientHandler target = online.get(member);
            if (target != null) target.send(Protocol.line("GROUP_MESSAGE", record));
        }
    }

    private boolean logged(ClientHandler c) {
        if (c.getUsername() == null) {
            c.send(Protocol.line("ERROR", "Ban chua dang nhap."));
            return false;
        }
        return true;
    }

    public void disconnect(ClientHandler c) {
        String u = c.getUsername();
        if (u != null && online.remove(u, c)) {
            System.out.println(u + " ngat ket noi.");
            broadcastUserList();
        }
        c.close();
    }

    private String privateKey(String a, String b) {
        return a.compareTo(b) < 0 ? a + "::" + b : b + "::" + a;
    }

    private String toJsonArray(List<String> records) {
        return "[" + String.join(",", records) + "]";
    }

    private String stringArray(Collection<String> values) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (String s : new TreeSet<>(values)) {
            if (!first) sb.append(',');
            first = false;
            sb.append('"').append(json(s)).append('"');
        }
        return sb.append(']').toString();
    }

    private String json(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\r", "\\r").replace("\n", "\\n");
    }
}
