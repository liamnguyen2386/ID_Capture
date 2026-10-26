# CM Fast Capture Android WebView v3.0

## Mục đích
Bọc **Web App GitHub Pages v2.5** thành APK Android, giữ nguyên giao diện và quy trình chụp ảnh.
Không sao chép `index.html` vào APK: ứng dụng mở chính URL GitHub Pages đang chạy, vì vậy cần Internet khi khởi động lần đầu.

## Bắt buộc sửa 1 dòng trước khi build
Tệp `app/src/main/java/com/cm/fastcapture/webview/MainActivity.java`:
```
private static final String WEB_APP_URL = "https://YOUR_GITHUB_USERNAME.github.io/cm-fast-capture-web/";
```
Thay bằng **URL GitHub Pages thực tế của bạn**, giữ `https://` và dấu `/` cuối.
Nếu repository GitHub Pages tên khác, sử dụng đúng URL được GitHub hiển thị ở Settings → Pages.

## Build không cần Android Studio
1. GitHub → tạo repository mới, ví dụ `CM-Fast-Capture-Android` (nên để private).
2. Upload toàn bộ **nội dung bên trong thư mục mã nguồn**, giữ đúng cấu trúc `.github/workflows/build.yml`.
3. Sửa `WEB_APP_URL`, commit.
4. Actions → `Build CM Fast Capture WebView APK` → Run workflow.
5. Khi xanh, tải artifact `CM-Fast-Capture-WebView-APK` → giải nén → `app-debug.apk`.
6. Cài trên Android. Cho phép Camera khi hệ thống hỏi; Web App có thể hỏi URL Apps Script và mật khẩu upload lần đầu.

## Kiểm thử
- Mở APK → màn hình logo Cooler Master / Chụp ảnh Căn cước / Bắt đầu chụp ảnh.
- Nhấn bắt đầu → cấp quyền Camera → camera trực tiếp, 1 nút chụp.
- Chụp 2 ảnh thử, kiểm tra bộ đếm và Drive theo thư mục ngày.
- Tắt mạng, chụp thử; bật lại mạng và mở app để kiểm tra ảnh trong IndexedDB được đồng bộ.
- Không xóa dữ liệu ứng dụng hay gỡ cài đặt khi còn ảnh chờ đồng bộ.

## Giới hạn và bảo mật
- APK **không đồng bộ nền bảo đảm** khi đóng ứng dụng. Không dùng để thay thế WorkManager native.
- Dữ liệu IndexedDB của APK nằm riêng với Chrome; ảnh đang chờ trong Chrome không tự chuyển sang APK.
- Không lưu bí mật upload vào mã nguồn hay GitHub công khai.
- Dùng cho căn cước là xử lý dữ liệu cá nhân nhạy cảm: cần quyền truy cập, bảo mật backend và chính sách lưu trữ phù hợp.
- `app-debug.apk` là bản kiểm thử, chưa phải bản phát hành được ký chính thức.
- WebView được giới hạn điều hướng vào URL GitHub Pages cấu hình, cấp quyền chỉ cho camera và chỉ từ HTTPS origin đó.


## v3.1
Đã cấu hình URL GitHub Pages: https://liamnguyen2386.github.io/CM-fast-capture-web/
Chưa kiểm chứng được trạng thái truy cập trang từ môi trường này; kiểm tra URL trên điện thoại trước khi build.
