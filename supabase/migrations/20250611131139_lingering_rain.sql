-- Create post table
-- This migration creates the basic post table structure for all tenant databases

CREATE TABLE IF NOT EXISTS post (
    id BIGINT NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create index on name for better query performance
CREATE INDEX IF NOT EXISTS idx_post_name ON post(name);

-- Create index on created_at for time-based queries
CREATE INDEX IF NOT EXISTS idx_post_created_at ON post(created_at);