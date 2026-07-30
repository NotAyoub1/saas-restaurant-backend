package com.restaurant.saas_restaurant.waiterzone.entity;

import com.restaurant.saas_restaurant.common.entity.BaseEntity;
import com.restaurant.saas_restaurant.employee.entity.Employee;
import com.restaurant.saas_restaurant.zone.entity.Zone;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table (name = "waiter_zone", uniqueConstraints = @UniqueConstraint( name = "uk_waiter_zone_zone", columnNames = "zone_id"))
@Getter
@Setter
public class WaiterZone extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "waiter_id", nullable = false)
    private Employee waiter;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;
}
