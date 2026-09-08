package com.project.airbnb.service;

import com.project.airbnb.dto.PaymentRequestDTO;
import com.project.airbnb.dto.StripeResponse;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.aspectj.internal.lang.annotation.ajcDeclareEoW;
import org.springframework.stereotype.Service;

@Service
public class StripeService {

    public StripeResponse checkoutProduct(PaymentRequestDTO paymentRequestDTO){

        SessionCreateParams.LineItem.PriceData.ProductData productData =
                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                        .setName(paymentRequestDTO.getProductName())
                        .build();

        SessionCreateParams.LineItem.PriceData priceData =
                SessionCreateParams.LineItem.PriceData.builder()
                        .setCurrency(paymentRequestDTO.getCurrency())
                        .setUnitAmount(paymentRequestDTO.getAmount().multiply(new java.math.BigDecimal(100)).longValue())
                        .setProductData(productData)
                        .build();

        SessionCreateParams.LineItem lineItem =
                SessionCreateParams.LineItem.builder()
                        .setPriceData(priceData)
                        .setQuantity(paymentRequestDTO.getQuantity())
                        .build();

        SessionCreateParams params =
                SessionCreateParams.builder()
                        .addLineItem(lineItem)
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setSuccessUrl("http://localhost:8080/success")
                        .setCancelUrl("http://localhost:8080/cancel")
                        .build();

        Session session = null;
        try {
            session = Session.create(params);
        } catch (Exception e) {
            e.printStackTrace();
            return new StripeResponse(null, "Error creating checkout session: " + e.getMessage(), "error", null);
        }
        return StripeResponse.builder()
                .sessionId(session.getId())
                .message("Checkout session created successfully")
                .status("success")
                .sessionUrl(session.getUrl())
                .build();
    }

}
