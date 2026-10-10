package com.restaurant.saas_restaurant.user.service;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurant.saas_restaurant.common.entity.Role;
import com.restaurant.saas_restaurant.common.entity.Role.RoleName;
import com.restaurant.saas_restaurant.common.repository.RoleRepository;
import com.restaurant.saas_restaurant.common.security.CurrentUserService;
import com.restaurant.saas_restaurant.user.dto.CreateUserRequest;
import com.restaurant.saas_restaurant.user.dto.UpdateUserRequest;
import com.restaurant.saas_restaurant.user.dto.UpdateUserRolesRequest;
import com.restaurant.saas_restaurant.user.dto.UserResponse;
import com.restaurant.saas_restaurant.user.entity.User;
import com.restaurant.saas_restaurant.user.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Transactional
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserService currentUserService;

    private UserResponse toResponse(User user) {
        Set<RoleName> roleNames = user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                roleNames,
                user.getCreated_at(),
                user.getUpdated_at()
        );
    }
    private User findUserById(Long id) {
        return userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + id));
    }
    private Set<Role> resolveRoles(Set<RoleName> names) {
        return names.stream()
                .map(name -> roleRepository.findByName(name)
                    .orElseThrow(() -> new EntityNotFoundException("Role not seeded: " + name)))
                .collect(Collectors.toSet());
    }
    private Set<Role> resolveRole(RoleName name) {
        Role role = roleRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Role not seeded: " +name));
        return Set.of(role);
    }
    //Only ADMIN can manage user accounts
    private void assertIsSuperAdmin(Long actingUserId) {
        if (!currentUserService.isSuperAdmin(actingUserId)) {
            throw new SecurityException("Only ADMIN can manage user accounts");
        }
    }
    public UserResponse create(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("This email is already used!");
        }
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(resolveRole(RoleName.USER)); //gets USER as default role
        return toResponse(userRepository.save(user));
    }
    @Transactional(readOnly = true)
    public UserResponse getById(Long id, Long actingUserId) {
        assertIsSuperAdmin(actingUserId);
        return toResponse(findUserById(id));
    }
    public UserResponse update(Long id, UpdateUserRequest request, Long actingUserId) {
        assertIsSuperAdmin(actingUserId);
        User user = findUserById(id);
        if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("This email is already used!");
        }
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        return toResponse(userRepository.save(user));
    }
    public UserResponse updateRoles(Long id, UpdateUserRolesRequest request, Long actingUserId) {
        assertIsSuperAdmin(actingUserId);
        User user = findUserById(id);
        user.setRoles(resolveRoles(request.getRoles()));
        return toResponse(userRepository.save(user));
    }
    public void softDelete(Long id, String actingUsername, Long actingUserId) {
        assertIsSuperAdmin(actingUserId);
        User user = findUserById(id);
        user.markDeleted(actingUsername);
        userRepository.save(user);
    }
    public void restore(Long id, Long actingUserId) {
        assertIsSuperAdmin(actingUserId);
        User user = userRepository.findByIdAndIsDeletedTrue(id)
            .orElseThrow(() -> new EntityNotFoundException("User not found: " + id));
        user.restore();
        userRepository.save(user);
    }
    @Transactional(readOnly = true)
    public Page<UserResponse> search(String email, RoleName role, Long actingUserId, Pageable pageable) {
        assertIsSuperAdmin(actingUserId);
        Specification<User> spec = (root, query, cb) -> cb.isFalse(root.get("isDeleted"));
        if (email != null && !email.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%"));
        }
        if (role != null) {
            spec = spec.and((root, query, cb) -> {
                query.distinct(true);
                return cb.equal(root.join("roles").get("name"), role);
            });
        }
        return userRepository.findAll(spec, pageable).map(this::toResponse);
    }
}
