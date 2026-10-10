package com.restaurant.saas_restaurant.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.restaurant.saas_restaurant.user.entity.User;

public interface UserRepository extends JpaRepository<User,Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);
    Optional<User> findByIdAndIsDeletedFalse(Long id);
    Optional<User> findByIdAndIsDeletedTrue(Long id);
}
