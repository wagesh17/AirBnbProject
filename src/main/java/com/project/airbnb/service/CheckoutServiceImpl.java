package com.project.airbnb.service;

import com.project.airbnb.entity.Booking;
import com.project.airbnb.entity.User;
import com.project.airbnb.repository.BookingRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckoutServiceImpl implements CheckoutService{

    private final BookingRepository bookingRepository;

    @Override
    public String getCheckoutSession(Booking booking, String successUrl, String failureUrl) {

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("Creating checkout session for booking id: {}, user: {}", booking.getId(), user.getEmail());
        try{
        Customer customer = Customer.create(
                CustomerCreateParams.builder()
                        .setName(user.getName())
                        .setEmail(user.getEmail())
                        .build()
        );
        SessionCreateParams sessionParams = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setBillingAddressCollection(SessionCreateParams.BillingAddressCollection.REQUIRED)
                .setCustomer(customer.getId())
                .setSuccessUrl(successUrl)
                .setCancelUrl(failureUrl)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("inr")
                                                .setUnitAmount(booking.getAmount().multiply(new java.math.BigDecimal(100)).longValue())
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Booking for hotel: "+booking.getHotel().getName()+", room: "+booking.getRoom().getType())
                                                                .setDescription("BookingId: "+booking.getId()+", check-in: "+booking.getCheckInDate()+", check-out: "+booking.getCheckOutDate())
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .build();

            Session session = Session.create(sessionParams);

            booking.setPaymentSessionId(session.getId());
            bookingRepository.save(booking);
            log.info("Checkout session created successfully for booking id: {}, session id: {}", booking.getId(), session.getId());
            return session.getUrl();
        }
        catch (StripeException e){
            throw  new RuntimeException("erorr in checkout");
        }
    }
}
