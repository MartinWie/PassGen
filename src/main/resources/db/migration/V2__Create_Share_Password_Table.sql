CREATE TABLE share_password(
    id uuid PRIMARY KEY,
    created timestamp not null,
    value TEXT NOT NULL,
    remaining_views numeric DEFAULT 1
);