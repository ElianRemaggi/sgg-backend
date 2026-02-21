-- Multi-tenant indexes (críticos para performance)
CREATE INDEX idx_gym_members_gym_id ON gym_members(gym_id);
CREATE INDEX idx_gym_members_user_gym ON gym_members(user_id, gym_id);
CREATE INDEX idx_coach_assignments_gym ON coach_assignments(gym_id);
CREATE INDEX idx_coach_assignments_coach ON coach_assignments(coach_user_id, gym_id);
CREATE INDEX idx_routine_templates_gym ON routine_templates(gym_id);
CREATE INDEX idx_routine_assignments_member ON routine_assignments(member_user_id, gym_id);
CREATE INDEX idx_exercise_completions_assignment ON exercise_completions(assignment_id);
CREATE INDEX idx_exercise_completions_gym ON exercise_completions(gym_id);
CREATE INDEX idx_schedule_activities_gym ON schedule_activities(gym_id);

-- Constraint de negocio: solo una solicitud PENDING por usuario por gym
CREATE UNIQUE INDEX idx_unique_pending_membership
    ON gym_members(user_id, gym_id)
    WHERE status = 'PENDING';

-- Performance adicional
CREATE UNIQUE INDEX idx_users_supabase_uid ON users(supabase_uid);
CREATE INDEX idx_gyms_slug ON gyms(slug);
CREATE INDEX idx_exercise_completions_user_assignment
    ON exercise_completions(user_id, assignment_id);
-- Índice para buscar rutina activa de un miembro
CREATE INDEX idx_routine_assignments_active
    ON routine_assignments(member_user_id, gym_id, starts_at, ends_at);
