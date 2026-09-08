package com.project.airbnb.service;

import com.project.airbnb.entity.Booking;

public interface CheckoutService {

    String getCheckoutSession(Booking booking, String successUrl, String failureUrl);
}
