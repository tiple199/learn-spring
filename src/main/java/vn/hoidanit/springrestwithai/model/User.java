package vn.hoidanit.springrestwithai.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;


@Entity
@Table(name = "users")
public class User {
    
    public enum GenderEnum {
        MALE, FEMALE, OTHER
    }

    /**
     * Khóa chính, tự tăng theo chiến lược IDENTITY (AUTO_INCREMENT).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    /**
     * Email phải là duy nhất (UNIQUE) và không được null.
     */
    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    private Integer age;

    private String address;

    /**
     * Lưu enum dưới dạng chuỗi STRING thay vì số thứ tự (ORDINAL mặc định).
     */
    @Enumerated(EnumType.STRING)
    private GenderEnum gender;

    /** Đường dẫn / URL ảnh đại diện của người dùng. */
    private String avatar;

    private Instant createdAt;

    private Instant updatedAt;

    // -------------------------
    // Constructor mặc định (no-args) — bắt buộc cho JPA
    // -------------------------
    public User() {
    }

    public User(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    // -------------------------
    // Getters & Setters
    // -------------------------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public GenderEnum getGender() {
        return gender;
    }

    public void setGender(GenderEnum gender) {
        this.gender = gender;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    // -------------------------
    // toString
    // -------------------------

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", age=" + age +
                ", address='" + address + '\'' +
                ", gender=" + gender +
                ", avatar='" + avatar + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
