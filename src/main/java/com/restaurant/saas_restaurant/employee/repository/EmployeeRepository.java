package com.restaurant.saas_restaurant.employee.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.restaurant.saas_restaurant.employee.entity.Employee;

public interface EmployeeRepository extends JpaRepository<Employee,Long> {
    List<Employee> findAllByRestaurantId(Long id);
    Optional<Employee> findByIdAndRestaurantId(Long id, Long restaurantId);
    Boolean existsByEmail(String email);
}
