package com.restaurant.saas_restaurant.table.entity;

import com.restaurant.saas_restaurant.common.entity.BaseEntity;
import com.restaurant.saas_restaurant.zone.entity.Zone;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "restaurant_table", uniqueConstraints = @UniqueConstraint(name = "uk_table_zone_number", columnNames = {"zone_id", "table_number"}))
@Getter
@Setter
@NoArgsConstructor
public class RestaurantTable extends BaseEntity {
    @Column(name = "table_number", nullable = false)
    private String tableNumber;
    
    @Column(nullable = false)
    private Integer capacity;

    @Column(name = "qr_code_token", nullable = false, unique = true, length = 100)
    private String qrCodeToken;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;
}
