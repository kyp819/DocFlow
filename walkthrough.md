# Walkthrough: Android Java Patient App

We have built a fully featured, offline-first **Android Java Patient App** that integrates directly with your Spring Boot REST API. This application adheres strictly to standard **Android MVVM Architecture** combined with **Dagger Hilt Dependency Injection**, **Retrofit** for network calls, **Room DB** for offline caching, and **Material Design 3** aesthetics.

---

## 🛠️ Tech Stack & Key Files Created

The application is scaffolded in the [patient-android](file:///c:/Users/kyp81/OneDrive/Documentos/Clinic%20Appointment%20API/patient-android) directory of your workspace.

```
patient-android/
  ├── build.gradle (project)                     # Project-level plugins & settings
  ├── settings.gradle                            # Subproject bindings
  └── app/
      ├── build.gradle (app)                     # App-level dependencies (Room, Hilt, Retrofit, MD3)
      └── src/main/
          ├── AndroidManifest.xml                # Internet permission, activities & Hilt startup
          ├── java/com/clinic/patientapp/
          │   ├── PatientApplication.java        # Initialized with @HiltAndroidApp
          │   ├── di/
          │   │   ├── DatabaseModule.java        # Provides Room database & DAOs
          │   │   └── NetworkModule.java         # Provides OkHttpClient, JWT Interceptor & Retrofit
          │   ├── utils/
          │   │   ├── Constants.java             # Holds configuration parameters (defaulting to Emulator localhost)
          │   │   ├── TokenManager.java          # Secure JWT Token & User storage via EncryptedSharedPreferences
          │   │   ├── Resource.java              # MVVM State wrapper (LOADING, SUCCESS, ERROR)
          │   │   └── AppExecutors.java          # Thread executor wrapper for background Room writes in Java
          │   ├── data/
          │   │   ├── model/                     # Java models (LoginRequest, PatientResponse, AppointmentStatus, etc.)
          │   │   ├── local/                     # Room Entities (DoctorEntity, AppointmentEntity) & DAOs
          │   │   ├── remote/                    # Retrofit ApiService & AuthInterceptor
          │   │   └── repository/                # Repositories (Auth, Doctor, Appointment, Patient)
          │   └── ui/
          │       ├── auth/                      # LoginActivity, RegisterActivity & AuthViewModel
          │       ├── main/                      # MainActivity (hosts bottom navigation)
          │       ├── doctors/                   # DoctorsFragment, DoctorViewModel & DoctorAdapter
          │       ├── appointments/              # AppointmentsFragment, BookAppointmentActivity & AppointmentViewModel
          │       └── profile/                   # ProfileFragment & ProfileViewModel
          └── res/
              ├── layout/                        # MD3 Layout XML files (login, register, main, fragments, cards)
              ├── menu/                          # bottom_nav_menu.xml
              └── values/                        # Colors, themes (Material 3), and UI strings
```

---

## 🏗️ Core Architectural Details

### 1. Dependency Injection (Hilt)
Hilt injects the singletons across the data layer. In [DatabaseModule.java](file:///c:/Users/kyp81/OneDrive/Documentos/Clinic%20Appointment%20API/patient-android/app/src/main/java/com/clinic/patientapp/di/DatabaseModule.java) and [NetworkModule.java](file:///c:/Users/kyp81/OneDrive/Documentos/Clinic%20Appointment%20API/patient-android/app/src/main/java/com/clinic/patientapp/di/NetworkModule.java), we configure and provide the Room database, DAOs, Retrofit API endpoints, and authentication interceptors.

### 2. Network Auth Interceptor
The [AuthInterceptor.java](file:///c:/Users/kyp81/OneDrive/Documentos/Clinic%20Appointment%20API/patient-android/app/src/main/java/com/clinic/patientapp/data/remote/AuthInterceptor.java) intercepts outgoing Retrofit calls and dynamically injects the `Authorization: Bearer <token>` header if a token is present in our secure preference storage.

### 3. Secure JWT Storage
Tokens are kept safe using **EncryptedSharedPreferences** in [TokenManager.java](file:///c:/Users/kyp81/OneDrive/Documentos/Clinic%20Appointment%20API/patient-android/app/src/main/java/com/clinic/patientapp/utils/TokenManager.java). It automatically handles AES-256 key generation and value encryption via Android Keystore.

### 4. Offline Caching (Room DB)
When loading the doctors list or booked appointments queue, we implement a **Single Source of Truth** caching pattern:
- The UI binds to the Room `LiveData` feed (`getCachedAppointments()` and `getCachedDoctors()`).
- On screen loads or swipe-refresh triggers, the repository syncs fresh details from the backend, clears old cached rows, and writes the new entries to Room inside database transactions via [AppExecutors.java](file:///c:/Users/kyp81/OneDrive/Documentos/Clinic%20Appointment%20API/patient-android/app/src/main/java/com/clinic/patientapp/utils/AppExecutors.java).
- If offline, the UI continues displaying previously loaded data from Room without interrupting the user.

### 5. Local Doctor Schedule Validation
Before booking, [BookAppointmentActivity.java](file:///c:/Users/kyp81/OneDrive/Documentos/Clinic%20Appointment%20API/patient-android/app/src/main/java/com/clinic/patientapp/ui/appointments/BookAppointmentActivity.java) pulls the doctor's weekly active schedules. When you choose a Date and Time, it checks if the doctor is active on that day and if the selected slot falls strictly within their availability window.

---

## 🚀 How to Import and Run the Application

To demo this app on your phone or emulator:

### Step 1: Start your Spring Boot REST API
Make sure the backend server is running locally (either via Docker Compose or Maven):
```bash
# In the project root directory
mvn clean spring-boot:run
```
Swagger UI will be active at `http://localhost:8080/swagger-ui/index.html`.

### Step 2: Open in Android Studio
1. Launch Android Studio.
2. Select **Open**, navigate to your workspace folder, and select the [patient-android](file:///c:/Users/kyp81/OneDrive/Documentos/Clinic%20Appointment%20API/patient-android) directory.
3. Wait for Gradle to sync dependencies and compile Hilt/Room annotation processors.

### Step 3: Run on Emulator/Device
1. Start an Android Emulator (API level 26 or higher recommended).
2. Click **Run** in Android Studio to deploy the `app` module.
3. **Emulator Loopback**: The app defaults to `http://10.0.2.2:8080/api/` in [Constants.java](file:///c:/Users/kyp81/OneDrive/Documentos/Clinic%20Appointment%20API/patient-android/app/src/main/java/com/clinic/patientapp/utils/Constants.java) to connect directly to your local backend. To test against the live API, uncomment the live Render URL line in `Constants.java`.

---

## 🧪 Verification Walkthrough

### 1. Authentication Screen
- Launching the app takes you to `LoginActivity`.
- Click **Create one** to navigate to `RegisterActivity`. Create a patient user and hit **Sign Up**.
- You will be sent back to Login. Enter the email and password, click **Sign In**, and verify the success toast.

### 2. Complete Profile Setup (Demographics)
- On successful login, the app checks for your profile. If you have just registered, you will see the **Setup Demographic Profile** form.
- Fill in:
  - Gender: `Female`
  - Date of Birth: `1995-10-15`
  - Blood Group: `A+`
  - Address: `456 Pine Ave, Toronto`
  - Emergency Contact: `+1-555-0199`
- Tap **Complete Setup**. The app saves the profile to the Spring Boot DB and switches to your completed profile card.

### 3. Browse Doctors
- Tap the **Doctors** tab. You'll see seeded doctors from `data.sql` (e.g. Dr. Jane Smith, Dr. John Doe).
- Type "Jane" in the search box to verify instantaneous offline filtering.

### 4. Schedule & Book Appointments
- Tap on a doctor (e.g., Dr. Jane Smith).
- The scheduling screen displays the doctor's weekly active slots.
- Click **Select Date** and choose a future date (e.g., a Monday).
- Click **Select Time** and pick a hour (e.g., `10:00 AM`).
- Add notes: `Fever and throat irritation.`
- Click **Confirm Booking**. The appointment is saved on the backend and cached in Room. You are returned to the doctors tab.

### 5. Offline Caching Verification
- Go to the **Bookings** tab to see your new booking.
- Disable internet connection on the emulator (turn on Airplane mode).
- Close and reopen the app.
- Go directly to the **Bookings** tab: the appointment is retrieved instantly from the local **Room DB cache**, proving full offline support!
- Reconnect to the internet and swipe down to sync fresh data.
- Click **Cancel** on the booking. Verify the status updates to `CANCELLED` locally and on the server.
