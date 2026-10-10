package com.restaurant.saas_restaurant.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InvoiceFile {
    private String filename;
    private byte[] content;
}