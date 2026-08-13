package com.health.healthcareappointmentbookingsystem.mapper;


import com.health.healthcareappointmentbookingsystem.dto.dtoRequest.RegisterRequest;
import com.health.healthcareappointmentbookingsystem.entity.Doctor;
import com.health.healthcareappointmentbookingsystem.entity.Patient;
import com.health.healthcareappointmentbookingsystem.entity.User;

public class UserMapper {

    public static Patient toPatientEntity(RegisterRequest request, User user) {
        Patient patient = new Patient();
        patient.setUser(user);
        patient.setFullName(request.getFullName());
        patient.setEmail(request.getEmail());
        patient.setPhoneNumber(request.getPhoneNumber());
        patient.setDateOfBirth(request.getDateOfBirth());
        return patient;
    }

    public static Doctor toDoctorEntity(RegisterRequest request, User user) {
        Doctor doctor = new Doctor();
        doctor.setUser(user);
        doctor.setFullName(request.getFullName());
        doctor.setEmail(request.getEmail());
        doctor.setSpecialization(request.getSpecialization());
        doctor.setLicenseNumber(request.getLicenseNumber());
        doctor.setHospital(request.getHospitalOrClinic());
        return doctor;
    }
}