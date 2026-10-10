package com.restaurant.saas_restaurant.common.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Setter
@Getter
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime created_at;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updated_at;

    @Column(name = "deleted_at")
    private LocalDateTime deleted_at;

    @Column(name = "deleted_by")
    private String deleted_by;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @PrePersist
    protected void on_create() {
        LocalDateTime now = LocalDateTime.now();
        created_at = now;
        updated_at = now;
    }

    @PreUpdate
    protected void on_update() {
        updated_at = LocalDateTime.now();
    }

    public void markDeleted(String deletedBy) {
        this.isDeleted = true;
        this.deleted_at = LocalDateTime.now();
        this.deleted_by = deletedBy;
    }

    public void restore() {
        this.isDeleted = false;
        this.deleted_at = null;
        this.deleted_by = null;
    }
}
