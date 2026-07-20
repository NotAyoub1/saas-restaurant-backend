package com.restaurant.saas_restaurant.employee.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import com.restaurant.saas_restaurant.employee.dto.CreateEmployeeRequest;
import com.restaurant.saas_restaurant.employee.dto.EmployeeResponse;
import com.restaurant.saas_restaurant.employee.dto.UpdateEmployeeRequest;
import com.restaurant.saas_restaurant.employee.entity.Employee;
import com.restaurant.saas_restaurant.employee.enums.Role;
import com.restaurant.saas_restaurant.employee.repository.EmployeeRepository;
import com.restaurant.saas_restaurant.restaurant.entity.Restaurant;
import com.restaurant.saas_restaurant.restaurant.repository.RestaurantRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final RestaurantRepository restaurantRepository;
    private final EmployeeRepository employeeRepository;

    private EmployeeResponse Response(Employee employee) {
        return new EmployeeResponse(employee.getId(),employee.getFirstName(),employee.getLastName(),employee.getEmail(),employee.getRole(),employee.getRestaurant().getId(),employee.getCreated_at(),employee.getUpdated_at());
    }
    private void fillEmployee(Employee employee,String firstName,String lastName,String email,Role role) {
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setEmail(email);
        employee.setRole(role);
    }
    private Restaurant findRestaurantId(Long id) {
        return restaurantRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Restaurant not found: "+ id));
    }
    public List<EmployeeResponse> findAllByRestaurantId(Long id) {
        findRestaurantId(id);
        return employeeRepository.findAllByRestaurantId(id).stream().map(this::Response).toList();
    } 
    private Employee findEmployeeByIdAndRestaurantId(Long employeeId, Long restaurantId) {
        return employeeRepository.findByIdAndRestaurantId(employeeId, restaurantId).orElseThrow(() -> new NoSuchElementException("Employee not found: "+ employeeId));
    }
    public EmployeeResponse create(Long restaurantId, CreateEmployeeRequest request) {
        Restaurant restaurant = findRestaurantId(restaurantId);

        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("This email is already used!");
        }
        Employee employee = new Employee();
        fillEmployee(employee,request.getFirstName(),request.getLastName(),request.getEmail(),request.getRole());
        employee.setRestaurant(restaurant);
        return Response(employeeRepository.save(employee));
    }
    public EmployeeResponse getById(Long restaurantId, Long employeeid) {
        return Response(findEmployeeByIdAndRestaurantId(employeeid, restaurantId));
    }
    public EmployeeResponse update(Long employeeId,Long restaurantId,UpdateEmployeeRequest request) {
        Employee employee = findEmployeeByIdAndRestaurantId(employeeId, restaurantId);
        if (!employee.getEmail().equals(request.getEmail()) && employeeRepository.existsByEmail(request.getEmail())) {
            throw new NoSuchElementException("This email is already used!");
        }
        fillEmployee(employee, request.getFirstName(), request.getLastName(), request.getEmail(), request.getRole());
        return Response(employeeRepository.save(employee));
    }
    public void delete(Long employeeId, Long restaurantId) {
        Employee employee = findEmployeeByIdAndRestaurantId(employeeId, restaurantId);
        employeeRepository.delete(employee);
    }
}
