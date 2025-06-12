-- Create additional databases for multi-tenant setup
-- This script runs when MySQL containers start for the first time

-- Create multi_client_b database in the main MySQL instance
CREATE DATABASE IF NOT EXISTS multi_client_b;

-- Grant permissions to app_user for all databases
GRANT ALL PRIVILEGES ON multi_main.* TO 'app_user'@'%';
GRANT ALL PRIVILEGES ON multi_client_b.* TO 'app_user'@'%';

-- Grant permissions to root for all databases (for migrations)
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION;

-- Flush privileges to ensure changes take effect
FLUSH PRIVILEGES;

-- Show created databases
SHOW DATABASES;