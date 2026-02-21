CREATE TABLE template_blocks (
    id              BIGSERIAL PRIMARY KEY,
    template_id     BIGINT NOT NULL REFERENCES routine_templates(id) ON DELETE CASCADE,
    name            VARCHAR(255) NOT NULL,
    day_number      INTEGER NOT NULL,
    sort_order      INTEGER NOT NULL DEFAULT 0
);
