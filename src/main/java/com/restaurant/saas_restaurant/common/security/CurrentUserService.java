package com.restaurant.saas_restaurant.common.security;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.restaurant.saas_restaurant.common.entity.Role.RoleName;
import com.restaurant.saas_restaurant.user.entity.User;
import com.restaurant.saas_restaurant.user.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CurrentUserService {
    private final UserRepository userRepository;

    public User getActingUser(Long actingUserId) {
        return userRepository.findByIdAndIsDeletedFalse(actingUserId)
            .orElseThrow(() -> new EntityNotFoundException("Acting user not found: "+actingUserId));
    }

    public Set<RoleName> getRoles(Long actingUserId) {
        return getActingUser(actingUserId).getRoles().stream()
            .map(r -> r.getName())
            .collect(Collectors.toSet());
    }

    public boolean isSuperAdmin(Long actingUserId) {
        return getRoles(actingUserId).contains(RoleName.SUPER_ADMIN);
    }
}
