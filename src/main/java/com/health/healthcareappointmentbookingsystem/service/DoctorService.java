package com.health.healthcareappointmentbookingsystem.service;

import com.health.healthcareappointmentbookingsystem.dto.dtoResponse.DoctorResponse;
import com.health.healthcareappointmentbookingsystem.entity.Doctor;
import com.health.healthcareappointmentbookingsystem.entity.Specialization;
import com.health.healthcareappointmentbookingsystem.mapper.DoctorMapper;
import com.health.healthcareappointmentbookingsystem.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;

    public List<DoctorResponse> getDoctors(Specialization specialization) {
        if (specialization != null) {
            return getDoctorsBySpecialization(specialization);
        }
        return getAllDoctors();
    }

    public List<DoctorResponse> getAllDoctors() {
        return doctorRepository.findAll().stream()
                .map(DoctorMapper::toResponse)
                .toList();
    }

    public List<DoctorResponse> getDoctorsBySpecialization(Specialization specialization) {
        return doctorRepository.findBySpecialization(specialization).stream()
                .map(DoctorMapper::toResponse)
                .toList();
    }

    public DoctorResponse getDoctorById(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Doctor not found with id: " + id));
        return DoctorMapper.toResponse(doctor);
    }

    public void deleteDoctor(Long id) {
        if (!doctorRepository.existsById(id)) {
            throw new IllegalStateException("Doctor not found with id: " + id);
        }
        doctorRepository.deleteById(id);
    }
}