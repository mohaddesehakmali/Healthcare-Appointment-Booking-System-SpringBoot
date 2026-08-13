package com.health.healthcareappointmentbookingsystem.mapper;

import com.health.healthcareappointmentbookingsystem.dto.dtoResponse.AppointmentResponse;
import com.health.healthcareappointmentbookingsystem.entity.Appointment;

public class AppointmentMapper {

    public static AppointmentResponse toResponse(Appointment appointment) {
        return AppointmentResponse.builder()
                .id(appointment.getId())
                .patientName(appointment.getPatient().getFullName())
                .doctorName(appointment.getDoctor().getFullName())
                .specialization(appointment.getDoctor().getSpecialization())
                .appointmentDateTime(appointment.getAppointmentDateTime())
                .status(appointment.getStatus())
                .reasonForVisit(appointment.getReasonForVisit())
                .notes(appointment.getNotes())
                .build();
    }
}