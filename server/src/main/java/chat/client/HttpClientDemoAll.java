package chat.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.io.IOException;
import java.net.ConnectException;
import java.time.Duration;

/**
 * BÀI 12: TỔNG HỢP TOÀN BỘ CÁC YÊU CẦU CỦA HTTP CLIENT
 * 1. Tải toàn bộ mã nguồn HTML/CSS từ Server về Client
 * 2. Gửi GET /api/time đọc JSON (Slide 17)
 * 3. Gửi POST /api/message kèm Body JSON (Slide 18, 19)
 * 4. Gọi API mở rộng /api/students trả danh sách sinh viên JSON (Slide 16)
 * 5. Xử lý HTTP Error 404 Not Found (Slide 20)
 * 6. Xử lý Lỗi kết nối / Timeout (Slide 20)
 */
public class HttpClientDemoAll {
    private static final String DEFAULT_HOST = "http://localhost:8080";

    public static void main(String[] args) {
        String baseUrl = (args.length > 0) ? args[0] : DEFAULT_HOST;
        System.out.println("======================================================================");
        System.out.println("       CHƯƠNG TRÌNH JAVA HTTP CLIENT - TỔNG HỢP BÀI 12");
        System.out.println(" Kết nối tới Server: " + baseUrl);
        System.out.println("======================================================================\n");

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        // 1. LẤY TOÀN BỘ SOURCE CODE HTML TỪ SERVER (GET /)
        testGetFullHtmlSource(client, baseUrl);

        // 2. LẤY MÃ NGUỒN CSS (GET /style.css)
        testGetCss(client, baseUrl);

        // 3. SLIDE 17: GỬI GET /api/time VÀ ĐỌC JSON RESPONSE
        testGetTimeApi(client, baseUrl);

        // 4. BÀI TẬP MỞ RỘNG SLIDE 16: LẤY DANH SÁCH SINH VIÊN DẠNG JSON
        testGetStudentsApi(client, baseUrl);

        // 5. SLIDE 18 & 19: GỬI POST JSON /api/message
        testPostJsonMessage(client, baseUrl);

        // 6. SLIDE 20: XỬ LÝ HTTP ERROR (MÃ 404 - CÓ RESPONSE TỪ SERVER)
        testHttpError404(client, baseUrl);

        // 7. SLIDE 20: XỬ LÝ LỖI KẾT NỐI (KHÔNG CÓ RESPONSE TỪ SERVER)
        testConnectionError(client);

        System.out.println("======================================================================");
        System.out.println(" HOÀN THÀNH KIỂM THỬ TOÀN BỘ CÁC YÊU CẦU CỦA BÀI 12!");
        System.out.println("======================================================================");
    }

    /** 1. Tải toàn bộ mã nguồn file HTML từ Server về */
    private static void testGetFullHtmlSource(HttpClient client, String baseUrl) {
        System.out.println("----------------------------------------------------------------------");
        System.out.println("[TEST 1] TẢI TOÀN BỘ MÃ NGUỒN HTML TỪ SERVER: GET " + baseUrl + "/");
        System.out.println("----------------------------------------------------------------------");
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/"))
                    .GET()
                    .timeout(Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            System.out.println("> Status: " + res.statusCode());
            System.out.println("> Content-Type: " + res.headers().firstValue("Content-Type").orElse("N/A"));
            System.out.println("> Đã nhận toàn bộ nội dung HTML (" + res.body().length() + " bytes):\n");
            System.out.println(res.body());
            System.out.println();
        } catch (Exception e) {
            System.err.println("Lỗi Test 1: " + e.getMessage());
        }
    }

    /** 2. Tải CSS từ Server về */
    private static void testGetCss(HttpClient client, String baseUrl) {
        System.out.println("----------------------------------------------------------------------");
        System.out.println("[TEST 2] TẢI MÃ NGUỒN CSS TỪ SERVER: GET " + baseUrl + "/style.css");
        System.out.println("----------------------------------------------------------------------");
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/style.css"))
                    .GET()
                    .timeout(Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            System.out.println("> Status: " + res.statusCode());
            System.out.println("> Content-Type: " + res.headers().firstValue("Content-Type").orElse("N/A"));
            System.out.println("> Đã nhận nội dung CSS (" + res.body().length() + " bytes). Preview 200 ký tự đầu:");
            String preview = res.body().length() > 200 ? res.body().substring(0, 200) + "..." : res.body();
            System.out.println(preview);
            System.out.println();
        } catch (Exception e) {
            System.err.println("Lỗi Test 2: " + e.getMessage());
        }
    }

