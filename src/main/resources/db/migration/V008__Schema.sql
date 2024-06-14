-- Check if pgcrypto is already installed
SELECT extname FROM pg_extension WHERE extname = 'pgcrypto';

-- If not installed, install pgcrypto
CREATE EXTENSION IF NOT EXISTS pgcrypto;
