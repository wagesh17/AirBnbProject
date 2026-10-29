package com.project.airbnb.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.airbnb.dto.HotelDto;
import com.project.airbnb.security.JWTService;
import com.project.airbnb.service.HotelService;
import com.project.airbnb.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HotelController.class)
public class HotelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private HotelService hotelService;

    @MockitoBean
    private JWTService jwtService;

    @MockitoBean
    private UserService userService;

    @Test
    void createNewHotel_success() throws Exception {
        HotelDto hotelDto = new HotelDto();
        hotelDto.setName("Test Hotel");

        when(hotelService.createNewHotel(any(HotelDto.class)))
                .thenReturn(hotelDto);

        mockMvc.perform(post("/admin/hotels")
                .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(hotelDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Hotel"));
    }

    @Test
    void getHotelById_success() throws Exception {
        HotelDto hotelDto = new HotelDto();
        hotelDto.setId(1L);
        hotelDto.setName("Test Hotel");

        when(hotelService.getHotelById(1L)).thenReturn(hotelDto);

        mockMvc.perform(get("/admin/hotels/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Hotel"));
    }

    @Test
    void updateHotelById_success() throws Exception {
        HotelDto hotelDto = new HotelDto();
        hotelDto.setId(1L);
        hotelDto.setName("Updated Hotel");

        when(hotelService.updateHotelById(any(Long.class), any(HotelDto.class)))
                .thenReturn(hotelDto);

        mockMvc.perform(put("/admin/hotels/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(hotelDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Updated Hotel"));
    }

    @Test
    void deleteHotelById_success() throws Exception {
        mockMvc.perform(delete("/admin/hotels/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void activateHotel_success() throws Exception {
        mockMvc.perform(patch("/admin/hotels/1"))
                .andExpect(status().isNoContent());
    }
}
