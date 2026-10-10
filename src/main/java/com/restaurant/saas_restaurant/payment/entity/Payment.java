package com.restaurant.saas_restaurant.payment.entity;

import java.math.BigDecimal;

import com.restaurant.saas_restaurant.common.entity.BaseEntity;
import com.restaurant.saas_restaurant.order.entity.CustomerOrder;
import com.restaurant.saas_restaurant.restaurant.enums.PaymentMethod;
import com.restaurant.saas_restaurant.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "payment")
@Getter
@Setter
@NoArgsConstructor
public class Payment extends BaseEntity {

    // one full payment per order
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private CustomerOrder order;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod method;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    // e.g. a transaction id for ONLINE payments
    @Column(length = 100)
    private String reference;

    // set right after the first save, once the id exists (INV-12-000045)
    @Column(name = "invoice_number", unique = true, length = 30)
    private String invoiceNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paid_by", nullable = false)
    private User paidBy;
}