HealthConnect 🏥

A Spring Boot REST API for healthcare appointment management — built with JWT authentication, role-based authorization, a doctor confirmation workflow, and an automatic available-slot calculator.

Patients can register, browse doctors by specialization, and book appointments. Doctors can define their weekly working hours, review pending requests, and confirm or reject them. Every part of the system is protected by role-based access control (ADMIN / DOCTOR / PATIENT).

✨ Features
🔐 JWT Authentication — stateless login, no server-side sessions
👥 Role-Based Access Control — ADMIN, DOCTOR, PATIENT, enforced at the endpoint level
🧑‍⚕️ Doctor & Patient Profiles — separate from login credentials
📅 Appointment Booking — with automatic PENDING status on creation
✅ Confirmation Workflow — doctors confirm, reject, or cancel appointments
🕐 Weekly Availability — doctors define recurring working hours
🔍 Free Slot Calculator — computes open 30-minute slots for a given date, excluding already-booked times
🚫 Minimum Age Rule — patients under 13 cannot book independently
🗄️ PostgreSQL + Spring Data JPA
⚠️ Centralized Exception Handling — clean, consistent JSON error responses
✔️ Bean Validation — request-level validation with readable error messages
🧪 Unit Tests — JUnit 5 + Mockito, covering the core business rules
🛠️ Tech Stack
Category	Technology
Language	Java 21
Framework	Spring Boot
Security	Spring Security, JWT (jjwt)
Persistence	Spring Data JPA, Hibernate
Database	PostgreSQL
Build Tool	Maven
Utilities	Lombok
Testing	JUnit 5, Mockito, AssertJ
🏗️ Architecture
Controller → Service → Repository → Database
                ↓
             Mapper (Entity ↔ DTO)

Layered by responsibility:

Layer	Responsibility
entity	Database table structure
repository	Database access (Spring Data JPA)
dto	Shape of API input/output
mapper	Converts between Entity and DTO
security	JWT generation, validation, and filtering
service	All business logic
controller	HTTP endpoints
exception	Centralized, consistent error responses
👤 Roles
ADMIN
DOCTOR
PATIENT

Admin accounts cannot self-register — they must be created separately.

📐 Core Business Rules
A newly booked appointment always starts as PENDING
Only the assigned doctor can CONFIRM or REJECT a pending appointment
Patients, doctors, or admins can CANCEL an appointment
Patients under 13 years old cannot book an appointment on their own
Available slots are computed from the doctor's weekly working hours, minus any PENDING or CONFIRMED appointments already on that date
🚀 Getting Started
Prerequisites
Java 21+
Maven 3.8+
PostgreSQL 14+
1. Create the database
sql
CREATE DATABASE healthconnect;
2. Configure application.properties
properties
spring.datasource.url=jdbc:postgresql://localhost:5432/healthconnect
spring.datasource.username=postgres
spring.datasource.password=YOUR_PASSWORD

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

jwt.secret=change-this-to-a-long-random-secret-in-production
jwt.expiration-ms=86400000
3. Run
bash
mvn spring-boot:run

The API will be available at http://localhost:8080.

4. Run the tests
bash
mvn test
🔑 Authentication

Register or log in to receive a JWT, then include it in the Authorization header for every protected request:

Authorization: Bearer <your-token>
📡 API Overview
Method	Endpoint	Role	Description
POST	/api/auth/register	Public	Register as PATIENT or DOCTOR
POST	/api/auth/login	Public	Log in, receive JWT
GET	/api/doctors	Any	List doctors, optionally filter by specialization
GET	/api/doctors/{id}	Any	Get one doctor
POST	/api/availability	DOCTOR	Set weekly working hours
GET	/api/availability/{doctorId}/slots?date=YYYY-MM-DD	Any	Get open slots for a date
POST	/api/appointments	PATIENT	Book an appointment
GET	/api/appointments/me/patient	PATIENT	My appointments
GET	/api/appointments/me/doctor	DOCTOR	My appointments
GET	/api/appointments/me/doctor/pending	DOCTOR	My pending requests
PATCH	/api/appointments/{id}/confirm	DOCTOR	Confirm a pending appointment
PATCH	/api/appointments/{id}/reject	DOCTOR	Reject a pending appointment
PATCH	/api/appointments/{id}/cancel	PATIENT/DOCTOR/ADMIN	Cancel an appointment
Example: register a patient
bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Sara Ahmadi",
    "email": "sara@example.com",
    "password": "password123",
    "role": "PATIENT",
    "dateOfBirth": "2000-01-01"
  }'
Example: book an appointment
bash
curl -X POST http://localhost:8080/api/appointments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <PATIENT_TOKEN>" \
  -d '{
    "doctorId": 1,
    "appointmentDateTime": "2026-09-01T10:00:00",
    "reasonForVisit": "Routine checkup"
  }'
🧪 Testing

Unit tests cover the core business logic without touching a real database:

AuthServiceTest — registration rules (duplicate email, admin self-registration, happy path)
AppointmentServiceTest — the minimum-age rule, booking success/failure, doctor-not-found handling
bash
mvn test
📄 License

Open source — feel free to adapt for learning or your own projects.

🤝 Contributing

Issues and pull requests are welcome. This project was built incrementally, layer by layer, as a learning exercise — contributions that keep that spirit (clear structure, explained decisions) are especially appreciated.
