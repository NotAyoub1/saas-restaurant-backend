package com.restaurant.saas_restaurant.table.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurant.saas_restaurant.table.dto.CreateRestaurantTableRequest;
import com.restaurant.saas_restaurant.table.dto.RestaurantTableResponse;
import com.restaurant.saas_restaurant.table.dto.UpdateRestaurantTableRequest;
import com.restaurant.saas_restaurant.table.entity.RestaurantTable;
import com.restaurant.saas_restaurant.table.repository.RestaurantTableRepository;
import com.restaurant.saas_restaurant.zone.entity.Zone;
import com.restaurant.saas_restaurant.zone.repository.ZoneRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class RestaurantTableService {
    private final RestaurantTableRepository restaurantTableRepository;
    private final ZoneRepository zoneRepository;

    private void fillRestaurantTable(RestaurantTable restaurantTable, String tableNumber, Integer capacity) {
        restaurantTable.setTableNumber(tableNumber);
        restaurantTable.setCapacity(capacity);    
    }
    private RestaurantTableResponse Response(RestaurantTable restaurantTable) {
        return new RestaurantTableResponse(
            restaurantTable.getId(), 
            restaurantTable.getTableNumber(), 
            restaurantTable.getCapacity(), 
            restaurantTable.getQrCodeToken(), 
            restaurantTable.getZone().getId(), 
            restaurantTable.getZone().getRestaurant().getId(), 
            restaurantTable.getCreated_at(), 
            restaurantTable.getUpdated_at()
        );
    }
    private RestaurantTable findTableByIdAndZoneAndRestaurant(Long tableId,Long zoneId,Long restaurantId) {
        return restaurantTableRepository.findByIdAndZoneIdAndZoneRestaurantId(tableId, zoneId, restaurantId).orElseThrow(() -> new NoSuchElementException("Table not found: "+tableId));
    }
    private Zone findZoneByIdAndRestaurantId(Long zoneId, Long restaurantId) {
        return zoneRepository.findByIdAndRestaurantId(zoneId, restaurantId)
            .orElseThrow(() -> new NoSuchElementException("Zone not found: " + zoneId));
    }
    @Transactional(readOnly = true)
    public RestaurantTableResponse getByQrCodeToken(String qrCodeToken) {
        RestaurantTable restaurantTable = restaurantTableRepository.findByQrCodeToken(qrCodeToken).orElseThrow(() -> new NoSuchElementException("QR code not found: "+ qrCodeToken));
        return Response(restaurantTable);
    }
    public void delete(Long tableId, Long zoneId, Long restaurantId) {
        RestaurantTable restaurantTable = findTableByIdAndZoneAndRestaurant(tableId, zoneId, restaurantId);
        restaurantTableRepository.delete(restaurantTable);
    }
    @Transactional(readOnly = true)
    public RestaurantTableResponse getById(Long restaurantId, Long zoneId, Long tableId) {
        return Response(findTableByIdAndZoneAndRestaurant(tableId, zoneId, restaurantId));
    }
    @Transactional(readOnly = true)
    public List<RestaurantTableResponse> getAllByRestaurant(Long restaurantId) {
        return restaurantTableRepository.findAllByRestaurantId(restaurantId).stream().map(this::Response).toList();
    }
    @Transactional(readOnly = true)
    public List<RestaurantTableResponse> getAllByZone(Long restaurantId, Long zoneId) {
        findZoneByIdAndRestaurantId(zoneId, restaurantId);
        return restaurantTableRepository.findAllByZoneId(zoneId).stream().map(this::Response).toList();
    }
    public RestaurantTableResponse create(CreateRestaurantTableRequest request, Long zoneId, Long restaurantId) {
        Zone zone = findZoneByIdAndRestaurantId(zoneId, restaurantId);
        if (restaurantTableRepository.existsByZoneIdAndTableNumber(zoneId, request.getTableNumber())) 
            throw new IllegalArgumentException("A table with this number already exists in this zone");
        RestaurantTable restaurantTable = new RestaurantTable();
        fillRestaurantTable(restaurantTable, request.getTableNumber(), request.getCapacity());
        restaurantTable.setQrCodeToken(UUID.randomUUID().toString());
        restaurantTable.setZone(zone);
        restaurantTable = restaurantTableRepository.save(restaurantTable);
        return Response(restaurantTable);
    }
    public RestaurantTableResponse update(UpdateRestaurantTableRequest request, Long zoneId, Long restaurantId, Long tableId) {
        RestaurantTable restaurantTable = findTableByIdAndZoneAndRestaurant(tableId, zoneId, restaurantId);
        if (!restaurantTable.getTableNumber().equals(request.getTableNumber()) && restaurantTableRepository.existsByZoneIdAndTableNumber(zoneId, request.getTableNumber())) 
            throw new IllegalArgumentException("A table with this number already exists in this zone");
        fillRestaurantTable(restaurantTable, request.getTableNumber(), request.getCapacity());
        restaurantTable = restaurantTableRepository.save(restaurantTable);
        return Response(restaurantTable);
    }
}
