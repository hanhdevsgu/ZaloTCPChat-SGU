package chat.server;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

public class HttpChatServer {
    public static final int PORT = 8080;
    private final ExecutorService pool = Executors.newCachedThreadPool();
    private final AccountStore accounts = new AccountStore(Path.of("accounts.db"));
    private final Set<String> online = ConcurrentHashMap.newKeySet();
    private final MessageStore messages = new MessageStore();
    private final GroupStore groups = new GroupStore();
    private final Path webRoot = findWebRoot();

    public static void main(String[] args) throws Exception { new HttpChatServer().start(); }

    private static Path findWebRoot() {
        Path[] candidates = {
            Path.of("web"),
            Path.of("..", "web"),
            Path.of("..", "..", "web")
        };
        for (Path candidate : candidates) {
            Path p = candidate.toAbsolutePath().normalize();
            if (Files.isDirectory(p) && Files.exists(p.resolve("index.html"))) {
                System.out.println("Web root: " + p);
                return p;
            }
        }
        Path fallback = Path.of("web").toAbsolutePath().normalize();
        System.out.println("WARNING: khong tim thay thu muc web. Dang dung: " + fallback);
        return fallback;
    }

    public void start() throws IOException {
        try (ServerSocket server = new ServerSocket(PORT)) {
            System.out.println("==============================================");
            System.out.println(" ZALO TCP + HTTP SERVER");
            System.out.println(" ServerSocket TCP port: " + PORT);
            System.out.println(" Web: http://localhost:" + PORT);
            System.out.println("==============================================");
            while (true) {
                Socket socket = server.accept();
                socket.setKeepAlive(true);
                pool.submit(() -> handle(socket));
            }
        }
    }

    private void handle(Socket socket) {
        try (socket) {
            socket.setSoTimeout(10000);
            HttpRequest req = HttpRequest.parse(socket.getInputStream());
            if (req == null) return;
            System.out.println("TCP/HTTP: " + req.method + " " + req.target + " from " + socket.getRemoteSocketAddress());
            route(req).write(socket.getOutputStream());
        } catch (Exception e) {
            System.err.println("Request error: " + e.getMessage());
        }
    }

    private HttpResponse route(HttpRequest r) throws IOException {
        if (r.method.equals("GET")) {
            if (r.path.equals("/")) return file("index.html", "text/html; charset=UTF-8");
            if (r.path.equals("/style.css")) return file("style.css", "text/css; charset=UTF-8");
            if (r.path.equals("/app.js")) return file("app.js", "application/javascript; charset=UTF-8");
            if (r.path.equals("/api/time")) {
                String now = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
                return json(200, "{\"time\":\"" + now + "\"}");
            }
            if (r.path.equals("/api/students")) {
                return json(200, "[{\"id\":\"SV001\",\"name\":\"Nguyen Van A\"},{\"id\":\"SV002\",\"name\":\"Tran Thi B\"}]");
            }
            if (r.path.equals("/api/users")) return json(200, usersJson());
            if (r.path.equals("/api/messages")) return json(200, messages.history(r.query.getOrDefault("user", ""), r.query.getOrDefault("with", "")));
            if (r.path.equals("/api/groups")) return json(200, groups.list(r.query.getOrDefault("user", "")));
            if (r.path.equals("/api/groups/messages")) return json(200, groups.history(r.query.getOrDefault("group", ""), r.query.getOrDefault("user", "")));
        }

        if (r.method.equals("POST")) {
            if (r.path.equals("/api/message")) {
                return json(201, "{\"message\":\"Đã nhận dữ liệu\"}");
            }
            if (r.path.equals("/api/register")) return register(r.body);
            if (r.path.equals("/api/login")) return login(r.body);
            if (r.path.equals("/api/logout")) return logout(r.body);
            if (r.path.equals("/api/messages")) return privateMessage(r.body);
            if (r.path.equals("/api/groups")) return createGroup(r.body);
            if (r.path.equals("/api/groups/members")) return addMember(r.body);
            if (r.path.equals("/api/groups/messages")) return groupMessage(r.body);
        }
        return json(404, "{\"status\":404,\"message\":\"404 Not Found - Khong tim thay API\"}");
    }

