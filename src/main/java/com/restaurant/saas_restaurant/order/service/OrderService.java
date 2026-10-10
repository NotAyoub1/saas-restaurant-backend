package com.restaurant.saas_restaurant.order.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurant.saas_restaurant.common.entity.Role.RoleName;
import com.restaurant.saas_restaurant.common.security.RestaurantAccessService;
import com.restaurant.saas_restaurant.menu.product.entity.Product;
import com.restaurant.saas_restaurant.menu.product.repository.ProductRepository;
import com.restaurant.saas_restaurant.menu.supplement.entity.Supplement;
import com.restaurant.saas_restaurant.menu.supplement.repository.SupplementRepository;
import com.restaurant.saas_restaurant.menu.variant.entity.ProductVariant;
import com.restaurant.saas_restaurant.menu.variant.repository.ProductVariantRepository;
import com.restaurant.saas_restaurant.order.dto.CreateOrderRequest;
import com.restaurant.saas_restaurant.order.dto.OrderItemRequest;
import com.restaurant.saas_restaurant.order.dto.OrderItemResponse;
import com.restaurant.saas_restaurant.order.dto.OrderResponse;
import com.restaurant.saas_restaurant.order.dto.OrderSummaryResponse;
import com.restaurant.saas_restaurant.order.dto.OrderSupplementResponse;
import com.restaurant.saas_restaurant.order.dto.UpdateOrderRequest;
import com.restaurant.saas_restaurant.order.entity.CustomerOrder;
import com.restaurant.saas_restaurant.order.entity.OrderItem;
import com.restaurant.saas_restaurant.order.entity.OrderItemSupplement;
import com.restaurant.saas_restaurant.order.entity.OrderStatus;
import com.restaurant.saas_restaurant.order.repository.CustomerOrderRepository;
import com.restaurant.saas_restaurant.order.repository.OrderSpecifications;
import com.restaurant.saas_restaurant.restaurant.entity.Restaurant;
import com.restaurant.saas_restaurant.restaurant.repository.RestaurantRepository;
import com.restaurant.saas_restaurant.table.entity.RestaurantTable;
import com.restaurant.saas_restaurant.table.repository.RestaurantTableRepository;
import com.restaurant.saas_restaurant.user.entity.User;
import com.restaurant.saas_restaurant.user.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Transactional
@Service
@RequiredArgsConstructor
public class OrderService {

    private final CustomerOrderRepository orderRepository;
    private final RestaurantRepository restaurantRepository;
    private final RestaurantTableRepository tableRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final SupplementRepository supplementRepository;
    private final UserRepository userRepository;
    private final RestaurantAccessService accessService;

    // Roles that can do anything on an order
    private static final Set<RoleName> MANAGEMENT =
        Set.of(RoleName.SUPER_ADMIN, RoleName.OWNER, RoleName.MANAGER);

    // Roles that can create staff-side edits (modify items / cancel)
    private static final Set<RoleName> ORDER_TAKERS =
        Set.of(RoleName.SUPER_ADMIN, RoleName.OWNER, RoleName.MANAGER, RoleName.WAITER, RoleName.CASHIER);

    // Extra roles allowed to move an order INTO each status (management is always allowed)
    private static final Map<OrderStatus, Set<RoleName>> STATUS_ROLES = Map.of(
        OrderStatus.CONFIRMED, Set.of(RoleName.WAITER, RoleName.CASHIER),
        OrderStatus.IN_PREPARATION, Set.of(RoleName.COOK),
        OrderStatus.READY, Set.of(RoleName.COOK),
        OrderStatus.SERVED, Set.of(RoleName.WAITER)
    );

    // helpers

    private Restaurant findRestaurant(Long id) {
        return restaurantRepository.findByIdAndIsDeletedFalse(id)
            .orElseThrow(() -> new EntityNotFoundException("Restaurant not found: " + id));
    }

