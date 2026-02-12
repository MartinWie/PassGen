-- Add format column to share_public_key table.
-- Tracks whether the key pair was generated in OpenSSH or PEM format.
-- Default is 'openssh' for backward compatibility with existing shares.

ALTER TABLE share_public_key ADD COLUMN format VARCHAR(20) NOT NULL DEFAULT 'openssh';
