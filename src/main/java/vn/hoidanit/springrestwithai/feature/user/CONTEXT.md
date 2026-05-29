# Feature Context: User

## Purpose
Quản lý người dùng trong hệ thống HR. User là entity trung tâm có hai mối quan hệ quan trọng:
- **ManyToOne → Company**: mỗi user thuộc về một công ty (nullable)
- **ManyToMany ↔ Role**: mỗi user có thể có nhiều vai trò (qua bảng `user_role`)

## Key Files
| File | Role |
|------|------|
| `User.java` | Entity với quan hệ Company + Role, audit fields |
| `UserRepository.java` | JPA repo: `existsByEmail`, `existsByEmailAndIdNot`, `findByEmail` |
| `UserService.java` | Interface: CRUD + pagination |
| `UserServiceImpl.java` | Impl: encode password khi tạo, resolve company/roles, @Transactional |
| `UserController.java` | REST: `GET/POST/PUT/DELETE /api/v1/users` |
| `dto/CreateUserRequest.java` | Validation: name, email, password bắt buộc; companyId/roleIds optional |
| `dto/UpdateUserRequest.java` | Validation: id + name + email bắt buộc; không có password field |
| `dto/UserResponse.java` | Nested records: `CompanyInfo(id,name)`, `RoleInfo(id,name)` |

## Business Rules
1. **Email unique**: kiểm tra khi tạo và cập nhật (dùng `existsByEmailAndIdNot` cho update)
2. **Password**: chỉ encode khi **tạo mới** — update KHÔNG thay đổi password
3. **Company (nullable)**: nếu `companyId = null` → user không thuộc công ty nào
4. **Roles (optional)**: nếu `roleIds = null/empty` → user có danh sách roles rỗng
5. **Resolve company/roles**: throw `ResourceNotFoundException` nếu id không tồn tại trong DB

## Relationships
```
User --[ManyToOne]--> Company   (column: company_id, nullable)
User --[ManyToMany]--> Role     (join table: user_role)
```

## API Endpoints
| Method | URL | Description |
|--------|-----|-------------|
| GET | /api/v1/users | Lấy danh sách (phân trang) |
| GET | /api/v1/users/{id} | Lấy theo id |
| POST | /api/v1/users | Tạo mới |
| PUT | /api/v1/users | Cập nhật (body có id) |
| DELETE | /api/v1/users/{id} | Xóa |

## Dependencies
- `CompanyRepository` — resolve company từ companyId
- `RoleRepository` — resolve roles từ roleIds
- `PasswordEncoder` — encode password khi tạo
