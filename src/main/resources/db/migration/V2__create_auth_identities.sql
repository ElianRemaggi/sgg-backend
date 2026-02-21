CREATE TABLE auth_identities (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id),
    provider        VARCHAR(50) NOT NULL,
    provider_uid    VARCHAR(255) NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(provider, provider_uid)
);
