# 🏢 Hệ Thống Quản lý phòng học và đăng kí lịch sử dụng (Meeting Room Booking System)

Ứng dụng full-stack quản lý việc đặt và sử dụng phòng họp trong tổ chức, bao gồm cổng quản trị (Admin), cổng duyệt đặt phòng (Approver) và cổng nhân viên (Employee). Dự án được xây dựng trên nền tảng công nghệ hiện đại, đảm bảo khả năng mở rộng, bảo mật và trải nghiệm người dùng chất lượng cao.

---

## 🚀 Tính Năng Chính

### Xác Thực & Phân Quyền
- Đăng nhập bảo mật sử dụng **PASETO** (Platform-Agnostic Security Tokens).
- Hỗ trợ **Refresh Token** để duy trì phiên đăng nhập.
- Xác thực OTP qua email.
- Hệ thống phân quyền 3 vai trò:
  - **ADMIN** — Quản trị toàn hệ thống.
  - **APPROVER** — Duyệt/từ chối yêu cầu đặt phòng trong phạm vi phòng ban.
  - **EMPLOYEE** — Tạo yêu cầu đặt phòng và theo dõi lịch họp.

### Quản Lý Phòng Họp
- Tạo, sửa, xoá phòng họp (tên, sức chứa, vị trí, thiết bị).
- Phân phòng họp theo **phòng ban** (Department) hoặc dùng chung cho toàn tổ chức.
- Đánh dấu trạng thái khả dụng (Available / Unavailable).

### Quản Lý Đặt Phòng
- Tạo yêu cầu đặt phòng với tiêu đề, mô tả, số người tham dự và danh sách người tham gia.
- **Đặt phòng định kỳ** (Recurring): hỗ trợ lặp lại theo ngày, tuần, tháng.
- Vòng đời đặt phòng với **7 trạng thái**:

  ```
  PENDING → APPROVED → CHECKED_IN → COMPLETED
      ↓          ↓
   REJECTED   CANCELLED / NO_SHOW
  ```

  | Trạng thái | Mô tả |
  |------------|-------|
  | `PENDING` | Chờ duyệt |
  | `APPROVED` | Đã được duyệt |
  | `REJECTED` | Bị từ chối |
  | `CANCELLED` | Đã huỷ |
  | `CHECKED_IN` | Đã check-in (điểm danh) |
  | `COMPLETED` | Cuộc họp đã hoàn thành |
  | `NO_SHOW` | Không đến (tự động đánh dấu) |

### Tự Động Hoá (Scheduler)
- **Tự động giải phóng phòng (Auto-Release):** Nếu cuộc họp đã được duyệt nhưng không ai check-in sau **15 phút** kể từ giờ bắt đầu, hệ thống tự động chuyển trạng thái sang `NO_SHOW`.
- **Nhắc nhở trước cuộc họp (Reminder):** Gửi thông báo trong ứng dụng và email nhắc nhở **15 phút** trước giờ họp.

### Thông Báo & Email
- Hệ thống thông báo trong ứng dụng (In-app Notification).
- Gửi email tự động khi booking thay đổi trạng thái và nhắc nhở trước cuộc họp.
- Tích hợp SMTP (hỗ trợ cấu hình Mailtrap cho môi trường phát triển).

### Lịch Trực Quan
- Hiển thị lịch đặt phòng theo dạng **Calendar View**.
- Xem lịch theo ngày với danh sách booking chi tiết.

### Quản Lý Người Dùng & Phòng Ban
- Admin tạo và quản lý tài khoản nhân viên.
- Quản lý danh sách phòng ban (Department).
- Phân người dùng vào phòng ban.

### Xuất Dữ Liệu
- Hỗ trợ **xuất dữ liệu** (Export) cho mục đích báo cáo.

### Tài Liệu API
- API REST được tài liệu hoá đầy đủ với **Swagger UI** (SpringDoc OpenAPI).
- Cho phép test API trực tiếp trên trình duyệt.

---

## 🏗️ Kiến Trúc Hệ Thống

Dự án theo mô hình **Client-Server** với sự tách biệt rõ ràng giữa Frontend và Backend:

