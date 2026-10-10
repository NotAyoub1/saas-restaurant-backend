package com.restaurant.saas_restaurant.employee.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.restaurant.saas_restaurant.common.entity.Role.RoleName;
import com.restaurant.saas_restaurant.employee.entity.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
  
  List<Employee> findAllByRestaurantId(Long id);
  Optional<Employee> findByIdAndRestaurantIdAndIsDeletedFalse(Long id, Long restaurantId);
  Optional<Employee> findByIdAndIsDeletedTrue(Long id);
  Boolean existsByEmail(String email);
  Boolean existsByEmailAndIdNot(String email, Long id);

  @Query(value = "SELECT e.id, u.created_at, u.deleted_at, u.deleted_by, u.email, " +
                "u.first_name, u.is_deleted, u.last_name, u.password, u.updated_at, " +
                "e.restaurant_id " +
                "FROM employee e JOIN users u ON e.id = u.id " +
                "WHERE e.restaurant_id = :restaurantId AND u.is_deleted = true",
        countQuery = "SELECT count(*) FROM employee e JOIN users u ON e.id = u.id " +
                      "WHERE e.restaurant_id = :restaurantId AND u.is_deleted = true",
        nativeQuery = true)
  Page<Employee> searchDeleted(@Param("restaurantId") Long restaurantId, Pageable pageable);
  @Query("""
        SELECT DISTINCT e FROM Employee e
        JOIN e.roles r
        WHERE e.restaurant.id = :restaurantId
          AND e.isDeleted = false
          AND (:email IS NULL OR LOWER(e.email) LIKE LOWER(CONCAT('%', CAST(:email AS string), '%')))
          AND (:firstName IS NULL OR LOWER(e.firstName) LIKE LOWER(CONCAT('%', CAST(:firstName AS string), '%')))
          AND (:lastName IS NULL OR LOWER(e.lastName) LIKE LOWER(CONCAT('%', CAST(:lastName AS string), '%')))
          AND (:role IS NULL OR r.name = :role)
        """)
  Page<Employee> search(@Param("restaurantId") Long restaurantId, @Param("email") String email,
    @Param("firstName") String firstName, @Param("lastName") String lastName,
    @Param("role") RoleName role, Pageable pageable);
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(value = "INSERT INTO employee (id, restaurant_id) VALUES (:id, :restaurantId)", nativeQuery = true)
  void insertEmployeeRow(@Param("id") Long id, @Param("restaurantId") Long restaurantId);
}
