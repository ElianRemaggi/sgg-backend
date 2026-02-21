CREATE TABLE routine_templates (
    id              BIGSERIAL PRIMARY KEY,
    gym_id          BIGINT NOT NULL REFERENCES gyms(id),
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    created_by      BIGINT NOT NULL REFERENCES users(id),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP
);
