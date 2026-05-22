package vn.hoidanit.springrestwithai.feature.company;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "companies")
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(length = 500)
    private String address;
    @Column(length = 500)
    private String logo;

    private Instant CreateAt;

    private Instant UpdateAt;

    public Company() {
    }

    @PrePersist
    protected void onCreate() {
        CreateAt = Instant.now();
        UpdateAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        UpdateAt = Instant.now();
    }

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public Instant getCreateAt() {
        return CreateAt;
    }

    public void setCreateAt(Instant createAt) {
        CreateAt = createAt;
    }

    public Instant getUpdateAt() {
        return UpdateAt;
    }

    public void setUpdateAt(Instant updateAt) {
        UpdateAt = updateAt;
    }
}
