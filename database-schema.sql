-- Event Management System Database Schema
-- This script creates all necessary tables for the application
-- Execute this in your Neon PostgreSQL database

-- ===============================
-- DROP TABLES (if needed for fresh setup)
-- ===============================
-- Uncomment these lines if you want to recreate tables
-- DROP TABLE IF EXISTS notifications CASCADE;
-- DROP TABLE IF EXISTS tickets CASCADE;
-- DROP TABLE IF EXISTS registrations CASCADE;
-- DROP TABLE IF EXISTS events CASCADE;
-- DROP TABLE IF EXISTS users CASCADE;
-- DROP TABLE IF EXISTS categories CASCADE;

-- ===============================
-- CREATE TABLES
-- ===============================

-- Categories table
CREATE TABLE IF NOT EXISTS categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    role VARCHAR(50) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Events table
CREATE TABLE IF NOT EXISTS events (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    location VARCHAR(255),
    event_date DATE,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    price DECIMAL(10, 2),
    capacity INTEGER,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    category_id INTEGER,
    organizer_id UUID,
    version BIGINT DEFAULT 0,
    CONSTRAINT fk_event_category FOREIGN KEY (category_id) REFERENCES categories(id),
    CONSTRAINT fk_event_organizer FOREIGN KEY (organizer_id) REFERENCES users(id)
);

-- Registrations table
CREATE TABLE IF NOT EXISTS registrations (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    event_id UUID NOT NULL,
    user_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'CONFIRMED',
    note TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_registration_event FOREIGN KEY (event_id) REFERENCES events(id),
    CONSTRAINT fk_registration_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uq_registration_event_user UNIQUE (event_id, user_id)
);

-- Tickets table
CREATE TABLE IF NOT EXISTS tickets (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    registration_id UUID NOT NULL,
    ticket_number VARCHAR(255) NOT NULL UNIQUE,
    qr_code TEXT,
    issued_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ticket_registration FOREIGN KEY (registration_id) REFERENCES registrations(id)
);

-- Notifications table
CREATE TABLE IF NOT EXISTS notifications (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID NOT NULL,
    event_id UUID NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_notification_event FOREIGN KEY (event_id) REFERENCES events(id)
);

-- ===============================
-- CREATE INDEXES FOR PERFORMANCE
-- ===============================

-- Indexes for users
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);

-- Indexes for events
CREATE INDEX IF NOT EXISTS idx_events_status ON events(status);
CREATE INDEX IF NOT EXISTS idx_events_event_date ON events(event_date);
CREATE INDEX IF NOT EXISTS idx_events_category_id ON events(category_id);
CREATE INDEX IF NOT EXISTS idx_events_organizer_id ON events(organizer_id);

-- Indexes for registrations
CREATE INDEX IF NOT EXISTS idx_registrations_event_id ON registrations(event_id);
CREATE INDEX IF NOT EXISTS idx_registrations_user_id ON registrations(user_id);
CREATE INDEX IF NOT EXISTS idx_registrations_status ON registrations(status);

-- Indexes for notifications
CREATE INDEX IF NOT EXISTS idx_notifications_user_id ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_event_id ON notifications(event_id);
CREATE INDEX IF NOT EXISTS idx_notifications_is_read ON notifications(is_read);
CREATE INDEX IF NOT EXISTS idx_notifications_user_is_read ON notifications(user_id, is_read);

-- Indexes for tickets
CREATE INDEX IF NOT EXISTS idx_tickets_registration_id ON tickets(registration_id);
CREATE INDEX IF NOT EXISTS idx_tickets_ticket_number ON tickets(ticket_number);

-- ===============================
-- SAMPLE DATA (Optional - for testing)
-- ===============================

-- Insert sample category
-- INSERT INTO categories (id, name, description, created_at, updated_at)
-- VALUES (1, 'Technology', 'Technology and Innovation Events', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ===============================
-- VERIFICATION QUERIES
-- ===============================
-- Run these to verify the setup

-- Check all tables
-- SELECT table_name FROM information_schema.tables WHERE table_schema = 'public';

-- Check all indexes
-- SELECT tablename, indexname FROM pg_indexes WHERE schemaname = 'public';
