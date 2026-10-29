package com.project.airbnb.service;

import com.project.airbnb.dto.HotelDto;
import com.project.airbnb.entity.Hotel;
import com.project.airbnb.entity.User;
import com.project.airbnb.exception.ResourceNotFoundException;
import com.project.airbnb.exception.UnAuthorisedException;
import com.project.airbnb.repository.HotelRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HotelServiceImplTest {

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private HotelServiceImpl hotelService;

    private User owner;
    private Hotel hotel;
    private User anotherUser;
    private HotelDto hotelDto;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);

        anotherUser = new User();
        anotherUser.setId(2L);

        hotel = new Hotel();
        hotel.setId(10L);
        hotel.setOwner(owner);

        hotelDto = new HotelDto();
        hotelDto.setId(10L);

        setAuthenticatedUser(owner);
    }

    private void setAuthenticatedUser(User user) {
        Authentication auth =
                new UsernamePasswordAuthenticationToken(
                        user, null, Collections.emptyList());

        SecurityContext context =
                SecurityContextHolder.createEmptyContext();

        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createNewHotel_success() {

        HotelDto newHotelDto = new HotelDto();
        newHotelDto.setName("Test Hotel");

        Hotel newHotel = new Hotel();
        newHotel.setName("Test Hotel");

        when(modelMapper.map(newHotelDto, Hotel.class)).thenReturn(newHotel);
        when(hotelRepository.save(newHotel)).thenReturn(newHotel);
        when(modelMapper.map(newHotel, HotelDto.class)).thenReturn(newHotelDto);

        HotelDto result = hotelService.createNewHotel(newHotelDto);

        assertNotNull(result);
        assertEquals("Test Hotel", result.getName());
       // assertFalse(result.getActive());
    }

    @Test
    void getHotelById_success() {
        when(hotelRepository.findById(10L))
                .thenReturn(Optional.of(hotel));
        when(modelMapper.map(hotel, HotelDto.class))
                .thenReturn(hotelDto);

        HotelDto result = hotelService.getHotelById(10L);

        assertNotNull(result);
        assertEquals(10L, result.getId());
    }

    @Test
    void getHotelById_notFound() {
        when(hotelRepository.findById(10L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> hotelService.getHotelById(10L));
    }

    @Test
    void getHotelById_unauthorized() {
        hotel.setOwner(anotherUser);

        when(hotelRepository.findById(10L))
                .thenReturn(Optional.of(hotel));

        assertThrows(UnAuthorisedException.class,
                () -> hotelService.getHotelById(10L));
    }
}
