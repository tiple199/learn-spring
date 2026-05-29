# Các Cách Phân Quyền User Trong Doanh Nghiệp (Spring Security)

> Mục tiêu: hiểu được bức tranh tổng thể các cách phân quyền, biết khi nào dùng cách nào.

---

## Mở đầu

Phân quyền (Authorization) trả lời câu hỏi: "User này được phép làm gì?"

Trong thực tế đi làm, tùy quy mô và yêu cầu của doanh nghiệp, có **5 cách tiếp cận** từ đơn giản đến phức tạp. Không có cách nào "tốt nhất" — chỉ có cách **phù hợp nhất** với bài toán cụ thể.

---

## Cách 1: Hardcode trong SecurityFilterChain

**Tư tưởng:** Phân quyền theo URL pattern, viết thẳng trong config.

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/admin/**").hasRole("ADMIN")
    .requestMatchers("/api/hr/**").hasAnyRole("HR", "ADMIN")
    .requestMatchers("/api/user/**").hasRole("USER")
    .anyRequest().authenticated())
```

**Ai dùng:** Startup nhỏ, internal tool, MVP, side project.

**Ưu điểm:**
- Đơn giản nhất, dễ hiểu
- Không cần thêm bảng DB nào
- Nhìn vào SecurityConfig là biết ai được truy cập gì

**Nhược điểm:**
- Mỗi lần thêm/sửa quyền → phải sửa code → build lại → deploy lại
- Khi hệ thống lớn (50+ endpoints) → file config rất dài, khó quản lý
- Business team (không biết code) không tự thay đổi quyền được

**Khi nào dùng:** Hệ thống nhỏ, ít role (2-3 roles), quyền ít thay đổi. Ví dụ: admin dashboard nội bộ, tool quản lý kho đơn giản.

---

## Cách 2: Annotation-based (@PreAuthorize) 

**Tư tưởng:** Gắn quyền trực tiếp trên từng method của Controller hoặc Service.

```java
@PreAuthorize("hasRole('ADMIN')")
@DeleteMapping("/users/{id}")
public void deleteUser(@PathVariable Long id) { ... }

@PreAuthorize("hasRole('HR') or #id == authentication.principal.claims['userId']")
@GetMapping("/users/{id}")
public UserResponse getUser(@PathVariable Long id) { ... }

@PreAuthorize("hasAuthority('APPROVE_LEAVE') and #request.amount <= 5")
@PostMapping("/leaves/approve")
public void approveLeave(@RequestBody LeaveRequest request) { ... }
```

**Cần:** `@EnableMethodSecurity` trong SecurityConfig.

**Ai dùng:** Startup đến mid-size, hệ thống cần logic phân quyền phức tạp hơn URL pattern.

**Ưu điểm:**
- Quyền nằm ngay cạnh code xử lý → dễ đọc, dễ review
- Hỗ trợ SpEL (Spring Expression Language) → logic linh hoạt: check owner, check amount, check department...
- Kết hợp được cả role lẫn business logic trong cùng 1 expression

**Nhược điểm:**
- Vẫn hardcode trong code → thay đổi quyền = sửa code + deploy
- Annotation rải rác khắp nơi → khó có cái nhìn tổng thể "ai được làm gì"
- Khi team lớn, dễ quên gắn annotation → endpoint bị lộ (thiếu security)

**Khi nào dùng:** Hệ thống trung bình, cần logic phân quyền theo business rule (chỉ sửa data của mình, chỉ approve đơn dưới X triệu...). Phổ biến nhất trong các dự án Spring Boot thực tế.

---

## Cách 3: Dynamic Permission-based (DB-driven)

**Tư tưởng:** Lưu quyền trong database, check runtime bằng custom AuthorizationManager. Admin thay đổi quyền trên giao diện, có hiệu lực ngay — không cần deploy lại.

**Mô hình DB:**

```
User ←ManyToMany→ Role ←ManyToMany→ Permission
                                        │
                                   (apiPath + httpMethod + module)
```

**Cơ chế:**

```
Request đến: GET /api/v1/users
    │
    ├─ Lấy roles của user từ JWT
    ├─ Từ roles → lấy permissions từ cache
    ├─ Check: có permission nào match (GET + /api/v1/users)?
    │
    ├─ Có → 200
    └─ Không → 403
```

**Implement:** Dùng custom AuthorizationManager đăng ký vào SecurityFilterChain:

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/v1/auth/**").permitAll()
    .anyRequest().access(permissionAuthorizationManager))  // ← custom
```

**Ai dùng:** Doanh nghiệp vừa đến lớn. Đây là cách dự án của khóa học đang áp dụng.

**Ưu điểm:**
- Dynamic: admin tạo role mới, gắn permission, có hiệu lực ngay
- Không cần sửa code khi thay đổi quyền
- Có giao diện quản lý → business team tự quản lý được
- Audit được: biết role nào có permission gì, thay đổi lúc nào

**Nhược điểm:**
- Phức tạp hơn nhiều: cần thiết kế DB, viết CRUD cho Role/Permission, build UI quản lý
- Cần cache (vì mỗi request đều check) → thêm logic invalidate cache
- Path matching phức tạp: `/api/v1/users/{id}` vs `/api/v1/users/5`
- Không check được business logic (chỉ sửa data của mình) — chỉ check được "có quyền gọi API này không"

**Khi nào dùng:** Hệ thống cần admin quản lý quyền linh hoạt, nhiều role khác nhau, quyền thay đổi thường xuyên. Ví dụ: HRM, ERP, CRM nội bộ doanh nghiệp.

---

## Cách 4: Kết hợp DB-driven + @PreAuthorize 

**Tư tưởng:** Thực tế trong doanh nghiệp, cách 3 (DB-driven) chỉ trả lời được "user có quyền gọi API này không?" nhưng không trả lời được "user có quyền thao tác trên resource cụ thể này không?". Vì vậy nhiều hệ thống **kết hợp cả hai**.

**Ví dụ thực tế:**

```
Tầng 1 — DB-driven (AuthorizationManager):
  "User role HR có quyền gọi PUT /api/v1/users không?"
  → Có (permission tồn tại trong DB) → cho vào Controller

Tầng 2 — @PreAuthorize hoặc Service logic:
  "Nhưng HR chỉ được sửa user cùng department"
  → Check trong Service: user.department == currentUser.department?
  → Đúng → xử lý / Sai → 403
```

```java
// Tầng 1: đã pass qua AuthorizationManager (check API-level permission)

// Tầng 2: business logic trong Service
public UserResponse updateUser(Long id, UpdateUserRequest request) {
    User target = userRepository.findById(id).orElseThrow();
    User currentUser = getCurrentUser();

    // HR chỉ sửa user cùng department
    if (currentUser.hasRole("HR") 
            && !target.getDepartment().equals(currentUser.getDepartment())) {
        throw new ForbiddenException("Chỉ được sửa user cùng phòng ban");
    }
    // ...
}
```

**Ai dùng:** Đa số doanh nghiệp vừa và lớn đều dùng cách này.

**Ưu điểm:** Linh hoạt nhất — vừa dynamic (DB) vừa xử lý được business rule phức tạp.

**Nhược điểm:** Logic phân quyền nằm ở 2 nơi (SecurityFilterChain + Service) → cần document rõ ràng, dễ confuse nếu team không thống nhất convention.

---

## Cách 5: External Authorization Service (OPA, Casbin, Keycloak Authorization) 

**Tư tưởng:** Tách hoàn toàn logic phân quyền ra khỏi application code, đưa vào một service/engine riêng. Application chỉ hỏi: "User X có được làm action Y trên resource Z không?" và nhận về yes/no.

**Các tool phổ biến:**

- **OPA (Open Policy Agent):** Viết policy bằng ngôn ngữ Rego. Google, Netflix, Atlassian dùng.
- **Casbin:** Library hỗ trợ nhiều model (RBAC, ABAC, ACL). Có Java adapter.
- **Keycloak Authorization Services:** Tích hợp sẵn nếu đã dùng Keycloak làm Identity Provider.
- **AWS IAM / Google IAP:** Nếu chạy trên cloud, dùng luôn authorization của cloud provider.

**Flow với OPA:**

```
Spring Boot App                          OPA Server
     │                                        │
     ├─ POST /v1/data/authz/allow             │
     │  {                                     │
     │    "input": {                          │
     │      "user": "john@company.com",       │
     │      "roles": ["HR"],                  │
     │      "action": "PUT",                  │
     │      "resource": "/api/v1/users/5",    │
     │      "department": "engineering"       │
     │    }                                   │
     │  }                                     │
     │                              ──────►   │
     │                                        │  Evaluate policy
     │                              ◄──────   │
     │  { "result": true }                    │
     │                                        │
     └─ Tiếp tục xử lý request                │
```

**Ai dùng:** Enterprise lớn, microservices, hệ thống cần centralized policy management.

**Ưu điểm:**
- Policy tách biệt khỏi code → thay đổi policy không cần deploy app
- Centralized: 1 nơi quản lý policy cho tất cả services
- Policy as Code: version control, review, test policy như code
- Hỗ trợ ABAC phức tạp (based on time, location, department, resource attributes...)

**Nhược điểm:**
- Thêm 1 service cần vận hành (OPA server)
- Network latency mỗi request (có thể cache để giảm)
- Learning curve cao (Rego, Casbin model)
- Overkill cho monolith hoặc hệ thống nhỏ

**Khi nào dùng:** Microservices cần chung policy, compliance yêu cầu (HIPAA, SOC2), team có DevOps/Platform Engineer vận hành. Ví dụ: fintech, healthcare, enterprise SaaS.

---

## Tổng kết — Chọn cách nào ?

| Cách | Quy mô | Thay đổi quyền | Độ phức tạp | Ví dụ thực tế |
|------|--------|-----------------|-------------|----------------|
| 1. Hardcode URL | Nhỏ | Deploy lại | ⭐ | Admin tool nội bộ |
| 2. @PreAuthorize | Nhỏ → Vừa | Deploy lại | ⭐⭐ | REST API startup |
| 3. DB-driven Permission | Vừa → Lớn | Ngay lập tức (qua UI) | ⭐⭐⭐ | ERP, CRM, HRM |
| 4. DB-driven + Business logic | Vừa → Lớn | Ngay + linh hoạt | ⭐⭐⭐⭐ | Đa số doanh nghiệp |
| 5. External (OPA, Casbin) | Lớn (microservices) | Centralized policy | ⭐⭐⭐⭐⭐ | Fintech, healthcare |

**Quy tắc chọn:**
- Dưới 10 endpoints, 2-3 roles → **Cách 1 hoặc 2**
- Cần admin quản lý quyền qua UI → **Cách 3**
- Cần check "chỉ sửa data của mình", "chỉ approve đơn dưới X triệu" → **Cách 4**
- Microservices, nhiều team, compliance → **Cách 5**

Lưu ý: trong thực tế, không nhất thiết phải chọn 1 cách duy nhất. Nhiều hệ thống bắt đầu bằng cách 2, sau đó evolve sang cách 3 + 4 khi business phát triển.

Trong khóa học này, chúng ta implement **Cách 3 (DB-driven Permission)** kết hợp **Cách 4 (business logic trong Service)** — đủ cho đa số doanh nghiệp vừa và lớn tại Việt Nam.