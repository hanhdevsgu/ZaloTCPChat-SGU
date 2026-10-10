# Zalo TCP + HTTP Server + HTML/CSS/JS

## 1. Kiến trúc

```text
Chrome / Edge
HTML + CSS + JavaScript
        |
        | HTTP Request / Response
        |
        | HTTP chạy trên TCP
        v
Java ServerSocket :8080
        |
        +-- GET /                    -> index.html
        +-- GET /style.css           -> CSS
        +-- GET /app.js              -> JavaScript
        +-- POST /api/register       -> đăng ký
        +-- POST /api/login          -> đăng nhập
        +-- GET /api/users           -> danh sách người dùng/online
        +-- POST /api/messages       -> gửi chat riêng
        +-- GET /api/messages        -> lấy lịch sử chat riêng
        +-- POST /api/groups         -> tạo nhóm
        +-- POST /api/groups/members -> thêm thành viên
        +-- POST /api/groups/messages -> gửi chat nhóm
        +-- GET /api/groups/messages -> lấy lịch sử nhóm
```

Không dùng JavaFX, WebView, Spring Boot, Node.js hoặc WebSocket.

Java tự mở `ServerSocket`, nhận TCP connection, đọc HTTP request và tạo HTTP response.

## 2. Cấu trúc thư mục

```text
ZaloTcpChat/
├── server/
│   ├── pom.xml
│   └── src/main/java/chat/server/
│       ├── HttpChatServer.java
│       ├── HttpRequest.java
│       ├── HttpResponse.java
│       ├── JsonUtil.java
│       ├── AccountStore.java
│       ├── MessageStore.java
│       └── GroupStore.java
├── web/
│   ├── index.html
│   ├── style.css
│   └── app.js
└── README/
    └── HUONG_DAN.md
```

## 3. Chạy bằng Maven

Mở terminal tại thư mục `server`:

```bash
cd server
mvn compile exec:java
```

Sau đó mở trình duyệt:

```text
http://localhost:8080
```

### Quan trọng

Bạn có thể chạy Maven từ thư mục `server`. Server đã được sửa để tự tìm `web` ở cả:

```text
server/web
../web
../../web
```

Vì vậy trường hợp project có cấu trúc `ZaloTcpChat/server` và `ZaloTcpChat/web` vẫn chạy đúng.

Khi khởi động thành công, console sẽ hiện:

```text
Web root: .../ZaloTcpChat/web
==============================================
 ZALO TCP + HTTP SERVER
 ServerSocket TCP port: 8080
 Web: http://localhost:8080
==============================================
```

## 4. Nếu Maven không dùng được

Có thể biên dịch trực tiếp bằng JDK 17+:

```bash
cd server
mkdir out
javac -encoding UTF-8 -d out src/main/java/chat/server/*.java
java -cp out chat.server.HttpChatServer
```

## 5. Chạy nhiều máy trong LAN

Máy chạy Java Server là Server. Các máy khác mở:

```text
http://IP-MAY-SERVER:8080
```

Ví dụ:

```text
http://192.168.1.10:8080
```

Nếu Windows Firewall chặn kết nối, cho phép TCP port 8080.

## 6. HTTP và TCP trong đồ án

Browser gửi HTTP request bằng kết nối TCP đến `ServerSocket` của Java.

Java đọc request từ:

```java
socket.getInputStream()
```

và gửi HTTP response qua:

```java
socket.getOutputStream()
```

Do đó có thể giải thích trong báo cáo:

> HTTP là giao thức tầng ứng dụng, còn TCP là giao thức tầng vận chuyển. Trong project này, Java sử dụng `ServerSocket` để tạo TCP server và tự xử lý HTTP Request/Response trên kết nối TCP.

Chat sử dụng HTTP polling: JavaScript định kỳ gửi HTTP GET để lấy tin nhắn mới. Không sử dụng WebSocket.

## 7. Tài khoản

Tài khoản được lưu trong `accounts.db` tại thư mục mà server được chạy. Password được băm SHA-256.
