package com.restaurant.saas_restaurant.zone.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurant.saas_restaurant.restaurant.entity.Restaurant;
import com.restaurant.saas_restaurant.restaurant.repository.RestaurantRepository;
import com.restaurant.saas_restaurant.zone.dto.CreateZoneRequest;
import com.restaurant.saas_restaurant.zone.dto.UpdateZoneRequest;
import com.restaurant.saas_restaurant.zone.dto.ZoneResponse;
import com.restaurant.saas_restaurant.zone.entity.Zone;
import com.restaurant.saas_restaurant.zone.repository.ZoneRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ZoneService {
    private final ZoneRepository zoneRepository;
    private final RestaurantRepository restaurantRepository;

    private ZoneResponse Response(Zone zone) {
        return new ZoneResponse(zone.getId(), zone.getName(), zone.getRestaurant().getId(), zone.getCreated_at(), zone.getUpdated_at());
    }
    private Zone findZoneByIdAndRestaurantId(Long zoneId, Long restaurantId) {
        return zoneRepository.findByIdAndRestaurantId(zoneId, restaurantId)
            .orElseThrow(() -> new NoSuchElementException("Zone not found: " + zoneId));
    }
    private Restaurant findByRestaurantId(Long restaurantId) {
        return restaurantRepository.findById(restaurantId).orElseThrow(() -> new NoSuchElementException("Restaurant not found: "+ restaurantId));
    }
    public ZoneResponse create(Long restaurantId, CreateZoneRequest request) {
        Restaurant restaurant = findByRestaurantId(restaurantId);
        if (zoneRepository.existsByRestaurantIdAndName(restaurantId, request.getName()))
            throw new IllegalArgumentException("A zone with that name already exists!");
        Zone zone = new Zone();
        zone.setName(request.getName());
        zone.setRestaurant(restaurant);
        zone = zoneRepository.save(zone);
        return Response(zone);
    }
    public ZoneResponse getById(Long restaurantId, Long zoneId) {
        Zone zone = findZoneByIdAndRestaurantId(zoneId, restaurantId);
        return Response(zone);
    }
    public ZoneResponse update(Long zoneId,Long restaurantId,UpdateZoneRequest request) {
        Zone zone = findZoneByIdAndRestaurantId(zoneId, restaurantId);
        if (!zone.getName().equals(request.getName()) && zoneRepository.existsByRestaurantIdAndName(restaurantId,request.getName())) {
            throw new IllegalArgumentException("A zone with this name already exists");
        }
        zone.setName(request.getName());
        Zone savedZone = zoneRepository.save(zone);
        return Response(savedZone);
    }
    public List<ZoneResponse> getAllByRestaurantID(Long restaurantId) {
        findByRestaurantId(restaurantId);
        return zoneRepository.getAllZonesByRestaurantId(restaurantId).stream().map(this::Response).toList();
    }
    public void delete(Long restaurantId, Long zoneId) {
        Zone zone = findZoneByIdAndRestaurantId(zoneId, restaurantId);
        zoneRepository.delete(zone); 
    }
}
