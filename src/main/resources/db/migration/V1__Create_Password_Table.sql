CREATE TABLE passwordword(
    id uuid PRIMARY KEY,
    value TEXT NOT NULL,
    language TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_passwordword_language on passwordword(language);