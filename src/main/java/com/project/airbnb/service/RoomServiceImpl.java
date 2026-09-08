package com.project.airbnb.service;

import com.project.airbnb.dto.RoomDto;
import com.project.airbnb.entity.Hotel;
import com.project.airbnb.entity.Room;
import com.project.airbnb.entity.User;
import com.project.airbnb.exception.ResourceNotFoundException;
import com.project.airbnb.exception.UnAuthorisedException;
import com.project.airbnb.repository.HotelRepository;
import com.project.airbnb.repository.RoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class RoomServiceImpl implements RoomService{

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final InventoryService inventoryService;
    private final ModelMapper modelMapper;

    @Override
    public RoomDto createNewRoom(Long hotelId, RoomDto roomDto) {

        log.info("Creating a room of type {} in hotel with Id : {}",roomDto.getType(), hotelId);
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(()-> new ResourceNotFoundException("No hotel found with Id : " +hotelId));
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if(user.equals(hotel.getOwner())){
            throw new UnAuthorisedException("This user doesn't own hotel");
        }
        Room room = modelMapper.map(roomDto, Room.class);
        room = roomRepository.save(room);

        //todo activate inventory when room is created
        if (hotel.getActive()) {
            inventoryService.initializeRoomForAYear(room);
        }
        return modelMapper.map(room, RoomDto.class);
    }

    @Override
    public List<RoomDto> getAllRoomsInHotel(Long hotelId) {
        log.info("getting a hotel with Id : {}", hotelId);
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(()-> new ResourceNotFoundException("No hotel found with Id : " +hotelId));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if(user.equals(hotel.getOwner())){
            throw new UnAuthorisedException("This user doesn't own hotel");
        }
        return roomRepository.findAll()
                .stream()
                .map((room -> modelMapper.map(room, RoomDto.class)))
                .toList();

        //todo verify this call
    }

    @Override
    public RoomDto getRoomById(Long roomId) {
        log.info("Getting the room with ID: {}", roomId);
        Room room = roomRepository
                .findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: "+roomId));
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if(user.equals(room.getHotel().getOwner())){
            throw new UnAuthorisedException("This user doesn't own this room in hotel");
        }
        return modelMapper.map(room, RoomDto.class);
    }

    @Transactional
    @Override
    public void deleteRoomById(Long roomId) {
        log.info("Deleting the room with ID: {}", roomId);
        Room room = roomRepository
                .findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: "+roomId));
        inventoryService.deleteFutureInventories(room);
        roomRepository.deleteById(roomId);
    }
}