```
┌─────────────────────┐     REST API     ┌─────────────────────┐     JDBC     ┌──────────────┐
│   Vue 3 SPA (FE)    │ ◄──────────────► │  Spring Boot (API)  │ ◄──────────► │  PostgreSQL  │
│   Vite + TypeScript  │                  │  Java 21            │              │              │
│   TailwindCSS        │                  │  PASETO Security    │              │              │
│   Pinia + Axios      │                  │  Liquibase Migration│              │              │
└─────────────────────┘                   └─────────────────────┘              └──────────────┘
```

- **Frontend (`meeting-room-booking-fe`):** Ứng dụng SPA xây dựng với Vue 3, TypeScript và Vite. Giao tiếp với Backend qua RESTful API.
- **Backend (`meeting-room-booking-api`):** REST API xây dựng với Spring Boot 4, theo kiến trúc phân tầng (Controller → Service → Repository).
- **Database:** PostgreSQL lưu trữ dữ liệu người dùng, phòng họp, phòng ban, lịch đặt và thông báo.

---

## 🛠️ Công Nghệ Sử Dụng

### Backend
| Thành phần | Công nghệ |
|------------|-----------|
| Ngôn ngữ | Java 21 |
| Framework | Spring Boot 4.0.6 |
| Bảo mật | Spring Security + PASETO |
| Cơ sở dữ liệu | PostgreSQL 16 |
| Migration | Liquibase |
| Lập lịch | Spring Scheduler |
| Email | JavaMail (SMTP) |
| Tài liệu API | SpringDoc OpenAPI (Swagger UI) |
| Build Tool | Maven (có `mvnw` wrapper) |

### Frontend
| Thành phần | Công nghệ |
|------------|-----------|
| Framework | Vue 3.5 |
| Build Tool | Vite 6 |
| Ngôn ngữ | TypeScript |
| Quản lý State | Pinia |
| Giao diện | TailwindCSS 4 |
| HTTP Client | Axios |

---

## 🐳 Khởi Chạy Nhanh Với Docker (Khuyến Nghị)

Cách nhanh nhất để chạy toàn bộ hệ thống. Docker sẽ tự động khởi tạo PostgreSQL, build Backend, chạy migration và serve Frontend qua Nginx.

### Yêu Cầu
- Đã cài đặt **Docker** và **Docker Compose**.

### Chạy Ứng Dụng

1. **Build và khởi động tất cả service:**
   ```bash
   docker compose up --build
   ```

