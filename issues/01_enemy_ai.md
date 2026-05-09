---
title: Hoàn thiện AI kẻ địch (Enemy AI)
labels: enhancement, ai
assignees: []
---

Mô tả:

Thiết kế và triển khai hệ thống hành vi cho kẻ địch: tuần tra, follow, tấn công, rút lui, và pathfinding đơn giản.

Checklist:
- [ ] Thêm trạng thái (idle, patrol, chase, attack, retreat)
- [ ] Cài đặt detection range và attack range
- [ ] Pathfinding cơ bản (A* hoặc grid-based) cho kẻ địch di chuyển
- [ ] Kiểm thử kẻ địch trên map mẫu
- [ ] Tối ưu hiệu suất AI

Ghi chú: liên quan tới `src/goldenroad/entity/Monster.java` và hệ thống map `src/goldenroad/map/`.
