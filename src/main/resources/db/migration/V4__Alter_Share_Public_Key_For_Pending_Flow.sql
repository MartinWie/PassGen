-- Modify share_public_key table to support the new flow where:
-- 1. Admin creates a "pending" share (no public key yet)
-- 2. Recipient opens link and generates key on their machine
-- 3. Public key is sent to server, private key stays with recipient

-- Make public_key nullable (NULL until recipient generates it)
ALTER TABLE share_public_key ALTER COLUMN public_key DROP NOT NULL;

-- Add label column for admin to identify the share
ALTER TABLE share_public_key ADD COLUMN label VARCHAR(256);

-- Add completed_at timestamp to track when the key was generated
ALTER TABLE share_public_key ADD COLUMN completed_at TIMESTAMP;

-- Add index for finding pending vs completed shares
CREATE INDEX idx_share_public_key_completed_at ON share_public_key(completed_at);
