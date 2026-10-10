package chat.client;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * BÀI 11: TCP CHAT NHIỀU CLIENT (CONSOLE CLIENT)
 * Chạy riêng biệt với Server, kết nối tới Server TCP port 5000.
 * Hỗ trợ các lệnh:
 *   - LOGIN|username|password
 *   - REGISTER|username|password
 *   - LIST_USERS
 *   - PRIVATE|to_user|message
 *   - CREATE_GROUP|group_name
 *   - GROUP|group_id|message
 *   - LOGOUT
 */
public class TcpChatClient {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 5000;

    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : DEFAULT_HOST;
        int port = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_PORT;

        System.out.println("==================================================");
        System.out.println("     TCP CHAT CLIENT - BÀI 11 (NHIỀU CLIENT)");
        System.out.println(" Kết nối tới TCP Server: " + host + ":" + port);
        System.out.println("==================================================");

        try (Socket socket = new Socket(host, port)) {
            System.out.println("--> Đã kết nối TCP thành công tới Server!");
            System.out.println("CÁC LỆNH MẪU:");
            System.out.println("  1. Đăng ký tài khoản:  REGISTER|alice|123456");
            System.out.println("  2. Đăng nhập:         LOGIN|alice|123456");
            System.out.println("  3. Danh sách online:  LIST_USERS");
            System.out.println("  4. Chat riêng:        PRIVATE|bob|Xin chao bob!");
            System.out.println("  5. Tạo nhóm chat:     CREATE_GROUP|NhomLapTrinh");
            System.out.println("  6. Thoát:             LOGOUT");
            System.out.println("--------------------------------------------------");

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

            // Luồng nhận tin nhắn từ Server
            Thread receiveThread = new Thread(() -> {
                try {
                    String serverMsg;
                    while ((serverMsg = in.readLine()) != null) {
                        System.out.println("\n[SERVER]: " + serverMsg);
                        System.out.print("> ");
                    }
                } catch (IOException ignored) {
                } finally {
                    System.out.println("\n[THÔNG BÁO] Đã ngắt kết nối với Server.");
                }
            });
            receiveThread.setDaemon(true);
            receiveThread.start();

            // Luồng gửi dữ liệu từ bàn phím lên Server
            Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);
            System.out.print("> ");
            while (scanner.hasNextLine()) {
                String input = scanner.nextLine().trim();
                if (input.equalsIgnoreCase("exit") || input.equalsIgnoreCase("LOGOUT")) {
                    out.println("LOGOUT");
                    break;
                }
                if (!input.isEmpty()) {
                    out.println(input);
                }
                System.out.print("> ");
            }

        } catch (Exception e) {
            System.err.println("Lỗi kết nối TCP Server: " + e.getMessage());
        }
    }
}
