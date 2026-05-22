# Role — Implementation Context
> Written: 2026-03-02 | Author: @hoidanit

## Business Context
Role là tầng giữa trong hệ thống RBAC: Permission → Role → User.
Một Role gom nhiều Permission lại; User được gán Role để kế thừa tập quyền đó.
Role phải tồn tại trước khi User có thể được phân quyền (Phase 3).

## Technical Decisions

- **Role owns the ManyToMany**: Role có `@JoinTable` trên join table `permission_role`.
  Khi xóa Role, JPA tự động xóa các row trong `permission_role` (không cần cascade).
  Permission không bị xóa theo (không có `CascadeType`).

- **Không thêm inverse side vào Permission.java**: Tránh circular reference trong JSON
  serialization và bảo vệ các test Permission đang pass. Quan hệ có thể navigate
  một chiều (Role → Permission) là đủ cho feature này.

- **`@Transactional` trên tất cả 5 methods** (kể cả read): `RoleResponse.fromEntity`
  gọi `role.getPermissions()` là LAZY collection — phải có Hibernate session mở.
  Khác với Permission (chỉ scalar fields), Role cần session suốt cả mapping bước.

- **`resolvePermissions()` private helper**:
  1. Dedup input bằng `.distinct()` để tránh false size-mismatch khi client gửi duplicate IDs
  2. Dùng `findAllById(uniqueIds)` — một query `IN (...)` thay vì N+1 `findById`
  3. So sánh size: nếu thiếu → tìm ID đầu tiên không có trong kết quả → throw `ResourceNotFoundException("Quyền hạn", ...)`
  4. Empty list OK — tạo Role không có Permission là hợp lệ

- **PUT thay thế toàn bộ permissions**: `role.setPermissions(newList)` replace, không merge.
  JPA diff join table và issue INSERT/DELETE statements tương ứng.

## API Endpoints
| Method | Path                  | Description                    |
|--------|-----------------------|--------------------------------|
| GET    | /api/v1/roles         | Danh sách có phân trang        |
| GET    | /api/v1/roles/{id}    | Chi tiết một vai trò           |
| POST   | /api/v1/roles         | Tạo vai trò (kèm permissionIds)|
| PUT    | /api/v1/roles         | Cập nhật (id trong body)       |
| DELETE | /api/v1/roles/{id}    | Xóa vai trò                    |

## Dependencies
- Depends on: `permission` (reads Permission entities, uses PermissionRepository)
- Depended by: `user` (ManyToMany via `user_role` join table — Phase 3)

## Integration Test Notes
`@AfterEach` phải xóa theo thứ tự:
```java
roleRepository.deleteAll();       // TRƯỚC: xóa permission_role entries
permissionRepository.deleteAll(); // SAU: mới an toàn xóa permissions
```
Đảo ngược thứ tự → FK constraint violation trên `permission_role.permission_id`.

## Known Limitations
- ⚠️ Không có sorting trên GET list — trả về theo thứ tự DB insertion trong page.
  Sẽ thêm sorting global ở Phase 8 polish.
- ⚠️ Integration tests yêu cầu MySQL trên localhost:3306/hr_management.
  CI phải provision MySQL container.

## Refactor Log
_No changes yet._
