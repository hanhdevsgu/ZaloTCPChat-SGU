# Đồ Án Lập Trình Mạng (SGU) - Bài 11 & Bài 12

Dự án hoàn thiện chuẩn theo yêu cầu slide bài giảng môn **Lập Trình Mạng** (Đại học Sài Gòn - SGU, GV: Trần Minh Nhật).

---

## 📌 Nội Dung Dự Án

### 1. BÀI 11: TCP CHAT NHIỀU CLIENT (Slide 11)
- **TCP Server** (`chat.server.TcpChatServer` - Port `5000`):
  - Quản lý đa luồng kết nối client đồng thời với `ExecutorService`.
  - Gán Client ID duy nhất cho mỗi phiên kết nối.
  - Broadcast tin nhắn đến tất cả thành viên trong phòng.
  - Xử lý khi Client ngắt kết nối không làm sập server.
  - **Bài tập mở rộng (Slide 13):**
    - Đăng ký / Đăng nhập tài khoản (`REGISTER`, `LOGIN`).
    - Chat riêng giữa 2 user (`PRIVATE|user|message`).
    - Chat nhóm (`CREATE_GROUP`, `GROUP`).
    - Danh sách người dùng online (`LIST_USERS`).
- **TCP Client** (`chat.client.TcpChatClient`):
  - Chạy giao diện Console độc lập, kết nối tới TCP Server port 5000 để chat.

---

### 2. BÀI 12: HTTP VÀ HTTP SERVER (Slide 12)
- **HTTP Server** (`chat.server.HttpChatServer` - Port `8080`):
  - Tự hiện thực HTTP Server trên nền `ServerSocket` TCP.
  - **Routing theo Method + URL:**
    - `GET /` -> Phục vụ toàn bộ mã nguồn `index.html`
    - `GET /style.css` -> Phục vụ CSS
    - `GET /app.js` -> Phục vụ JavaScript
    - `GET /api/time` -> Trả về JSON thời gian thực (Slide 17)
    - `GET /api/students` -> Trả về danh sách sinh viên JSON (Slide 16)
    - `POST /api/message` -> Nhận JSON body, trả `201 Created` (Slide 18, 19)
    - Route không tồn tại -> Trả mã lỗi `404 Not Found`
- **Java HTTP Client** (`chat.client.HttpClientDemoAll` & `HttpClientErrorExample`):
  - **Lấy toàn bộ mã nguồn về Client:** Gửi `GET /` tải toàn bộ nội dung HTML từ Server in ra màn hình.
  - **Gửi GET đọc JSON:** Đọc `/api/time` và `/api/students` (Slide 17).
  - **Gửi POST JSON:** Gửi `/api/message` và nhận Status `201 Created` (Slide 18).
  - **Xử lý lỗi theo Slide 20:**
    - Phân biệt **HTTP Error (4xx, 5xx)**: Server có trả response báo lỗi.
    - Phân biệt **Lỗi kết nối / Timeout**: Bắt `ConnectException` / `HttpTimeoutException` khi không có response.

---

## 🚀 Hướng Dẫn Chạy

### Cách 1: Sử dụng các file `.bat` (Click đúp chuột trên Windows)

1. **Khởi động HTTP Server (Bài 12):** Click đúp `run_http_server.bat`
2. **Chạy HTTP Client lấy toàn bộ mã nguồn và test Bài 12:** Click đúp `run_http_client.bat`
3. **Khởi động TCP Chat Server (Bài 11):** Click đúp `run_tcp_server.bat`
4. **Chạy TCP Chat Client (Bài 11):** Click đúp `run_tcp_client.bat` (có thể mở nhiều cửa sổ để chat cùng lúc)

---

### Cách 2: Sử dụng dòng lệnh (Terminal / CMD)

Di chuyển vào thư mục `server`:
```bash
cd server
```

#### 1. Biên dịch:
```bash
mvn compile
```

#### 2. Chạy HTTP Server (Port 8080):
```bash
java -Dfile.encoding=UTF-8 -cp target/classes chat.server.HttpChatServer
```

#### 3. Chạy HTTP Client (Lấy mã nguồn và kiểm thử Bài 12):
Mở một cửa sổ Terminal mới:
```bash
java -Dfile.encoding=UTF-8 -cp target/classes chat.client.HttpClientDemoAll
```
*(Nếu server chạy trên máy khác trong mạng LAN/Wi-Fi: `java -cp target/classes chat.client.HttpClientDemoAll http://172.20.10.x:8080`)*

#### 4. Chạy TCP Server và TCP Client (Bài 11):
- Server:
  ```bash
  java -Dfile.encoding=UTF-8 -cp target/classes chat.server.TcpChatServer
  ```
- Client (mở ở terminal khác):
  ```bash
  java -Dfile.encoding=UTF-8 -cp target/classes chat.client.TcpChatClient
  ```
