package com.project.airbnb.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class StripeResponse {

    private String sessionId;
    private String message;
    private String status;
    private String sessionUrl;

}
