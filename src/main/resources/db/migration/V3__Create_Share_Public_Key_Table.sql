CREATE TABLE share_public_key(
    id uuid PRIMARY KEY,
    created timestamp not null,
    public_key TEXT NOT NULL,
    algorithm VARCHAR(50) NOT NULL,
    purpose VARCHAR(50) NOT NULL
);
