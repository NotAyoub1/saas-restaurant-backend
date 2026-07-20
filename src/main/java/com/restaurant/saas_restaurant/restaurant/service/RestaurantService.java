package com.restaurant.saas_restaurant.restaurant.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.restaurant.saas_restaurant.restaurant.repository.RestaurantRepository;
import com.restaurant.saas_restaurant.restaurant.dto.RestaurantResponse;
import com.restaurant.saas_restaurant.restaurant.entity.Restaurant;
import com.restaurant.saas_restaurant.restaurant.dto.CreateRestaurantRequest;
import com.restaurant.saas_restaurant.restaurant.dto.UpdateRestaurantRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RestaurantService {
    private final RestaurantRepository restaurantRepository;

    private void applyRequest(Restaurant restaurant, String name, String address, String phone, String email) {
        restaurant.setName(name);
        restaurant.setAddress(address);
        restaurant.setPhone(phone);
        restaurant.setEmail(email);
    }

    private RestaurantResponse Response(Restaurant restaurant) {
        return new RestaurantResponse(restaurant.getId(),restaurant.getName(),restaurant.getAddress(),restaurant.getPhone(),restaurant.getEmail(),restaurant.getCreated_at(),restaurant.getUpdated_at());
    }

    public RestaurantResponse create(CreateRestaurantRequest request) {
        if (restaurantRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("A restaurant already uses this email");
        }
        Restaurant restaurant = new Restaurant();
        applyRequest(restaurant,request.getName(), request.getAddress(),request.getPhone(), request.getEmail());
        return Response(restaurantRepository.save(restaurant));
    }

    private Restaurant findRestaurantById(Long id) {
        if (restaurantRepository.findById(id).isEmpty()) {
            throw new IllegalArgumentException("Restaurant not found");
        }
        return restaurantRepository.findById(id).get();
    }

    public void delete(Long id) {
        Restaurant restaurant = findRestaurantById(id);
        restaurantRepository.delete(restaurant);
    }

    public RestaurantResponse getById(Long id) {
        return Response(findRestaurantById(id));
    }

    public RestaurantResponse update(Long id, UpdateRestaurantRequest request) {
        Restaurant restaurant = findRestaurantById(id);

        if (restaurantRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("A restaurant already uses this email");
        }
        applyRequest(restaurant,request.getName(),request.getAddress(),request.getPhone(),request.getEmail());
        return Response(restaurantRepository.save(restaurant));
    }
    public List<RestaurantResponse> getAll() {
        return restaurantRepository.findAll().stream().map(this::Response).toList();
    }
}