2. **Truy cập các dịch vụ:**

   | Dịch vụ | URL |
   |---------|-----|
   | 🌐 Frontend | [http://localhost:5173](http://localhost:5173) |
   | ⚙️ Backend API | [http://localhost:8080](http://localhost:8080) |
   | 📖 Swagger UI | [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html) |
   | 🗄️ PostgreSQL | `localhost:5432` |

3. **Thông tin kết nối Database:**

   | Thuộc tính | Giá trị |
   |------------|---------|
   | Host | `localhost` |
   | Port | `5432` |
   | Database | `meeting_room_booking_db` |
   | Username | `postgres` |
   | Password | `password` |

### Tài Khoản Mặc Định (Seed Data)

Khi chạy với profile `seed`, hệ thống sẽ tự động tạo dữ liệu mẫu:

| Vai trò | Username | Mật khẩu | Ghi chú |
|---------|----------|-----------|---------|
| Admin | `superadmin` | `password` | Quản trị viên toàn hệ thống |
| Approver | `approver.vht` | `password` | Người duyệt phòng ban VHT |
| Employee | `nguyen.van.a` | `password` | Nhân viên mẫu |

### Dừng Ứng Dụng

```bash
# Dừng và giữ nguyên dữ liệu
docker compose down

# Dừng và xoá toàn bộ dữ liệu (reset database)
docker compose down -v
```

---

## 📋 Yêu Cầu Hệ Thống (Cài Đặt Thủ Công)

| Phần mềm | Phiên bản yêu cầu |
|-----------|-------------------|
| Java JDK | 21+ |
| Node.js | 18+ |
| PostgreSQL | 16+ (chạy trên port 5432) |
| Maven | Tuỳ chọn (đã có `mvnw` wrapper) |
| Make | Tuỳ chọn (để chạy lệnh migration thuận tiện) |

---

## ⚙️ Cài Đặt Backend (`meeting-room-booking-api`)

1. **Cấu hình Database:**

   Tạo database PostgreSQL:
   ```sql
   CREATE DATABASE meeting_room_booking_db;
   ```

   Cập nhật file `src/main/resources/application.properties` nếu thông tin kết nối khác mặc định:
   ```properties
   spring.datasource.username=postgres
   spring.datasource.password=password
   spring.datasource.url=jdbc:postgresql://localhost:5432/meeting_room_booking_db
   ```

2. **Chạy Migration:**

   Xem phần [Migration Database](#-migration-database-liquibase) bên dưới.

3. **Khởi động Backend:**
   ```bash
   cd meeting-room-booking-api
   ./mvnw spring-boot:run
   ```
   API sẽ chạy tại `http://localhost:8080`.

4. **(Tuỳ chọn) Chạy với dữ liệu mẫu:**
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=seed
   ```

---

## 💻 Cài Đặt Frontend (`meeting-room-booking-fe`)

1. **Cài đặt dependencies:**
   ```bash
   cd meeting-room-booking-fe
   npm install
   ```

2. **Chạy Development Server:**
   ```bash
   npm run dev
   ```
   Frontend sẽ chạy tại `http://localhost:5173`.

---

## 🗃️ Migration Database (Liquibase)

Dự án sử dụng **Liquibase** để quản lý schema database. File changelog chính nằm tại:
```
src/main/resources/db/changelog/db.changelog-master.yaml
```

Thư mục `meeting-room-booking-api` có sẵn `Makefile` để chạy migration thuận tiện:

| Lệnh | Mô tả |
|-------|-------|
| `make migrate` | Chạy tất cả migration chưa áp dụng |
| `make status` | Kiểm tra trạng thái migration |
| `make rollback COUNT=X` | Rollback X migration gần nhất |
| `make rollback-all` | Xoá toàn bộ bảng và reset database |

Nếu không có `make`, chạy trực tiếp:
```bash
./mvnw liquibase:update
```

---

## 📖 Tài Liệu API (Swagger)

Sau khi Backend đã chạy, truy cập Swagger UI tại:

🔗 **http://localhost:8080/swagger-ui/index.html**

Tại đây bạn có thể:
- Xem toàn bộ danh sách endpoints.
- Hiểu cấu trúc request/response.
- Test API trực tiếp trên trình duyệt.

---

## 📁 Cấu Trúc Dự Án

```
meeting-room-booking/
├── docker-compose.yml              # Cấu hình Docker Compose
├── README.md                       # Tài liệu dự án
│
├── meeting-room-booking-api/       # Backend (Spring Boot)
│   ├── Dockerfile
│   ├── Makefile                    # Lệnh migration tiện ích
│   ├── pom.xml                     # Maven dependencies
│   └── src/main/java/.../
│       ├── controller/             # REST Controllers
│       ├── service/                # Business Logic
│       ├── repository/             # Data Access Layer
│       ├── entity/                 # JPA Entities
│       ├── dto/                    # Data Transfer Objects
│       ├── config/                 # Cấu hình (Security, CORS, ...)
│       ├── filter/                 # Security Filters
│       ├── scheduler/              # Scheduled Tasks (Auto-Release, Reminder)
│       ├── seeder/                 # Database Seeder (dữ liệu mẫu)
│       └── exception/              # Xử lý ngoại lệ
│
└── meeting-room-booking-fe/        # Frontend (Vue 3)
    ├── Dockerfile
    └── src/
        ├── api/                    # API Client (Axios)
        ├── components/             # Vue Components
        ├── composables/            # Vue Composables
        ├── views/                  # Các trang chính
        ├── stores/                 # Pinia Stores
        ├── types/                  # TypeScript Types
        ├── router/                 # Vue Router
        └── assets/                 # Tài nguyên tĩnh (CSS, hình ảnh)
```

---


