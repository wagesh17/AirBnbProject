package com.project.airbnb.service;

import com.project.airbnb.dto.RoomDto;
import com.project.airbnb.entity.Room;

import java.util.List;

public interface RoomService {

    public RoomDto createNewRoom(Long hotelId, RoomDto roomDto);

    public List<RoomDto> getAllRoomsInHotel(Long hotelId);

    public RoomDto getRoomById(Long roomId);

    public void deleteRoomById(Long roomId);
}
