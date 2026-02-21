CREATE TABLE exercise_completions (
    id              BIGSERIAL PRIMARY KEY,
    assignment_id   BIGINT NOT NULL REFERENCES routine_assignments(id),
    exercise_id     BIGINT NOT NULL REFERENCES template_exercises(id),
    gym_id          BIGINT NOT NULL REFERENCES gyms(id),
    user_id         BIGINT NOT NULL REFERENCES users(id),
    completed_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    is_completed    BOOLEAN NOT NULL DEFAULT TRUE,
    updated_at      TIMESTAMP
);
