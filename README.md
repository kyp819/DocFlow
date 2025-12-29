# Clinic Appointment Booking REST API

A production-ready Spring Boot 3 REST API for booking and managing clinic appointments, built using enterprise best practices.

## 🔗 Deployed Live API Demo
* **Interactive API Documentation (Swagger UI):** [https://docflow-p3f1.onrender.com/swagger-ui/index.html](https://docflow-p3f1.onrender.com/swagger-ui/index.html)


## Tech Stack
* **Language:** Java 17
* **Framework:** Spring Boot 3.2.4
* **Security:** Spring Security (JWT Token authentication, BCrypt encryption, Role-based authorization)
* **ORM:** Spring Data JPA + Hibernate
* **Database:** PostgreSQL
* **API Documentation:** SpringDoc OpenAPI / Swagger UI
* **Utility:** Lombok
* **Docker:** Dockerfile and Docker Compose configurations

---

## Folder Structure
```
clinic-appointment-api/
 src/
    main/
       java/com/clinic/
          config/          # SecurityConfig, SwaggerConfig, ApplicationConfig
          controller/      # REST API Controllers (endpoints)
          dto/             # Data Transfer Objects
          entity/          # JPA Hibernate Entities
          repository/      # JPA Spring Data Repositories
          service/         # Service layer Interfaces
          service/impl/    # Service implementation classes
          security/        # JWT components, CustomUserDetailsService, UserPrincipal
          exception/       # GlobalExceptionHandler, Custom Exceptions
          mapper/          # DtoMapper (Entity <=> DTO converter)
          ClinicAppointmentApplication.java
       resources/
           application.yml  # Application properties
           data.sql         # Seed data (Users, Doctor, Patient, Availabilities)
    test/                    # Unit/Integration Tests
 Dockerfile                   # Docker packaging
 docker-compose.yml           # Database + Application multi-container orchestration
 pom.xml                      # Maven build settings
 clinic_appointment_api.postman_collection.json # Postman Collection
```

---

## Authentication Roles & Seed Credentials

Authentication utilizes JWT tokens. The database is preloaded with three mock users (all passwords are `password123`):

1. **ADMIN Role:** `admin@clinic.com` (Full control over profiles, schedules, and bookings)
2. **DOCTOR Role:** `doctor@clinic.com` (Can CRUD own availabilities, cancel/reschedule own appointments, view today's queue)
3. **PATIENT Role:** `patient@clinic.com` (Can CRUD own patient profile, view doctors, book appointments, cancel/reschedule own appointments)

---

## Key Features & Endpoints

### 🔐 Authentication
* `POST /api/auth/register` - Create a new user (role ADMIN, DOCTOR, or PATIENT)
* `POST /api/auth/login` - Authenticate user credentials and receive a Bearer JWT Token

### 🩺 Doctor API
* `POST /api/doctors` - Setup doctor specialization, fee, hospital, and bio profile (DOCTOR, ADMIN)
* `PUT /api/doctors/{id}` - Edit doctor profile (DOCTOR owner, ADMIN)
* `GET /api/doctors/{id}` - Fetch doctor by profile ID (All authenticated roles)
* `GET /api/doctors` - Search and list doctors (Supports filtering by `specialization` and searching by `name` query params)

### 👤 Patient API
* `POST /api/patients` - Create patient demographic and emergency profile (PATIENT, ADMIN)
* `PUT /api/patients/{id}` - Edit patient details (PATIENT owner, ADMIN)
* `GET /api/patients/me` - Fetch profile details of logged-in Patient (PATIENT)

### 🗓 Availability API
* `POST /api/availabilities` - Add a weekly availability schedule slot (DOCTOR)
* `PUT /api/availabilities/{id}` - Edit availability details (DOCTOR owner)
* `DELETE /api/availabilities/{id}` - Delete availability (DOCTOR owner)
* `GET /api/availabilities/doctor/{doctorId}/active` - Find schedule for booking appointments (All authenticated roles)

### 📅 Appointment API
* `POST /api/appointments` - Book appointment. Enforces non-overlapping, inside availability, and future date checks (PATIENT)
* `PUT /api/appointments/{id}/cancel` - Cancel appointment status (Owner PATIENT, assigned DOCTOR, ADMIN)
* `PUT /api/appointments/{id}/reschedule` - Reschedule appointment. Validates new slot overlaps (Owner PATIENT, assigned DOCTOR, ADMIN)
* `GET /api/appointments/me` - Get logged-in user's personalized queue (All roles)
* `GET /api/appointments/today` - Today's appointment timeline queue (DOCTOR, ADMIN)

---

## Business Logic Rules Enforced
1. **No Overlapping Bookings:** Patients cannot have multiple appointments at the same time. Doctors cannot have concurrent appointments.
2. **In Future Only:** Appointments cannot be booked in the past.
3. **Doctor Availability Match:** Appointments must fall strictly within the doctor's active availability day/time ranges.
4. **Error Handling:** Triggers an `HTTP 409 Conflict` message with precise reasons for any scheduling overlaps or conflicts.

---

## How to Run the Project

### Option A: Using Docker Compose (Recommended)
This runs both PostgreSQL and the Spring Boot application inside containers.
1. Run the following command in the project root:
   ```bash
   docker compose up --build -d
   ```
2. The REST API will be available at `http://localhost:8080`.
3. Open Swagger UI to test the endpoints at: `http://localhost:8080/swagger-ui.html`.

### Option B: Running Locally with Maven
1. Ensure you have a PostgreSQL database running locally.
2. Update the database URL, username, and password in `src/main/resources/application.yml`.
3. Run the following command to compile and launch the application:
   ```bash
   mvn clean spring-boot:run
   ```

### Option C: Running the ReactJS Frontend UI
We have created a complete ReactJS + TypeScript frontend dashboard connected to the Spring Boot REST API.
1. Open a new terminal window and navigate to the `frontend` folder:
   ```bash
   cd frontend
   ```
2. Run the development server (dependencies are pre-installed):
   ```bash
   npm run dev
   ```
3. Open `http://localhost:5173` in your browser. You can log in, view doctors, manage availabilities, and schedule appointments instantly.

---

## Testing with Postman
1. Import `clinic_appointment_api.postman_collection.json` into Postman.
2. Set the `baseUrl` variable to `http://localhost:8080` (pre-configured).
3. Execute the login requests (e.g. `Login (Patient)`). The test script inside the collection automatically sets the `jwt_token` environment variable on success, authorizing subsequent requests.

---

## 🐳 The Role of Docker
In this project, Docker serves multiple critical roles for portability, consistency, and professional deployment:
1. **Consistency ("Works on My Machine" Guarantee):** Docker encapsulates the exact OpenJDK 17 runtime, Maven dependency cache, database driver versions, and compiled Spring Boot binary into a lightweight container image. This eliminates differences between development, testing, and production OS environments.
2. **PostgreSQL Orchestration:** Through `docker-compose.yml`, Docker allows developers to spin up a pre-configured PostgreSQL server (`clinic-db`) in seconds without needing a global database installation.
3. **Cloud Native Portability:** Modern hosting platforms (Render, AWS ECS, Google Cloud Run) leverage Docker containers directly. Because our project is dockerized, it is ready to run on any of these platforms without modifications.

---

## 🚀 Cloud Deployment on Render
Deploying this API to Render (free tier) is pre-configured using **Infrastructure as Code (IaC)**.
1. Create a free account on [Render](https://render.com/).
2. Click **New** > **Blueprint** (which reads the `render.yaml` configuration).
3. Connect your GitHub repository containing this codebase.
4. Render will parse `render.yaml`, provision a secure cloud PostgreSQL instance (`clinic-db`), automatically build the Java application from the `Dockerfile`, inject database credentials into the Spring Boot environment, and spin up the live service.
5. Once deployed, Render provides a public URL for the service. You can access the live interactive API documentation (Swagger UI) directly at: [https://docflow-p3f1.onrender.com/swagger-ui/index.html](https://docflow-p3f1.onrender.com/swagger-ui/index.html).

---

## 🛠 CI/CD with GitHub Actions
The project includes a pre-configured GitHub Actions pipeline (`.github/workflows/build.yml`). 
Whenever code is pushed or a pull request is created on the `master`/`main` branch:
* A runner boots up on GitHub's cloud.
* JDK 17 (Temurin distribution) is installed.
* The Spring Boot application is compiled and packaged via Maven.
* The `Dockerfile` image is built to verify containerization integrity.
This guarantees that your repository remains deployable and serves as a stellar demonstration of DevOps maturity to recruiters.

