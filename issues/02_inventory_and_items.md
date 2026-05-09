---
title: Cơ chế nhặt vật phẩm & hệ thống inventory
labels: enhancement, gameplay
assignees: []
---

Mô tả:

Triển khai cơ chế nhặt vật phẩm, lưu trữ vào inventory, sử dụng item (HP/MP potion, key), và hiển thị trong HUD.

Checklist:
- [ ] Thiết kế class `Inventory` và `Item` API
- [ ] Implement nhặt item và update inventory
- [ ] Hiệu ứng khi dùng item (HP/MP restore)
- [ ] Kết nối với HUD để hiển thị số lượng
- [ ] Kiểm thử

Ghi chú: xem `src/goldenroad/entity/Item.java` và `src/goldenroad/scene/`.
