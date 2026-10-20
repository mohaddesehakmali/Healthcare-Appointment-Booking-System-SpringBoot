package com.health.healthcareappointmentbookingsystem.service;

import com.health.healthcareappointmentbookingsystem.dto.dtoRequest.AppointmentRequest;
import com.health.healthcareappointmentbookingsystem.entity.*;
import com.health.healthcareappointmentbookingsystem.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {
    @Mock AppointmentRepository appointmentRepository;
    @Mock PatientRepository patientRepository;
    @Mock DoctorRepository doctorRepository;
    @Mock DoctorAvailabilityRepository availabilityRepository;
    @InjectMocks AppointmentService service;

    User patientUser;
    Patient patient;
    Doctor doctor;
    AppointmentRequest request;

    @BeforeEach void setup() {
        patientUser = User.builder().id(1L).email("p@test.com").role(Role.PATIENT).build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(patientUser, null, patientUser.getAuthorities()));
        patient = new Patient();
        patient.setId(10L);
        patient.setUser(patientUser);
        patient.setFullName("Jane");
        patient.setDateOfBirth(LocalDate.now().minusYears(25));
        User doctorUser = User.builder().id(2L).email("d@test.com").role(Role.DOCTOR).build();
        doctor = new Doctor();
        doctor.setId(20L);
        doctor.setUser(doctorUser);
        doctor.setFullName("Doctor");
        doctor.setSpecialization(Specialization.CARDIOLOGY);
        request = new AppointmentRequest();
        request.setDoctorId(20L);
        request.setAppointmentDateTime(LocalDate.now().plusDays(1).atTime(10, 0));
        request.setReasonForVisit("Checkup");
    }

    @AfterEach void cleanup() { SecurityContextHolder.clearContext(); }

    @Test void underagePatientCannotBook() {
        patient.setDateOfBirth(LocalDate.now().minusYears(10));
        when(patientRepository.findByUserId(1L)).thenReturn(Optional.of(patient));
        assertThatThrownBy(() -> service.bookAppointment(request))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("under 13");
    }

    @Test void unavailableSlotCannotBeBooked() {
        when(patientRepository.findByUserId(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(doctor));
        when(availabilityRepository.findByDoctorIdAndDayOfWeek(eq(20L), any())).thenReturn(List.of());
        assertThatThrownBy(() -> service.bookAppointment(request))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("not available");
    }

    @Test void bookedSlotCannotBeBookedTwice() {
        availableDoctor();
        when(appointmentRepository.existsByDoctorIdAndAppointmentDateTimeAndStatusIn(eq(20L),any(),anyList()))
                .thenReturn(true);
        assertThatThrownBy(() -> service.bookAppointment(request))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("already booked");
    }

    @Test void validBookingCreatesPendingAppointment() {
        availableDoctor();
        when(appointmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        var result = service.bookAppointment(request);
        assertThat(result.getStatus()).isEqualTo(AppointmentStatus.PENDING);
        assertThat(result.getPatientName()).isEqualTo("Jane");
    }

    @Test void unrelatedDoctorCannotConfirm() {
        User stranger = User.builder().id(3L).email("other@test.com").role(Role.DOCTOR).build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(stranger,null,stranger.getAuthorities()));
        Appointment a = pendingAppointment();
        when(appointmentRepository.findById(99L)).thenReturn(Optional.of(a));
        assertThatThrownBy(() -> service.confirmAppointment(99L)).isInstanceOf(AccessDeniedException.class);
    }

    @Test void patientCannotCancelSomeoneElsesAppointment() {
        Appointment a = pendingAppointment();
        User other = User.builder().id(55L).email("other@test.com").role(Role.PATIENT).build();
        a.getPatient().setUser(other);
        when(appointmentRepository.findById(99L)).thenReturn(Optional.of(a));
        assertThatThrownBy(() -> service.cancelAppointment(99L)).isInstanceOf(AccessDeniedException.class);
    }

    @Test void ownerCanCancelPendingAppointment() {
        Appointment a = pendingAppointment();
        when(appointmentRepository.findById(99L)).thenReturn(Optional.of(a));
        when(appointmentRepository.save(a)).thenReturn(a);
        assertThat(service.cancelAppointment(99L).getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
    }

    private Appointment pendingAppointment() {
        Appointment a = new Appointment();
        a.setId(99L); a.setPatient(patient); a.setDoctor(doctor);
        a.setAppointmentDateTime(request.getAppointmentDateTime());
        a.setStatus(AppointmentStatus.PENDING);
        return a;
    }

    private void availableDoctor() {
        when(patientRepository.findByUserId(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(doctor));
        DoctorAvailability slot = new DoctorAvailability();
        slot.setStartTime(LocalTime.of(9,0));
        slot.setEndTime(LocalTime.of(17,0));
        when(availabilityRepository.findByDoctorIdAndDayOfWeek(eq(20L),any()))
                .thenReturn(List.of(slot));
    }
}
