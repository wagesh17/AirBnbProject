package com.project.airbnb.service;

import com.project.airbnb.dto.HotelDto;
import com.project.airbnb.dto.HotelPriceDto;
import com.project.airbnb.dto.HotelSearchRequest;
import com.project.airbnb.entity.Room;
import org.springframework.data.domain.Page;

public interface InventoryService {
    void initializeRoomForAYear(Room room);

    void deleteFutureInventories(Room room);

    Page<HotelPriceDto> searchHotels(HotelSearchRequest hotelSearchRequest);

    void deleteAllInventories(Room room);
}
