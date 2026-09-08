package com.project.airbnb.dto;

import com.project.airbnb.entity.User;
import com.project.airbnb.entity.enums.Gender;
import lombok.Data;

@Data
public class GuestDto {
    private Long id;
    private User user;
    private String name;
    private Gender gender;
    private Integer age;
}