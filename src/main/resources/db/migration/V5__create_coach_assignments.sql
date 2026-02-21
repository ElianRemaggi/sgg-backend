CREATE TABLE coach_assignments (
    id                BIGSERIAL PRIMARY KEY,
    gym_id            BIGINT NOT NULL REFERENCES gyms(id),
    coach_user_id     BIGINT NOT NULL REFERENCES users(id),
    member_user_id    BIGINT NOT NULL REFERENCES users(id),
    assigned_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    unassigned_at     TIMESTAMP
);
