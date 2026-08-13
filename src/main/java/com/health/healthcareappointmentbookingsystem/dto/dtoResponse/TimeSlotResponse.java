package com.health.healthcareappointmentbookingsystem.dto.dtoResponse;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlotResponse {
    private LocalDateTime slotStart;
    private LocalDateTime slotEnd;
}