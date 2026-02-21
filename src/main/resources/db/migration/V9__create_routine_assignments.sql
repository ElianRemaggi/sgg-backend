CREATE TABLE routine_assignments (
    id                BIGSERIAL PRIMARY KEY,
    gym_id            BIGINT NOT NULL REFERENCES gyms(id),
    template_id       BIGINT NOT NULL REFERENCES routine_templates(id),
    member_user_id    BIGINT NOT NULL REFERENCES users(id),
    assigned_by       BIGINT NOT NULL REFERENCES users(id),
    starts_at         DATE NOT NULL,
    ends_at           DATE NOT NULL,
    created_at        TIMESTAMP NOT NULL DEFAULT NOW()
);
