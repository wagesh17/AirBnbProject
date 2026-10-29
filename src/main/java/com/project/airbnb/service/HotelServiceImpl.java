package com.project.airbnb.service;

import com.project.airbnb.dto.HotelDto;
import com.project.airbnb.dto.HotelInfoDto;
import com.project.airbnb.dto.RoomDto;
import com.project.airbnb.entity.Hotel;
import com.project.airbnb.entity.Room;
import com.project.airbnb.entity.User;
import com.project.airbnb.exception.ResourceNotFoundException;
import com.project.airbnb.exception.UnAuthorisedException;
import com.project.airbnb.repository.HotelRepository;
import com.project.airbnb.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;
    private final RoomRepository roomRepository;
    private final InventoryService inventoryService;

    @Override
    public HotelDto createNewHotel(HotelDto hotelDto) {
        log.info("Creating a new hotel by name {}", hotelDto.getName());
        Hotel hotel = modelMapper.map(hotelDto, Hotel.class);
        hotel.setActive(false);

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        hotel = hotelRepository.save(hotel);
        log.info("Hotel created with id {}",hotel.getId());
        return modelMapper.map(hotel, HotelDto.class);
    }

    @Override
    public HotelDto getHotelById(Long id) {
        log.info("Getting hotel with id : {}",id);
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id : "+id));
        User user = (User) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();

        assert user != null;
        if(!user.equals(hotel.getOwner())){
            throw new UnAuthorisedException("This user doesn't own hotel");
        }
        return modelMapper.map(hotel, HotelDto.class);
    }

    @Override
    public HotelDto updateHotelById(Long id, HotelDto hotelDto) {
        log.info("Updating hotel with id : {}",id);
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id : "+id));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if(user.equals(hotel.getOwner())){
            throw new UnAuthorisedException("This user doesn't own hotel");
        }
        modelMapper.map(hotelDto, hotel);
        hotel.setId(id);
        hotelRepository.save(hotel);
        return modelMapper.map(hotel, HotelDto.class);
    }

    @Override
    @Transactional
    public void deleteHotelById(Long id) {
        log.info("Deleting hotel with id : {}",id);
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id : "+id));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if(user.equals(hotel.getOwner())){
            throw new UnAuthorisedException("This user doesn't own hotel");
        }

        hotelRepository.deleteById(id);
        //todo : write logic for deleting the inventory
        for(Room room: hotel.getRooms()) {
            inventoryService.deleteAllInventories(room);
            roomRepository.deleteById(room.getId());
        }
        hotelRepository.deleteById(id);
    }

    @Override
    public void activateHotel(Long id) {
        log.info("Activating hotel with id : {}",id);
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id : "+id));
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if(user.equals(hotel.getOwner())){
            throw new UnAuthorisedException("This user doesn't own hotel");
        }

        hotel.setActive(true);
        //todo : write logic for creating inventory
        for(Room room: hotel.getRooms()) {
            inventoryService.initializeRoomForAYear(room);
        }
    }

    @Override
    public HotelInfoDto getHotelInfoById(Long hotelId) {
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID: "+hotelId));

        List<RoomDto> rooms = hotel.getRooms()
                .stream()
                .map((element) -> modelMapper.map(element, RoomDto.class))
                .toList();

        return new HotelInfoDto(modelMapper.map(hotel, HotelDto.class), rooms);
    }
}