    /** 3. Slide 17: Gọi GET /api/time */
    private static void testGetTimeApi(HttpClient client, String baseUrl) {
        System.out.println("----------------------------------------------------------------------");
        System.out.println("[TEST 3 - SLIDE 17] GỬI GET VÀ ĐỌC JSON: GET " + baseUrl + "/api/time");
        System.out.println("----------------------------------------------------------------------");
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/time"))
                    .GET()
                    .timeout(Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            System.out.println("> Status: " + res.statusCode() + " OK");
            System.out.println("> Content-Type: " + res.headers().firstValue("Content-Type").orElse("N/A"));
            System.out.println("> Body: " + res.body());
            System.out.println();
        } catch (Exception e) {
            System.err.println("Lỗi Test 3: " + e.getMessage());
        }
    }

    /** 4. Bài tập mở rộng Slide 16: Danh sách sinh viên JSON */
    private static void testGetStudentsApi(HttpClient client, String baseUrl) {
        System.out.println("----------------------------------------------------------------------");
        System.out.println("[TEST 4 - MỞ RỘNG SLIDE 16] LẤY DANH SÁCH SINH VIÊN JSON: GET " + baseUrl + "/api/students");
        System.out.println("----------------------------------------------------------------------");
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/students"))
                    .GET()
                    .timeout(Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            System.out.println("> Status: " + res.statusCode());
            System.out.println("> Body JSON: " + res.body());
            System.out.println();
        } catch (Exception e) {
            System.err.println("Lỗi Test 4: " + e.getMessage());
        }
    }

    /** 5. Slide 18 & 19: Gửi POST JSON /api/message */
    private static void testPostJsonMessage(HttpClient client, String baseUrl) {
        System.out.println("----------------------------------------------------------------------");
        System.out.println("[TEST 5 - SLIDE 18, 19] GỬI POST JSON: POST " + baseUrl + "/api/message");
        System.out.println("----------------------------------------------------------------------");
        try {
            String jsonBody = "{\"text\":\"Xin chào Server từ Java Client!\"}";
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/message"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .timeout(Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            System.out.println("> Status: " + res.statusCode() + " (201 Created)");
            System.out.println("> Request Body gửi đi: " + jsonBody);
            System.out.println("> Response Body nhận về: " + res.body());
            System.out.println();
        } catch (Exception e) {
            System.err.println("Lỗi Test 5: " + e.getMessage());
        }
    }

    /** 6. Slide 20: Phân biệt HTTP Error (4xx - Server trả response báo lỗi) */
    private static void testHttpError404(HttpClient client, String baseUrl) {
        System.out.println("----------------------------------------------------------------------");
        System.out.println("[TEST 6 - SLIDE 20] THỬ NGHIỆM HTTP ERROR 404: GET " + baseUrl + "/api/khong-ton-tai");
        System.out.println("----------------------------------------------------------------------");
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/khong-ton-tai"))
                    .GET()
                    .timeout(Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() >= 200 && res.statusCode() < 300) {
                System.out.println("> Thành công: " + res.body());
            } else {
                System.out.println("> Nhận diện HTTP ERROR: " + res.statusCode() + " - Có nhận Response từ Server");
                System.out.println("> Body lỗi: " + res.body());
            }
            System.out.println();
        } catch (Exception e) {
            System.err.println("Lỗi Test 6: " + e.getMessage());
        }
    }

    /** 7. Slide 20: Phân biệt Lỗi kết nối / Timeout (Không có response từ server) */
    private static void testConnectionError(HttpClient client) {
        System.out.println("----------------------------------------------------------------------");
        System.out.println("[TEST 7 - SLIDE 20] THỬ NGHIỆM LỖI KẾT NỐI (GỌI ĐẾN PORT 9999 KHÔNG HOẠT ĐỘNG)");
        System.out.println("----------------------------------------------------------------------");
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:9999/api/time"))
                    .GET()
                    .timeout(Duration.ofSeconds(2))
                    .build();

            client.send(req, HttpResponse.BodyHandlers.ofString());
        } catch (ConnectException e) {
            System.out.println("> ĐÚNG THEO SLIDE 20: Bắt được ConnectException (Lỗi kết nối)!");
            System.out.println("  Thông báo lỗi: " + e.getMessage());
            System.out.println("  ==> Kết luận: Server không chạy -> KHÔNG CÓ RESPONSE NÀO TỪ SERVER.");
            System.out.println();
        } catch (HttpTimeoutException e) {
            System.out.println("> Bắt được TimeoutException: " + e.getMessage());
            System.out.println();
        } catch (Exception e) {
            System.out.println("> Bắt được ngoại lệ mạng: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            System.out.println();
        }
    }
}
