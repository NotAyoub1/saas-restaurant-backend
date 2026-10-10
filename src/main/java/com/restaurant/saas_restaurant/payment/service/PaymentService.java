package com.restaurant.saas_restaurant.payment.service;

import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurant.saas_restaurant.common.entity.Role.RoleName;
import com.restaurant.saas_restaurant.common.security.RestaurantAccessService;
import com.restaurant.saas_restaurant.order.entity.CustomerOrder;
import com.restaurant.saas_restaurant.order.entity.OrderStatus;
import com.restaurant.saas_restaurant.order.repository.CustomerOrderRepository;
import com.restaurant.saas_restaurant.payment.dto.CreatePaymentRequest;
import com.restaurant.saas_restaurant.payment.dto.InvoiceFile;
import com.restaurant.saas_restaurant.payment.dto.PaymentResponse;
import com.restaurant.saas_restaurant.payment.entity.Payment;
import com.restaurant.saas_restaurant.payment.repository.PaymentRepository;
import com.restaurant.saas_restaurant.restaurant.entity.Restaurant;
import com.restaurant.saas_restaurant.restaurant.repository.RestaurantRepository;
import com.restaurant.saas_restaurant.user.entity.User;
import com.restaurant.saas_restaurant.user.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Transactional
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final CustomerOrderRepository orderRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final RestaurantAccessService accessService;
    private final InvoicePdfGenerator invoiceGenerator;

    private static final Set<RoleName> PAYMENT_ROLES =
        Set.of(RoleName.SUPER_ADMIN, RoleName.OWNER, RoleName.MANAGER, RoleName.CASHIER);

    // ---------- helpers ----------

    private Restaurant findRestaurant(Long id) {
        return restaurantRepository.findByIdAndIsDeletedFalse(id)
            .orElseThrow(() -> new EntityNotFoundException("Restaurant not found: " + id));
    }

    private CustomerOrder findOrder(Long orderId, Long restaurantId) {
        return orderRepository.findByIdAndRestaurantId(orderId, restaurantId)
            .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));
    }

    private boolean isPaymentStaff(Restaurant restaurant, Long userId) {
        return accessService.rolesIn(restaurant, userId).stream().anyMatch(PAYMENT_ROLES::contains);
    }

    /** Payment staff, or the customer who placed the order. */
    private void assertCanView(Restaurant restaurant, CustomerOrder order, Long userId) {
        boolean placedByUser = order.getPlacedBy().getId().equals(userId);
        if (!placedByUser && !isPaymentStaff(restaurant, userId))
            throw new SecurityException("You are not allowed to view this payment");
    }

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
            payment.getId(),
            payment.getOrder().getId(),
            payment.getOrder().getRestaurant().getId(),
            payment.getAmount(),
            payment.getMethod(),
            payment.getReference(),
            payment.getInvoiceNumber(),
            payment.getPaidBy().getId(),
            payment.getCreated_at());
    }

    // ---------- use cases ----------

    /** Records the full payment of a SERVED order and moves it to PAID. */
    public PaymentResponse pay(Long restaurantId, Long orderId, CreatePaymentRequest request, Long actingUserId) {
        Restaurant restaurant = findRestaurant(restaurantId);
        if (!isPaymentStaff(restaurant, actingUserId))
            throw new SecurityException("You are not permitted to record payments in this restaurant");

        CustomerOrder order = findOrder(orderId, restaurantId);

        if (paymentRepository.existsByOrderId(orderId))
            throw new IllegalStateException("This order is already paid");
        if (order.getStatus() != OrderStatus.SERVED)
            throw new IllegalStateException("Only a SERVED order can be paid (status: " + order.getStatus() + ")");
        if (!restaurant.getAcceptedPaymentMethods().contains(request.getMethod()))
            throw new IllegalArgumentException("This restaurant does not accept the payment method " + request.getMethod());

        User user = userRepository.findByIdAndIsDeletedFalse(actingUserId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setMethod(request.getMethod());
        payment.setAmount(order.getTotalAmount()); // always the full order total
        payment.setReference(request.getReference());
        payment.setPaidBy(user);

        Payment saved = paymentRepository.save(payment);
        saved.setInvoiceNumber(String.format("INV-%d-%06d", restaurantId, saved.getId()));

        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getByOrder(Long restaurantId, Long orderId, Long actingUserId) {
        Restaurant restaurant = findRestaurant(restaurantId);
        CustomerOrder order = findOrder(orderId, restaurantId);
        assertCanView(restaurant, order, actingUserId);

        Payment payment = paymentRepository.findByOrderIdAndOrderRestaurantId(orderId, restaurantId)
            .orElseThrow(() -> new EntityNotFoundException("No payment found for order " + orderId));
        return toResponse(payment);
    }

    @Transactional(readOnly = true)
    public InvoiceFile getInvoice(Long restaurantId, Long orderId, Long actingUserId) {
        Restaurant restaurant = findRestaurant(restaurantId);
        CustomerOrder order = findOrder(orderId, restaurantId);
        assertCanView(restaurant, order, actingUserId);

        Payment payment = paymentRepository.findByOrderIdAndOrderRestaurantId(orderId, restaurantId)
            .orElseThrow(() -> new EntityNotFoundException("No payment found for order " + orderId));

        return new InvoiceFile(payment.getInvoiceNumber() + ".pdf", invoiceGenerator.generate(payment));
    }
}