package com.vj.ezybuy.payments.dto;

import com.vj.ezybuy.payments.entity.PaymentMethod;
import com.vj.ezybuy.payments.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
    Long id,
    String transactionId,
    Long orderId,
    BigDecimal amount,
    PaymentMethod paymentMethod,
    PaymentStatus status,
    String paymentGatewayTxnId,
    Instant createdAt,
    Instant updatedAt
) {}
