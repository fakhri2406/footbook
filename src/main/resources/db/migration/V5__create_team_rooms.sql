-- Team Rooms
CREATE TABLE IF NOT EXISTS team_rooms (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id UUID NOT NULL,
    creator_team_id UUID NOT NULL,
    opponent_team_id UUID,
    scheduled_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    required_team_size INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT fk_team_rooms_branch FOREIGN KEY (branch_id) REFERENCES branches(id),
    CONSTRAINT fk_team_rooms_creator_team FOREIGN KEY (creator_team_id) REFERENCES teams(id),
    CONSTRAINT fk_team_rooms_opponent_team FOREIGN KEY (opponent_team_id) REFERENCES teams(id),
    CONSTRAINT chk_team_rooms_status CHECK (status IN ('OPEN', 'MATCHED', 'CANCELLED')),
    CONSTRAINT chk_team_rooms_size CHECK (required_team_size > 0),
    CONSTRAINT chk_team_rooms_time CHECK (end_time > start_time)
);

CREATE INDEX IF NOT EXISTS idx_team_rooms_branch_id ON team_rooms (branch_id);
CREATE INDEX IF NOT EXISTS idx_team_rooms_creator_team_id ON team_rooms (creator_team_id);
CREATE INDEX IF NOT EXISTS idx_team_rooms_opponent_team_id ON team_rooms (opponent_team_id);
CREATE INDEX IF NOT EXISTS idx_team_rooms_scheduled_date ON team_rooms (scheduled_date);
CREATE INDEX IF NOT EXISTS idx_team_rooms_status ON team_rooms (status);
CREATE INDEX IF NOT EXISTS idx_team_rooms_date_time ON team_rooms (scheduled_date, start_time, end_time);
