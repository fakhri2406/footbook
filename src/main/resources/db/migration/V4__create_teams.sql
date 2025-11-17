-- Teams
CREATE TABLE IF NOT EXISTS teams (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    logo_url VARCHAR(500),
    captain_id UUID NOT NULL,
    roster_size INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT fk_teams_captain FOREIGN KEY (captain_id) REFERENCES users(id),
    CONSTRAINT chk_teams_status CHECK (status IN ('ACTIVE', 'DISBANDED')),
    CONSTRAINT chk_teams_roster_size CHECK (roster_size > 0)
);

CREATE INDEX IF NOT EXISTS idx_teams_captain_id ON teams (captain_id);
CREATE INDEX IF NOT EXISTS idx_teams_status ON teams (status);
CREATE INDEX IF NOT EXISTS idx_teams_name ON teams (name);

-- Team Members
CREATE TABLE IF NOT EXISTS team_members (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    team_id UUID NOT NULL,
    user_id UUID NOT NULL,
    joined_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT fk_team_members_team FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE CASCADE,
    CONSTRAINT fk_team_members_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uq_team_members_team_user UNIQUE (team_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_team_members_team_id ON team_members (team_id);
CREATE INDEX IF NOT EXISTS idx_team_members_user_id ON team_members (user_id);
