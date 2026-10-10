package com.restaurant.saas_restaurant.employee.service;

import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurant.saas_restaurant.common.entity.Role;
import com.restaurant.saas_restaurant.common.entity.Role.RoleName;
import com.restaurant.saas_restaurant.common.repository.RoleRepository;
import com.restaurant.saas_restaurant.common.security.CurrentUserService;
import com.restaurant.saas_restaurant.common.security.RoleAssignmentPolicy;
import com.restaurant.saas_restaurant.employee.dto.AssignEmployeeRequest;
import com.restaurant.saas_restaurant.employee.dto.EmployeeResponse;
import com.restaurant.saas_restaurant.employee.dto.UpdateEmployeeRequest;
import com.restaurant.saas_restaurant.employee.entity.Employee;
import com.restaurant.saas_restaurant.employee.repository.EmployeeRepository;
import com.restaurant.saas_restaurant.restaurant.entity.Restaurant;
import com.restaurant.saas_restaurant.restaurant.repository.RestaurantRepository;
import com.restaurant.saas_restaurant.user.entity.User;
import com.restaurant.saas_restaurant.user.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Transactional
@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final RestaurantRepository restaurantRepository;
    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;
    private final RoleAssignmentPolicy roleAssignmentPolicy;

    private EmployeeResponse toResponse(Employee employee) {
        Set<RoleName> roleNames = employee.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
        return new EmployeeResponse(
            employee.getId(),
            employee.getFirstName(),
            employee.getLastName(),
            employee.getEmail(),
            roleNames,
            employee.getRestaurant().getId(),
            employee.getCreated_at(),
            employee.getUpdated_at()
        );
    }
    private Restaurant findRestaurantById(Long id) {
        return restaurantRepository.findByIdAndIsDeletedFalse(id)
            .orElseThrow(() -> new EntityNotFoundException("Restaurant not found: "+id));
    }
    private Employee findEmployeeByIdAndRestaurantId(Long employeeId, Long restaurantId) {
        return employeeRepository.findByIdAndRestaurantIdAndIsDeletedFalse(employeeId, restaurantId)
            .orElseThrow(() -> new EntityNotFoundException("Employee not found: " + employeeId));
    }
    private Set<Role> resolveRoles(Set<RoleName> names) {
        return names.stream()
            .map(name -> roleRepository.findByName(name)
                    .orElseThrow(() -> new EntityNotFoundException("Role not seeded: "+name)))
            .collect(Collectors.toSet());
    }
    private void assertCanManage(Restaurant restaurant, Long actingUserId) {
        if (currentUserService.isSuperAdmin(actingUserId)) return;
        if (restaurant.getOwnerId().equals(actingUserId)) return;
        boolean isManagerHere = employeeRepository.findByIdAndRestaurantIdAndIsDeletedFalse(actingUserId, restaurant.getId())
            .map(e -> e.hasRole(RoleName.MANAGER))
            .orElse(false);
        if (isManagerHere) return;
        throw new SecurityException("You are not permitted to manage employees of this restaurant");
    }
    private void assertCanDelete(Restaurant restaurant, Long actingUserId) {
        if (currentUserService.isSuperAdmin(actingUserId)) return;
        if (restaurant.getOwnerId().equals(actingUserId)) return;
        throw new SecurityException("Only Admin or the restaurant OWNER can delete employees");
    }
    private RoleName resolveActingRole(Restaurant restaurant, Long actingUserId) {
        if (restaurant.getOwnerId().equals(actingUserId)) return RoleName.OWNER;
        return employeeRepository.findByIdAndRestaurantIdAndIsDeletedFalse(actingUserId, restaurant.getId())
            .flatMap(e -> e.getRoles().stream().map(Role::getName).max(Comparator.comparingInt(RoleName::rank)))
            .orElseThrow(() -> new SecurityException("You have no role in this restaurant"));
    }
    @Transactional(readOnly = true)
    public Page<EmployeeResponse> search(Long restaurantId, String email, String firstName, String lastName, RoleName role, Long actingUserId, Pageable pageable) {
        Restaurant restaurant = findRestaurantById(restaurantId);
        assertCanManage(restaurant, actingUserId);
        return employeeRepository.search(restaurantId, email, firstName, lastName, role, pageable).map(this::toResponse);
    }
    public EmployeeResponse create(Long restaurantId, AssignEmployeeRequest request, Long actingUserId) {
        Restaurant restaurant = findRestaurantById(restaurantId);
        assertCanManage(restaurant, actingUserId);
        if (!currentUserService.isSuperAdmin(actingUserId)) {
            RoleName actingRole = resolveActingRole(restaurant, actingUserId);
            roleAssignmentPolicy.assertCanAssignRoles(actingRole,request.getRoles());
        }
        User user = userRepository.findByIdAndIsDeletedFalse(request.getUserId())
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
        if (employeeRepository.existsById(user.getId()))
            throw new IllegalArgumentException("This user is already an employee");
        employeeRepository.insertEmployeeRow(user.getId(), restaurantId);
        Employee employee = employeeRepository.findById(user.getId())
            .orElseThrow(() -> new IllegalStateException("Employee row insert failed"));
        employee.setRoles(resolveRoles(request.getRoles()));
        return toResponse(employeeRepository.save(employee));
    }
    @Transactional(readOnly = true)
    public EmployeeResponse getById(Long restaurantId, Long employeeId, Long actingUserId) {
        Restaurant restaurant = findRestaurantById(restaurantId);
        assertCanManage(restaurant, actingUserId);
        return toResponse(findEmployeeByIdAndRestaurantId(employeeId, restaurantId));
    }
    public EmployeeResponse update(Long employeeId, Long restaurantId, UpdateEmployeeRequest request, Long actingUserId) {
        Employee employee = findEmployeeByIdAndRestaurantId(employeeId, restaurantId);
        assertCanManage(employee.getRestaurant(), actingUserId);
        boolean isSuperAdmin = currentUserService.isSuperAdmin(actingUserId);
        if (!isSuperAdmin && employeeId.equals(actingUserId)) {
            throw new SecurityException("You cannot update your own employee data");
        }
        employee.setRoles(resolveRoles(request.getRoles()));
        return toResponse(employeeRepository.save(employee));
    }
    public void softDelete(Long employeeId, Long restaurantId, Long actingUserId) {
        Employee employee = findEmployeeByIdAndRestaurantId(employeeId, restaurantId);
        assertCanDelete(employee.getRestaurant(), actingUserId);
        employee.markDeleted(String.valueOf(actingUserId));
        employeeRepository.save(employee);
    }
    public void restore(Long employeeId, Long restaurantId, Long actingUserId) {
        Restaurant restaurant = findRestaurantById(restaurantId);
        assertCanDelete(restaurant, actingUserId);
        Employee employee = employeeRepository.findByIdAndIsDeletedTrue(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found: " + employeeId));
        employee.restore();
        employeeRepository.save(employee);
    }
    @Transactional(readOnly = true)
    public Page<EmployeeResponse> searchDeleted(Long restaurantId, Long actingUserId, Pageable pageable) {
        if (!currentUserService.isSuperAdmin(actingUserId))
            throw new SecurityException("Only Admin can view deleted employees");
        return employeeRepository.searchDeleted(restaurantId, pageable).map(this::toResponse);
    }
}