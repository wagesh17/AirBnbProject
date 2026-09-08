package com.project.airbnb.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class PaymentRequestDTO {

    private String productName;
    private Long quantity;
    private BigDecimal amount;
    private String currency;

}