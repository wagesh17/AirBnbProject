package com.project.airbnb.service;

import com.project.airbnb.dto.HotelDto;
import com.project.airbnb.dto.HotelInfoDto;
import org.jspecify.annotations.Nullable;

public interface HotelService {
    HotelDto createNewHotel(HotelDto hotelDto);

    HotelDto getHotelById(Long id);

    HotelDto updateHotelById(Long id, HotelDto hotelDto);

    void deleteHotelById(Long id);

    void activateHotel(Long id);

    @Nullable HotelInfoDto getHotelInfoById(Long hotelId);
}
