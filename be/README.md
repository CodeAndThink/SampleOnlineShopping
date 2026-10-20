# SampleOnlineShopping

Project backend thương mại điện tử, tổ chức theo microservices.

## Cấu trúc

- `services/catalog-service/`: ứng dụng Java độc lập dành cho nghiệp vụ sản phẩm.
- `services/auth-service/`: khung ứng dụng cho tích hợp xác thực Firebase.
- Mỗi service quản lý cấu hình build và source code riêng.

## Catalog service

Mở `services/catalog-service` trong IDE như một Gradle project, hoặc liên kết
file `services/catalog-service/build.gradle.kts` khi mở thư mục gốc.

Chạy build từ thư mục gốc:

```sh
cd services/catalog-service
./gradlew build
```

Package gốc: `com.truongngo.catalog`.

- `domain`: đối tượng và quy tắc nghiệp vụ.
- `application`: use case và các interface phục vụ use case.
- `infrastructure`: triển khai persistence và các kết nối bên ngoài.
- `presentation`: lớp giao tiếp HTTP.

Hiện tại service vẫn là Java thuần với entry point `CatalogApplication`.
Spring Boot, PostgreSQL và Flyway sẽ được bổ sung ở bước tiếp theo.

## Auth service

Mở `services/auth-service` như một Gradle project độc lập hoặc liên kết
`services/auth-service/build.gradle.kts` trong IDE.

```sh
cd services/auth-service
./gradlew build
```

Package gốc: `com.truongngo.auth`; entry point: `AuthApplication`.
Cấu trúc Clean Architecture giống `catalog-service`.

Hiện tại đây chỉ là khung Java thuần, chưa có HTTP API, Spring Security,
Firebase SDK hay logic đăng nhập/phân quyền.

Dự kiến Firebase xử lý đăng nhập Google và cấp ID token. Backend xác minh
ID token; từng service nghiệp vụ kiểm tra quyền truy cập tài nguyên của mình.
`auth-service` không thay thế Firebase bằng một hệ thống cấp token riêng.
