-- Add sample data for testing
-- This migration adds initial sample data to help verify the multi-database routing

-- Insert sample data only if the table is empty
INSERT IGNORE INTO post (id, name) VALUES 
(1, 'Welcome to Multi-Database System'),
(2, 'Database Routing Example'),
(3, 'Flyway Migration Success');