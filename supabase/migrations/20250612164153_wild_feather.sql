-- Create database for Client A MySQL instance
-- This script runs in the mysql-client-a container

-- The multi_client_a database is already created by the MYSQL_DATABASE environment variable
-- This script ensures proper permissions and setup

-- Grant permissions to app_user
GRANT ALL PRIVILEGES ON multi_client_a.* TO 'app_user'@'%';

-- Grant permissions to root for migrations
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION;

-- Flush privileges
FLUSH PRIVILEGES;

-- Show databases
SHOW DATABASES;