CREATE TABLE gyms (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    slug            VARCHAR(100) NOT NULL UNIQUE,
    description     TEXT,
    logo_url        VARCHAR(500),
    routine_cycle   VARCHAR(20) NOT NULL DEFAULT 'WEEKLY',
    owner_user_id   BIGINT NOT NULL REFERENCES users(id),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP
);
