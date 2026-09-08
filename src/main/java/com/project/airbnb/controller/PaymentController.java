package com.project.airbnb.controller;

import com.project.airbnb.dto.PaymentRequestDTO;
import com.project.airbnb.dto.StripeResponse;
import com.project.airbnb.service.StripeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {
    private StripeService stripeService;
    public PaymentController(StripeService stripeService) {
        this.stripeService = stripeService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<StripeResponse> checkout(@RequestBody PaymentRequestDTO paymentRequestDTO){

        StripeResponse response = stripeService.checkoutProduct(paymentRequestDTO);
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }
}
