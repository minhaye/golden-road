# SCIENTIFIC WITCHERY

Mô tả ngắn: trò chơi 2D theo phong cách metroidvania, phát triển bằng Java cho môn Lập trình Hướng đối tượng. Người chơi khám phá một cơ sở nghiên cứu, chiến đấu với robot, thu thập vật phẩm và mở khóa kỹ năng để tiến sâu vào các tầng tiếp theo.

Link báo cáo tiến độ
https://docs.google.com/document/d/1UuNXtMHy_4ewUpr_JN-LcqvNzZqqJhtlUja23kPz9RQ/edit?tab=t.0#heading=h.orhg72i9j0dq

Link plot + đề xuất 
https://docs.google.com/document/d/138VPqWpEGtzm8KxrY4FCCdarLBsm77eg-wRZ5wo0ONM/edit?tab=t.0

---

**Thông tin về game**

- Thể loại: Metroidvania / Platformer 2D
- Ngôn ngữ: Java (Swing cho rendering)
- Entry point: [src/goldenroad/main/Main.java](src/goldenroad/main/Main.java)
- Thư mục assets: `src/assets/` và `src/assets/player/`

---

**Những gì đã hoàn thành hiện tại**

- Hệ thống thực thể cơ bản: [src/goldenroad/entity/Entity.java](src/goldenroad/entity/Entity.java), [src/goldenroad/entity/Player.java](src/goldenroad/entity/Player.java), [src/goldenroad/entity/Monster.java](src/goldenroad/entity/Monster.java), [src/goldenroad/entity/Bullet.java](src/goldenroad/entity/Bullet.java), [src/goldenroad/entity/Item.java](src/goldenroad/entity/Item.java)
- Giao diện game và panel: [src/goldenroad/game/GamePanel.java](src/goldenroad/game/GamePanel.java)
- Xử lý input: [src/goldenroad/input/KeyHandler.java](src/goldenroad/input/KeyHandler.java), [src/goldenroad/input/MouseHandler.java](src/goldenroad/input/MouseHandler.java)
- Hệ thống map & va chạm: [src/goldenroad/map/CollisionMap.java](src/goldenroad/map/CollisionMap.java), [src/goldenroad/map/CollisionHandler.java](src/goldenroad/map/CollisionHandler.java)
- Hệ thống render & camera: [src/goldenroad/render/RenderSystem.java](src/goldenroad/render/RenderSystem.java), [src/goldenroad/render/Camera.java](src/goldenroad/render/Camera.java)
- Quản lý scene/menu: [src/goldenroad/scene/SceneManager.java](src/goldenroad/scene/SceneManager.java), [src/goldenroad/scene/Floor.java](src/goldenroad/scene/Floor.java), [src/goldenroad/scene/Menu.java](src/goldenroad/scene/Menu.java)

---

**Các công việc cần triển khai tiếp**

1. Hoàn thiện AI kẻ địch (pathfinding, trạng thái tấn công/tuần tra)
2. Cơ chế nhặt vật phẩm, cập nhật inventory và hiệu ứng dùng item
3. Thêm animation cho nhân vật, kẻ địch và vật phẩm
4. Âm thanh: hiệu ứng va chạm, nhạc nền, hiệu ứng kỹ năng
5. Giao diện HUD (HP/MP/skill cooldown/inventory)
6. Thiết kế level/maps bổ sung và cân bằng gameplay
7. Lưu/Load trạng thái người chơi
8. Tối ưu va chạm và render để giảm lag
9. Kiểm thử (unit/integration) và sửa lỗi
10. Đóng gói thành file JAR để chạy độc lập

---

**Dự định phân chia công việc (gợi ý)**

- Game Logic & Entities: phát triển các lớp entity, hệ thống skill, inventory
- AI & Enemy Behavior: thiết kế trạng thái và hành vi kẻ địch
- Map & Collisions: xây dựng công cụ thiết kế map, tối ưu hệ va chạm
- Rendering & Camera: cải thiện hệ render, animation, camera theo nhân vật
- Input & Controls: hoàn thiện điều khiển, mapping phím, chuột
- UI & Scenes: menu, HUD, màn hình tạm dừng, màn hình kết thúc
- Assets & Sound: quản lý sprites, tilesets, âm thanh
- Testing & Build: test, CI, đóng gói release

Gợi ý phân công: mỗi mục giao cho 1 dev (hoặc nhóm nhỏ) tùy quy mô. Giao việc bằng issue/branch theo `feature/<tên>`.

---

**Hướng dẫn đóng góp nhanh**

- Tạo branch mới từ `main`: `feature/<mô-tả-ngắn>`
- Mỗi PR kèm mô tả, checklist các bước kiểm thử
- Kiểm tra build bằng IDE hoặc dòng lệnh trước khi gửi PR

Ví dụ chạy nhanh (từ IDE hoặc terminal):

```bash
# Biên dịch (tham khảo cấu trúc project)
javac -d bin src/goldenroad/main/Main.java src/goldenroad/**/**/*.java

# Chạy (điều chỉnh classpath nếu cần)
java -cp bin goldenroad.main.Main
```

---

Nếu muốn, tôi có thể mở thêm các issue/tickets tương ứng cho từng công việc trên để phân công cụ thể.