    private HttpResponse register(String body) {
        boolean ok = accounts.register(JsonUtil.getString(body, "username"), JsonUtil.getString(body, "password"));
        return ok ? json(200, "{\"success\":true,\"message\":\"Dang ky thanh cong\"}") :
                json(400, "{\"success\":false,\"message\":\"Username da ton tai hoac du lieu khong hop le\"}");
    }

    private HttpResponse login(String body) {
        String user = JsonUtil.getString(body, "username");
        String pass = JsonUtil.getString(body, "password");
        if (!accounts.verify(user, pass)) return json(401, "{\"success\":false,\"message\":\"Sai username hoac password\"}");
        if (!online.add(user)) return json(409, "{\"success\":false,\"message\":\"Tai khoan dang online\"}");
        return json(200, "{\"success\":true,\"username\":\"" + JsonUtil.escape(user) + "\"}");
    }

    private HttpResponse logout(String body) {
        online.remove(JsonUtil.getString(body, "username"));
        return json(200, "{\"success\":true}");
    }

    private HttpResponse privateMessage(String body) {
        String from = JsonUtil.getString(body, "from"), to = JsonUtil.getString(body, "to"), text = JsonUtil.getString(body, "message");
        if (!online.contains(from)) return json(401, "{\"success\":false,\"message\":\"Chua dang nhap\"}");
        if (!accounts.allUsers().contains(to)) return json(404, "{\"success\":false,\"message\":\"Nguoi dung khong ton tai\"}");
        String msg = messages.add(from, to, text);
        return json(200, "{\"success\":true,\"message\":" + msg + "}");
    }

    private HttpResponse createGroup(String body) {
        String owner = JsonUtil.getString(body, "owner"), name = JsonUtil.getString(body, "name");
        if (!online.contains(owner)) return json(401, "{\"success\":false,\"message\":\"Chua dang nhap\"}");
        if (name.isBlank()) return json(400, "{\"success\":false,\"message\":\"Ten nhom rong\"}");
        GroupStore.Group g = groups.create(name, owner);
        return json(200, "{\"success\":true,\"group\":" + g.json() + "}");
    }

    private HttpResponse addMember(String body) {
        boolean ok = groups.addMember(JsonUtil.getString(body, "groupId"), JsonUtil.getString(body, "owner"), JsonUtil.getString(body, "member"));
        return ok ? json(200, "{\"success\":true}") : json(400, "{\"success\":false,\"message\":\"Khong the them thanh vien\"}");
    }

    private HttpResponse groupMessage(String body) {
        String id = JsonUtil.getString(body, "groupId"), from = JsonUtil.getString(body, "from"), text = JsonUtil.getString(body, "message");
        String msg = groups.addMessage(id, from, text);
        return msg == null ? json(400, "{\"success\":false,\"message\":\"Khong co quyen chat trong nhom\"}") : json(200, "{\"success\":true,\"message\":" + msg + "}");
    }

    private String usersJson() {
        List<String> list = new ArrayList<>();
        for (String u : accounts.allUsers()) list.add("{\"username\":\"" + JsonUtil.escape(u) + "\",\"online\":" + online.contains(u) + "}");
        return "[" + String.join(",", list) + "]";
    }

    private HttpResponse file(String name, String type) throws IOException {
        Path p = webRoot.resolve(name).normalize();
        if (!p.startsWith(webRoot) || !Files.exists(p)) return HttpResponse.text(404, "Not Found");
        return HttpResponse.bytes(200, type, Files.readAllBytes(p));
    }

    private static HttpResponse json(int status, String body) { return HttpResponse.json(status, body); }
}
