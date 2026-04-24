-- Insert Users
INSERT INTO users (full_name, email, password, phone, role, enabled, created_at)
VALUES 
('Admin User', 'admin@clinic.com', '$2a$10$dXJ3ADWyyTXMn5EEWP5odO.5WG69670pk70pyeQG./sM5vKA.tCWS', '+1234567890', 'ADMIN', true, CURRENT_TIMESTAMP),
('Dr. John Doe', 'doctor@clinic.com', '$2a$10$dXJ3ADWyyTXMn5EEWP5odO.5WG69670pk70pyeQG./sM5vKA.tCWS', '+1987654321', 'DOCTOR', true, CURRENT_TIMESTAMP),
('Jane Smith', 'patient@clinic.com', '$2a$10$dXJ3ADWyyTXMn5EEWP5odO.5WG69670pk70pyeQG./sM5vKA.tCWS', '+1555555555', 'PATIENT', true, CURRENT_TIMESTAMP)
ON CONFLICT (email) DO NOTHING;

-- Insert Doctor Profile
INSERT INTO doctors (specialization, qualification, experience, consultation_fee, hospital_name, bio, user_id)
SELECT 'Cardiology', 'MD - Cardiology', 12, 150.00, 'General City Hospital', 'Experienced cardiologist specializing in cardiovascular health.', u.id
FROM users u
WHERE u.email = 'doctor@clinic.com'
AND NOT EXISTS (SELECT 1 FROM doctors WHERE user_id = u.id);

-- Insert Patient Profile
INSERT INTO patients (gender, date_of_birth, address, blood_group, emergency_contact, user_id)
SELECT 'FEMALE', '1992-08-15', '456 Elm St, Metropia', 'O+', '+1555123456', u.id
FROM users u
WHERE u.email = 'patient@clinic.com'
AND NOT EXISTS (SELECT 1 FROM patients WHERE user_id = u.id);

-- Insert Doctor Availabilities
INSERT INTO doctor_availabilities (doctor_id, day_of_week, start_time, end_time, active)
SELECT d.id, 'MONDAY', '09:00:00', '17:00:00', true
FROM doctors d JOIN users u ON d.user_id = u.id
WHERE u.email = 'doctor@clinic.com'
AND NOT EXISTS (SELECT 1 FROM doctor_availabilities WHERE doctor_id = d.id AND day_of_week = 'MONDAY');

INSERT INTO doctor_availabilities (doctor_id, day_of_week, start_time, end_time, active)
SELECT d.id, 'WEDNESDAY', '09:00:00', '17:00:00', true
FROM doctors d JOIN users u ON d.user_id = u.id
WHERE u.email = 'doctor@clinic.com'
AND NOT EXISTS (SELECT 1 FROM doctor_availabilities WHERE doctor_id = d.id AND day_of_week = 'WEDNESDAY');

INSERT INTO doctor_availabilities (doctor_id, day_of_week, start_time, end_time, active)
SELECT d.id, 'FRIDAY', '09:00:00', '13:00:00', true
FROM doctors d JOIN users u ON d.user_id = u.id
WHERE u.email = 'doctor@clinic.com'
AND NOT EXISTS (SELECT 1 FROM doctor_availabilities WHERE doctor_id = d.id AND day_of_week = 'FRIDAY');
