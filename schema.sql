-- =============================================================================
-- EASY REGISTRATION — ONLINE SEMESTER REGISTRATION & APPROVAL MANAGEMENT SYSTEM
-- MySQL Database Schema and Seed Data Script
-- =============================================================================

CREATE DATABASE IF NOT EXISTS `easyregistration` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `easyregistration`;

-- 1. Users Table
CREATE TABLE IF NOT EXISTS `users` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `full_name` VARCHAR(100) NOT NULL,
    `email` VARCHAR(100),
    `phone` VARCHAR(20),
    `role` VARCHAR(30) NOT NULL,
    `department` VARCHAR(100),
    `batch_year` VARCHAR(20),
    `current_semester` INT,
    `roll_number` VARCHAR(50),
    `transport_opted` BOOLEAN DEFAULT FALSE,
    `bus_route` VARCHAR(100),
    `active` BOOLEAN DEFAULT TRUE,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 2. Semester Registration Applications Table
CREATE TABLE IF NOT EXISTS `registration_applications` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `application_number` VARCHAR(64) NOT NULL UNIQUE,
    `student_id` BIGINT NOT NULL,
    `academic_year` VARCHAR(30) NOT NULL,
    `target_semester` INT NOT NULL,
    `department` VARCHAR(100) NOT NULL,
    `cgpa` DOUBLE,
    `backlogs_count` INT DEFAULT 0,
    `transport_opted` BOOLEAN DEFAULT FALSE,
    `bus_route` VARCHAR(100),
    `bus_stop` VARCHAR(100),
    `fee_receipt_number` VARCHAR(80),
    `fee_amount_paid` DOUBLE,
    `fee_payment_date` DATE,
    `transport_fee_receipt` VARCHAR(80),
    `transport_fee_amount` DOUBLE,
    `selected_subjects` TEXT,
    `status` VARCHAR(35) NOT NULL,
    `accounts_cleared` BOOLEAN DEFAULT FALSE,
    `accounts_cleared_at` DATETIME,
    `accounts_approved_by` VARCHAR(100),
    `accounts_remarks` VARCHAR(255),
    `transport_cleared` BOOLEAN DEFAULT FALSE,
    `transport_cleared_at` DATETIME,
    `transport_approved_by` VARCHAR(100),
    `transport_remarks` VARCHAR(255),
    `lab_cleared` BOOLEAN DEFAULT FALSE,
    `lab_cleared_at` DATETIME,
    `lab_approved_by` VARCHAR(100),
    `lab_remarks` VARCHAR(255),
    `hod_approved` BOOLEAN DEFAULT FALSE,
    `hod_approved_at` DATETIME,
    `hod_approved_by` VARCHAR(100),
    `hod_remarks` VARCHAR(255),
    `rejection_reason` VARCHAR(255),
    `rejection_stage` VARCHAR(30),
    `submitted_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `completed_at` DATETIME,
    FOREIGN KEY (`student_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
);

-- 3. Approval Audit Logs Table
CREATE TABLE IF NOT EXISTS `approval_audit_logs` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `application_id` BIGINT NOT NULL,
    `action_by_user_id` BIGINT NOT NULL,
    `stage` VARCHAR(30) NOT NULL,
    `decision` VARCHAR(20) NOT NULL,
    `previous_status` VARCHAR(35),
    `new_status` VARCHAR(35),
    `remarks` VARCHAR(500),
    `action_timestamp` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`application_id`) REFERENCES `registration_applications`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`action_by_user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
);

-- Note: The application's DataInitializer component will automatically seed
-- default test users and sample applications on startup if tables are empty.
