package chat.server;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ClientHandler implements Runnable {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final TcpChatServer server;
    private final Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private volatile boolean running = true;
    private String username;

    public ClientHandler(TcpChatServer server, Socket socket) {
        this.server = server;
        this.socket = socket;
    }

    public String getUsername() { return username; }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

            send(Protocol.line("CONNECTED", "Ket noi TCP thanh cong"));

            String line;
            while (running && (line = in.readLine()) != null) {
                server.handle(this, line);
            }
        } catch (IOException ignored) {
        } finally {
            server.disconnect(this);
        }
    }

    public synchronized void send(String line) {
        if (out != null) out.println(line);
    }

    public void close() {
        running = false;
        try { socket.close(); } catch (IOException ignored) {}
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String now() {
        return LocalDateTime.now().format(FMT);
    }
}
