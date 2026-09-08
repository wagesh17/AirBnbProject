package com.project.airbnb.service;

import com.project.airbnb.dto.BookingDto;
import com.project.airbnb.dto.BookingRequest;
import com.project.airbnb.dto.GuestDto;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface BookingService  {
    BookingDto initializeBooking(BookingRequest bookingRequest);

    BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList);

    String initiatePayments(Long bookingId);
}
