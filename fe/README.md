# Catalog API Lab

Trang kiểm thử local bằng HTML/JS và Python 3, không cần npm. Không dùng server này để deploy production.

## Chạy

Từ thư mục gốc SampleOnlineShopping:

```sh
python3 fe/server.py --backend-port 8080
```

Mở **http://localhost:5173** (không mở file HTML trực tiếp). Nếu catalog chạy 8081, đổi `--backend-port 8081`. Server chỉ lắng nghe loopback và chỉ proxy GET/POST `/api/products`, không sửa cấu hình CORS backend.

## Firebase

1. Firebase Console → Project settings → Your apps → thêm Web app nếu chưa có.
2. Lấy apiKey, projectId, authDomain, appId từ firebaseConfig, nhập vào giao diện. Đây là cấu hình Web; không nhập private key/service account.
3. Authentication → Sign-in method → bật Google.
4. Authentication → Settings → Authorized domains → thêm `localhost` nếu chưa có.
5. Project ID phải khớp FIREBASE_PROJECT_ID của catalog.

SDK tải từ CDN Google nên cần Internet. Đăng nhập dùng popup; cho phép popup nếu trình duyệt chặn. Phiên đăng nhập chỉ lưu trong bộ nhớ: tải lại trang sẽ cần cấu hình/đăng nhập lại.

## Kiểm thử

- Bỏ chọn gửi token: GET → 200, POST → 401.
- Đăng nhập tài khoản chưa có ADMIN, gửi token: POST → 403.
- Sau khi backend quản trị cấp custom claim `roles: ["ADMIN"]`, nhấn Làm mới token / quyền: POST → 201.
- POST thành công tạo dữ liệu thật trong database local.
- 502: kiểm tra catalog đã chạy và đúng --backend-port.
- 401 khi có token: kiểm tra project ID, token và cấu hình JWT backend.
- Nút sao chép lấy Firebase ID token, không phải Google access token; không chia sẻ token.

Việc hiển thị roles trên trang không cấp quyền. Backend vẫn xác minh chữ ký và quyền. Trang không có chức năng tự cấp ADMIN.
