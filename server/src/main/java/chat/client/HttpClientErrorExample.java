package chat.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpTimeoutException;
import java.io.IOException;
import java.net.ConnectException;
import java.time.Duration;

/**
 * Slide 20 - BÀI 12: HTTP CLIENT XỬ LÝ LỖI (JAVA)
 * Phân biệt HTTP Error (4xx, 5xx), lỗi kết nối và timeout.
 */
public class HttpClientErrorExample {
    public static void main(String[] args) {
        String url = (args.length > 0) ? args[0] : "http://localhost:8080/api/time";
        System.out.println("==================================================");
        System.out.println(" TEST HTTP CLIENT XỬ LÝ LỖI (THEO SLIDE 20)");
        System.out.println(" URL mục tiêu: " + url);
        System.out.println("==================================================");

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5)) // timeout kết nối
                .build();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .timeout(Duration.ofSeconds(5)) // timeout đọc response
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body();

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                System.out.println("--> THÀNH CÔNG (2xx):");
                System.out.println("    Status code: " + response.statusCode());
                System.out.println("    Body: " + body);
            } else {
                System.out.println("--> HTTP ERROR (4xx / 5xx) - Server có trả response:");
                System.out.println("    Status: " + response.statusCode());
                System.out.println("    Body lỗi: " + body);
            }
        } catch (HttpConnectTimeoutException e) {
            System.err.println("--> LỖI TIMEOUT KẾT NỐI: Request bị timeout: " + e.getMessage());
        } catch (HttpTimeoutException e) {
            System.err.println("--> LỖI TIMEOUT ĐỌC RESPONSE: " + e.getMessage());
        } catch (ConnectException e) {
            System.err.println("--> LỖI KẾT NỐI (Không có response - Server chưa bật hoặc sai port): " + e.getMessage());
        } catch (IOException e) {
            System.err.println("--> LỖI I/O / KẾT NỐI: " + e.getMessage());
        } catch (InterruptedException e) {
            System.err.println("--> Bị gián đoạn: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}
