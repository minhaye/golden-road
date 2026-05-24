---
title: Lưu/Load trạng thái người chơi
labels: enhancement, persistence
assignees: []
---

Mô tả:

Thêm chức năng lưu trạng thái (player position, HP/MP, inventory, unlocked skills) và load khi khởi động lại.

Checklist:
- [ ] Định dạng file save (JSON/serialized)
- [ ] Implement save/load API
- [ ] Tự động save ở checkpoint
- [ ] Kiểm thử compatibility giữa phiên bản
