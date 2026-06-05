# SCIENTIFIC WITCHERY

Scientific Witchery là game platformer 2D phong cách metroidvania được phát triển bằng Java Swing cho môn Lập trình Hướng đối tượng. Người chơi điều khiển nhân vật khám phá các phòng thí nghiệm, tiêu diệt quái vật, thu thập vật phẩm và dùng key để mở đường sang map tiếp theo.

## Tài liệu nhóm

- Báo cáo tổng: https://docs.google.com/document/d/1MrxgA7x5BaSrnTLDqTEuLMbqwj0k8qiKsfzggrGQr1M/edit?usp=sharing
- Báo cáo tiến độ: https://docs.google.com/document/d/1UuNXtMHy_4ewUpr_JN-LcqvNzZqqJhtlUja23kPz9RQ/edit?tab=t.0#heading=h.orhg72i9j0dq
- Plot và đề xuất: https://docs.google.com/document/d/138VPqWpEGtzm8KxrY4FCCdarLBsm77eg-wRZ5wo0ONM/edit?tab=t.0

## Thông tin dự án

- Thể loại: Metroidvania / 2D platformer shooter
- Ngôn ngữ: Java
- Giao diện: Java Swing
- Entry point: [src/goldenroad/main/Main.java](src/goldenroad/main/Main.java)
- Assets: [src/assets/](src/assets/)
- Package chính: [src/goldenroad/](src/goldenroad/)

## Tính năng hiện có

- Game loop 60 FPS, cửa sổ Swing và render theo buffer trong [GamePanel.java](src/goldenroad/game/GamePanel.java)
- Menu chính, pause menu, settings, tutorial và màn hình kết thúc trong [Menu.java](src/goldenroad/scene/Menu.java) và [EndScreenOverlay.java](src/goldenroad/ui/EndScreenOverlay.java)
- Hệ thống map nhiều phòng, collision map, hidden layer và chuyển map trong [MapCatalog.java](src/goldenroad/map/MapCatalog.java), [CollisionMap.java](src/goldenroad/map/CollisionMap.java), [GameWorld.java](src/goldenroad/game/GameWorld.java)
- Nhân vật người chơi với di chuyển, nhảy, dash, tài nguyên HP/MP và bắn đạn trong [Player.java](src/goldenroad/entity/player/Player.java), [PlayerMovement.java](src/goldenroad/entity/player/PlayerMovement.java), [PlayerAttack.java](src/goldenroad/entity/player/PlayerAttack.java)
- Quái vật mặt đất và bay với nhiều behavior như idle, patrol, aggressive, airborne trong [src/goldenroad/entity/monster/](src/goldenroad/entity/monster/)
- Vật phẩm HP, MP, Key, inventory và quick-use trong [src/goldenroad/entity/item/](src/goldenroad/entity/item/) và [InventoryPanel.java](src/goldenroad/ui/InventoryPanel.java)
- HUD, minimap, toast thông báo và overlay game over/victory trong [src/goldenroad/ui/](src/goldenroad/ui/)
- Âm thanh menu, nhạc nền theo map và hiệu ứng gameplay trong [src/goldenroad/audio/](src/goldenroad/audio/)
- Lưu settings và tiến độ map trong [src/goldenroad/settings/](src/goldenroad/settings/)

## Điều khiển

| Hành động | Phím / chuột |
| --- | --- |
| Di chuyển trái/phải | A / D hoặc phím mũi tên trái/phải |
| Nhảy | W, mũi tên lên hoặc Space |
| Đi xuống | S hoặc mũi tên xuống |
| Chạy nhanh | Ctrl |
| Dash | Shift |
| Ngắm và bắn | Chuột trái / chuột phải |
| Mở inventory | Tab hoặc I |
| Dùng nhanh item | 1, 2, 3 |
| Pause / mở menu | Esc |
| Toggle minimap | Alt + N |
| Chuyển map để test | Alt + M |
| Diệt toàn bộ quái để test | Alt + X |

## Cách chạy

Yêu cầu:

- JDK 17 trở lên
- Terminal hỗ trợ lệnh Unix/Git Bash hoặc chạy trực tiếp bằng IDE

Chạy bằng terminal từ thư mục gốc project:

```bash
mkdir -p bin
javac -encoding UTF-8 -d bin $(git ls-files "src/**/*.java")
java -cp "bin:src" goldenroad.main.Main
```

Nếu chạy trên Windows Command Prompt hoặc PowerShell, hãy đổi dấu phân tách classpath từ `:` sang `;`:

```bash
java -cp "bin;src" goldenroad.main.Main
```

Có thể chạy trực tiếp trong IDE bằng class [Main.java](src/goldenroad/main/Main.java).

## Cấu trúc thư mục

```text
src/
├── assets/                 # Hình ảnh, map, âm thanh, sprites
└── goldenroad/
    ├── audio/              # Nhạc nền và hiệu ứng âm thanh
    ├── entity/             # Player, monster, projectile, item
    ├── game/               # GamePanel, GameWorld, input controller
    ├── input/              # Keyboard và mouse handler
    ├── main/               # Entry point
    ├── map/                # Map catalog, collision, pathfinding
    ├── render/             # Camera, render system, parallax
    ├── scene/              # Menu, screen, scene manager, spawn planner
    ├── settings/           # Difficulty, settings, progress store
    ├── ui/                 # HUD, inventory panel, overlays
    └── util/               # Asset loader
```

## Đóng góp

- Tạo branch mới từ `main`, ví dụ: `feature/inventory-ui` hoặc `fix/player-collision`
- Mỗi PR nên có mô tả thay đổi, ảnh/video nếu có UI, và checklist đã test
- Trước khi gửi PR, hãy build lại và chạy thử game bằng [Main.java](src/goldenroad/main/Main.java)

## Công việc có thể phát triển tiếp

- Hoàn thiện cân bằng gameplay theo từng difficulty
- Bổ sung map, quái và boss
- Hoàn thiện animation cho player, monster và vật phẩm
- Cải thiện lưu/load trạng thái người chơi
- Thêm test tự động và cấu hình build/đóng gói JAR
- Tối ưu render/collision nếu map hoặc số lượng entity tăng
