CREATE TABLE gym_members (
    id                      BIGSERIAL PRIMARY KEY,
    user_id                 BIGINT NOT NULL REFERENCES users(id),
    gym_id                  BIGINT NOT NULL REFERENCES gyms(id),
    role                    VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    status                  VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    membership_expires_at   TIMESTAMP,
    created_at              TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP,
    CONSTRAINT chk_role CHECK (role IN ('MEMBER','COACH','ADMIN','ADMIN_COACH')),
    CONSTRAINT chk_status CHECK (status IN ('PENDING','ACTIVE','REJECTED','BLOCKED'))
);
