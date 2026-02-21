CREATE TABLE schedule_activities (
    id              BIGSERIAL PRIMARY KEY,
    gym_id          BIGINT NOT NULL REFERENCES gyms(id),
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    day_of_week     INTEGER NOT NULL CHECK (day_of_week BETWEEN 1 AND 7),
    start_time      TIME NOT NULL,
    end_time        TIME NOT NULL,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);
