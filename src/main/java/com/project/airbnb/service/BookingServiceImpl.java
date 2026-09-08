package com.project.airbnb.service;

import com.project.airbnb.dto.BookingDto;
import com.project.airbnb.dto.BookingRequest;
import com.project.airbnb.dto.GuestDto;
import com.project.airbnb.entity.*;
import com.project.airbnb.entity.enums.BookingStatus;
import com.project.airbnb.exception.ResourceNotFoundException;
import com.project.airbnb.exception.UnAuthorisedException;
import com.project.airbnb.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService{
    private final GuestRepository guestRepository;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final InventoryRepository inventoryRepository;
    private final ModelMapper modelMapper;
    private final CheckoutService checkoutService;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Transactional
    @Override
    public BookingDto initializeBooking(BookingRequest bookingRequest) {

        log.info("Initializing booking for hotel : {}, room : {}, date {}-{}",bookingRequest.getHotelId(),bookingRequest.getRoomId(),bookingRequest.getCheckInDate(),bookingRequest.getCheckOutDate());

        Hotel hotel = hotelRepository.findById(bookingRequest.getHotelId()).orElseThrow(() ->
                new ResourceNotFoundException("Hotel not found with Id: "+bookingRequest.getHotelId()));
        Room room = roomRepository.findById(bookingRequest.getRoomId()).orElseThrow(() ->
                new ResourceNotFoundException("Room not found with id: "+bookingRequest.getRoomId()));

        List<Inventory> inventoryList = inventoryRepository.findAndLockAvailableInventory(room.getId(),
                bookingRequest.getCheckInDate(),bookingRequest.getCheckOutDate(), bookingRequest.getRoomsCount());

        long daysCount = ChronoUnit.DAYS.between(bookingRequest.getCheckInDate(),bookingRequest.getCheckOutDate())+1;
        if(inventoryList.size()!=daysCount){
            throw new IllegalStateException("Room is not available");
        }

        for (Inventory inventory: inventoryList){
            inventory.setReservedCount(inventory.getReservedCount()+ bookingRequest.getRoomsCount());
        }

        inventoryRepository.saveAll(inventoryList);

        User user = new User();
        user.setId(1L);

        //todo : calculate dynamic price
        Booking booking = Booking.builder()
                .bookingStatus(BookingStatus.RESERVED)
                .hotel(hotel)
                .room(room)
                .checkInDate(bookingRequest.getCheckInDate())
                .checkOutDate(bookingRequest.getCheckOutDate())
                .user(user)
                .roomsCount(bookingRequest.getRoomsCount())
                .amount(BigDecimal.TEN)
                .build();

        booking = bookingRepository.save(booking);
        return modelMapper.map(booking, BookingDto.class);

    }

    @Override
    @Transactional
    public BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList) {
        log.info("Adding guests for booking for hotel : {}", bookingId);

        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                new ResourceNotFoundException("Booking not found with Id: "+bookingId));

        User user = getCurrentUser();
        if(!user.equals(booking.getUser())){
            throw new UnAuthorisedException("Booking not authorised");
        }
        if(hasBookingExpired(booking)){
            throw new IllegalStateException("Booking expired");
        }

        if(booking.getBookingStatus()!=BookingStatus.RESERVED){
            throw new IllegalStateException("Booking not under reserved state");
        }

        for(GuestDto guestDto:guestDtoList){
            Guest guest = modelMapper.map(guestDto,Guest.class);
            guest.setUser(getCurrentUser());
            guestRepository.save(guest);
            booking.getGuests().add(guest);

        }
        booking.setBookingStatus(BookingStatus.GUESTS_ADDED);
        booking = bookingRepository.save(booking);

        return modelMapper.map(booking,BookingDto.class);
    }

    @Override
    public String initiatePayments(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                () -> new ResourceNotFoundException("Booking not found")
        );
        User user = getCurrentUser();
        if(!user.equals(booking.getUser())){
            throw new UnAuthorisedException("Booking not authorised");
        }
        if(hasBookingExpired(booking)){
            throw new IllegalStateException("Booking expired");
        }
        String sessionUrl = checkoutService.getCheckoutSession(
                booking, frontendUrl+"/payment/success", frontendUrl+"/payment/failure");

        booking.setBookingStatus(BookingStatus.PAYMENTS_PENDING);
        return sessionUrl;
    }

    public boolean hasBookingExpired(Booking booking){
        return booking.getCreatedAt().plusMinutes(10).isBefore(LocalDateTime.now());
    }

    public User getCurrentUser(){

        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
