package com.vj.ezybuy.payments.service;

import com.vj.ezybuy.payments.dto.PaymentRequest;
import com.vj.ezybuy.payments.dto.PaymentResponse;

import java.util.List;

public interface PaymentService {

    PaymentResponse processPayment(PaymentRequest request);
    List<PaymentResponse> getPaymentsByOrderId(Long orderId);
    PaymentResponse getPaymentByTransactionId(String transactionId);

}