    private CustomerOrder findOrder(Long orderId, Long restaurantId) {
        return orderRepository.findByIdAndRestaurantId(orderId, restaurantId)
            .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));
    }

    private boolean hasAny(Set<RoleName> roles, Set<RoleName> allowed) {
        return roles.stream().anyMatch(allowed::contains);
    }

    private boolean isStaff(Set<RoleName> roles) {
        return roles.stream().anyMatch(r -> r != RoleName.USER);
    }

    private boolean isPlacedBy(CustomerOrder order, Long userId) {
        return order.getPlacedBy().getId().equals(userId);
    }

    private RestaurantTable resolveTable(String qrToken, Long restaurantId) {
        if (qrToken == null || qrToken.isBlank()) return null;
        RestaurantTable table = tableRepository.findByQrCodeToken(qrToken)
            .orElseThrow(() -> new EntityNotFoundException("Table not found"));
        if (table.isDeleted() || !table.getZone().getRestaurant().getId().equals(restaurantId))
            throw new EntityNotFoundException("Table not found");
        return table;
    }

    // Builds the order lines from the request, snapshotting names and prices from the current menu.
    private void buildItems(CustomerOrder order, List<OrderItemRequest> requests, Long restaurantId) {
        for (OrderItemRequest req : requests) {
            Product product = productRepository.findByIdAndRestaurantIdAndIsDeletedFalse(req.getProductId(), restaurantId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + req.getProductId()));

            BigDecimal unitPrice = product.getBasePrice();
            String variantName = null;
            if (req.getVariantId() != null) {
                ProductVariant variant = variantRepository
                    .findByIdAndProductIdAndIsDeletedFalse(req.getVariantId(), product.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Variant not found: " + req.getVariantId()));
                unitPrice = unitPrice.add(variant.getPriceModifier());
                variantName = variant.getName();
            }

            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setProductName(product.getName());
            item.setVariantName(variantName);
            item.setUnitPrice(unitPrice);
            item.setQuantity(req.getQuantity());

            if (req.getSupplementIds() != null) {
                for (Long supplementId : new LinkedHashSet<>(req.getSupplementIds())) {
                    Supplement supplement = supplementRepository
                        .findByIdAndProductIdAndIsDeletedFalse(supplementId, product.getId())
                        .orElseThrow(() -> new EntityNotFoundException("Supplement not found: " + supplementId));
                    OrderItemSupplement line = new OrderItemSupplement();
                    line.setName(supplement.getName());
                    line.setPrice(supplement.getPrice());
                    item.addSupplement(line);
                }
            }

            item.recalculate();
            order.addItem(item);
        }
    }

    private OrderResponse toResponse(CustomerOrder order) {
        RestaurantTable table = order.getTable();
        List<OrderItemResponse> items = order.getItems().stream()
            .map(i -> new OrderItemResponse(
                i.getId(),
                i.getProduct() != null ? i.getProduct().getId() : null,
                i.getProductName(),
                i.getVariantName(),
                i.getUnitPrice(),
                i.getQuantity(),
                i.getLineTotal(),
                i.getSupplements().stream()
                    .map(s -> new OrderSupplementResponse(s.getName(), s.getPrice()))
                    .collect(Collectors.toList())))
            .collect(Collectors.toList());

        return new OrderResponse(
            order.getId(),
            order.getRestaurant().getId(),
            table != null ? table.getId() : null,
            table != null ? table.getTableNumber() : null,
            table != null ? table.getZone().getId() : null,
            table != null ? table.getZone().getName() : null,
            order.getPlacedBy().getId(),
            order.getStatus(),
            order.getTotalAmount(),
            order.getNotes(),
            items,
            order.getCreated_at(),
            order.getUpdated_at());
    }

    private OrderSummaryResponse toSummary(CustomerOrder order) {
        RestaurantTable table = order.getTable();
        return new OrderSummaryResponse(
            order.getId(),
            table != null ? table.getId() : null,
            table != null ? table.getTableNumber() : null,
            table != null ? table.getZone().getName() : null,
            order.getPlacedBy().getId(),
            order.getStatus(),
            order.getTotalAmount(),
            order.getCreated_at());
    }

    // use cases 

    // Any logged-in user (customer or staff) can place an order.
    public OrderResponse create(Long restaurantId, CreateOrderRequest request, Long actingUserId) {
        Restaurant restaurant = findRestaurant(restaurantId);
        User user = userRepository.findByIdAndIsDeletedFalse(actingUserId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));

        CustomerOrder order = new CustomerOrder();
        order.setRestaurant(restaurant);
        order.setPlacedBy(user);
        order.setNotes(request.getNotes());
        order.setTable(resolveTable(request.getTableQrToken(), restaurantId));
        buildItems(order, request.getItems(), restaurantId);
        order.recalculateTotal();

        return toResponse(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public OrderResponse getById(Long restaurantId, Long orderId, Long actingUserId) {
        Restaurant restaurant = findRestaurant(restaurantId);
        CustomerOrder order = findOrder(orderId, restaurantId);
        Set<RoleName> roles = accessService.rolesIn(restaurant, actingUserId);

        if (!isStaff(roles) && !isPlacedBy(order, actingUserId))
            throw new SecurityException("You are not allowed to view this order");

        return toResponse(order);
    }

    // Staff only: list orders of the restaurant, filtered by zone, table, status and period.
    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> search(Long restaurantId, Long zoneId, Long tableId, OrderStatus status,
            LocalDate from, LocalDate to, Long actingUserId, Pageable pageable) {
        Restaurant restaurant = findRestaurant(restaurantId);
        if (!isStaff(accessService.rolesIn(restaurant, actingUserId)))
            throw new SecurityException("You are not permitted to list the orders of this restaurant");

        Specification<CustomerOrder> spec =
            OrderSpecifications.filter(restaurantId, zoneId, tableId, status, from, to, null);
        return orderRepository.findAll(spec, pageable).map(this::toSummary);
    }

    // A customer's own orders in this restaurant.
    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> searchMine(Long restaurantId, OrderStatus status, Long actingUserId, Pageable pageable) {
        findRestaurant(restaurantId);
        Specification<CustomerOrder> spec =
            OrderSpecifications.filter(restaurantId, null, null, status, null, null, actingUserId);
        return orderRepository.findAll(spec, pageable).map(this::toSummary);
    }

    /**
     * Replace the items (and optionally the notes) of an order.
     * Staff (waiter/cashier/manager/owner/admin): while CREATED or CONFIRMED.
     * The customer who placed it: only while CREATED.
     * Lines are re-priced from the current menu.
     */
    public OrderResponse update(Long restaurantId, Long orderId, UpdateOrderRequest request, Long actingUserId) {
        Restaurant restaurant = findRestaurant(restaurantId);
        CustomerOrder order = findOrder(orderId, restaurantId);
        Set<RoleName> roles = accessService.rolesIn(restaurant, actingUserId);

        if (hasAny(roles, ORDER_TAKERS)) {
            if (order.getStatus() != OrderStatus.CREATED && order.getStatus() != OrderStatus.CONFIRMED)
                throw new IllegalStateException("This order can no longer be modified (status: " + order.getStatus() + ")");
        } else if (isPlacedBy(order, actingUserId)) {
            if (order.getStatus() != OrderStatus.CREATED)
                throw new SecurityException("You can only modify your order before it is confirmed");
        } else {
            throw new SecurityException("You are not allowed to modify this order");
        }

        order.getItems().clear();
        buildItems(order, request.getItems(), restaurantId);
        if (request.getNotes() != null) order.setNotes(request.getNotes());
        order.recalculateTotal();

        return toResponse(orderRepository.save(order));
    }

    // Move the order one step forward in the workflow. Cancelling goes through cancel().
    public OrderResponse updateStatus(Long restaurantId, Long orderId, OrderStatus target, Long actingUserId) {
        if (target == OrderStatus.CANCELLED)
            throw new IllegalArgumentException("Use the cancel endpoint to cancel an order");
        if (target == OrderStatus.PAID)
            throw new IllegalArgumentException("Use the payment endpoint to mark an order as paid");

        Restaurant restaurant = findRestaurant(restaurantId);
        CustomerOrder order = findOrder(orderId, restaurantId);
        Set<RoleName> roles = accessService.rolesIn(restaurant, actingUserId);

        boolean allowed = hasAny(roles, MANAGEMENT)
            || hasAny(roles, STATUS_ROLES.getOrDefault(target, Set.of()));
        if (!allowed)
            throw new SecurityException("Your role is not allowed to set the status " + target);

        if (!order.getStatus().canTransitionTo(target))
            throw new IllegalStateException("Invalid status change: " + order.getStatus() + " -> " + target);

        order.setStatus(target);
        return toResponse(orderRepository.save(order));
    }

    /**
     * Cancel an order. Staff (waiter/cashier/manager/owner/admin) at any step before PAID;
     * the customer who placed it only while it is still CREATED.
     */
    public OrderResponse cancel(Long restaurantId, Long orderId, Long actingUserId) {
        Restaurant restaurant = findRestaurant(restaurantId);
        CustomerOrder order = findOrder(orderId, restaurantId);
        Set<RoleName> roles = accessService.rolesIn(restaurant, actingUserId);

        if (!hasAny(roles, ORDER_TAKERS)) {
            if (!isPlacedBy(order, actingUserId))
                throw new SecurityException("You are not allowed to cancel this order");
            if (order.getStatus() != OrderStatus.CREATED)
                throw new SecurityException("You can only cancel your order before it is confirmed");
        }

        if (!order.getStatus().canTransitionTo(OrderStatus.CANCELLED))
            throw new IllegalStateException("This order can no longer be cancelled (status: " + order.getStatus() + ")");

        order.setStatus(OrderStatus.CANCELLED);
        return toResponse(orderRepository.save(order));
    }
}