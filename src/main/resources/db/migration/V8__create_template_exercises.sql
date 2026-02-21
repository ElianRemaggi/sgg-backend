CREATE TABLE template_exercises (
    id              BIGSERIAL PRIMARY KEY,
    block_id        BIGINT NOT NULL REFERENCES template_blocks(id) ON DELETE CASCADE,
    name            VARCHAR(255) NOT NULL,
    sets            INTEGER,
    reps            VARCHAR(50),
    rest_seconds    INTEGER,
    notes           TEXT,
    sort_order      INTEGER NOT NULL DEFAULT 0
);
