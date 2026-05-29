---
title: Hoàn thiện AI kẻ địch (Enemy AI)
labels: enhancement, ai
assignees: []
---

Mô tả:

Thiết kế và triển khai hệ thống hành vi cho kẻ địch: tuần tra, follow, tấn công, rút lui, và pathfinding đơn giản.

Checklist:
- [x] Thêm trạng thái (idle, patrol, chase, attack, retreat)
- [x] Cài đặt detection range và attack range
- [x] Pathfinding cơ bản (A* hoặc grid-based) cho kẻ địch di chuyển
- [x] Kiểm thử kẻ địch trên map mẫu
- [x] Tối ưu hiệu suất AI

Ghi chú: liên quan tới `src/goldenroad/entity/Monster.java` và hệ thống map `src/goldenroad/map/`.

Triển khai:
- `Monster` hiện là quái bay có state machine, patrol route, attack cooldown, retreat khi máu thấp hoặc quá gần player.
- `GridPathfinder` dùng A* trên grid 16px, có giới hạn node và chỉ refresh path theo chu kỳ để giảm tải.
- `CollisionMap` hỗ trợ kiểm tra vùng va chạm và line-of-sight cho enemy bay.
- Map mẫu có quái bay gần vị trí spawn của player để có thể kiểm thử chase/attack ngay.
