package com.restaurant.saas_restaurant.waiterzone.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurant.saas_restaurant.employee.entity.Employee;
import com.restaurant.saas_restaurant.employee.enums.Role;
import com.restaurant.saas_restaurant.employee.repository.EmployeeRepository;
import com.restaurant.saas_restaurant.waiterzone.dto.AssignWaiterRequest;
import com.restaurant.saas_restaurant.waiterzone.dto.WaiterZoneResponse;
import com.restaurant.saas_restaurant.waiterzone.entity.WaiterZone;
import com.restaurant.saas_restaurant.waiterzone.repository.WaiterZoneRepository;
import com.restaurant.saas_restaurant.zone.entity.Zone;
import com.restaurant.saas_restaurant.zone.repository.ZoneRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class WaiterZoneService {
    private final WaiterZoneRepository waiterZoneRepository;
    private final ZoneRepository zoneRepository;
    private final EmployeeRepository employeeRepository;

    private WaiterZoneResponse Response(WaiterZone waiterZone) {
        return new WaiterZoneResponse(waiterZone.getId(), 
            waiterZone.getWaiter().getId(),
            waiterZone.getWaiter().getFirstName(),
            waiterZone.getWaiter().getLastName(),
            waiterZone.getZone().getId(),
            waiterZone.getZone().getName(),
            waiterZone.getZone().getRestaurant().getId(),
            waiterZone.getCreated_at(),
            waiterZone.getUpdated_at()
        );
    }

    private Zone findZoneByIdAndRestaurantId(Long zoneId, Long restaurantId) {
        return zoneRepository.findByIdAndRestaurantId(zoneId, restaurantId)
            .orElseThrow(() -> new NoSuchElementException("Zone not found:" + zoneId));
    }
    public WaiterZoneResponse assign(Long restaurantId, Long zoneId, AssignWaiterRequest request) {
        Zone zone = findZoneByIdAndRestaurantId(zoneId, restaurantId);
        Employee waiter = employeeRepository.findByIdAndRestaurantId(request.getWaiterId(), restaurantId)
            .orElseThrow(() -> new NoSuchElementException("Employee not found: "+request.getWaiterId()));
        if (waiter.getRole() != Role.WAITER) {
            throw new IllegalArgumentException("Only an employee with role WAITER can be assigned to a zone");
        }
        WaiterZone waiterZone = waiterZoneRepository.findByZoneId(zoneId).orElse(new WaiterZone());
        waiterZone.setZone(zone);
        waiterZone.setWaiter(waiter);

        return Response(waiterZoneRepository.save(waiterZone));
    }
    @Transactional(readOnly = true)
    public WaiterZoneResponse getByZone(Long restaurantId, Long zoneId) {
        findZoneByIdAndRestaurantId(zoneId, restaurantId);
        WaiterZone waiterZone = waiterZoneRepository.findByZoneId(zoneId)
            .orElseThrow(() -> new NoSuchElementException("No waiter is assigned to zone: "+zoneId));
        return Response(waiterZone);
    }
    @Transactional(readOnly = true)
    public List<WaiterZoneResponse> getAllByRestaurant(Long restaurantId) {
        return waiterZoneRepository.findAllByRestaurantId(restaurantId).stream().map(this::Response).toList();
    }
    public void remove(Long restaurantId, Long zoneId) {
        findZoneByIdAndRestaurantId(zoneId, restaurantId);
        WaiterZone waiterZone = waiterZoneRepository.findByZoneId(zoneId)
            .orElseThrow(() ->new NoSuchElementException("No waiter is assigned to zone: " + zoneId));
        waiterZoneRepository.delete(waiterZone);
    }
}
