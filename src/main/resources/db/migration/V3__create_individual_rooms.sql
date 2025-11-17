-- Individual Rooms
CREATE TABLE IF NOT EXISTS individual_rooms (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id UUID NOT NULL,
    owner_id UUID NOT NULL,
    scheduled_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    total_slots INTEGER NOT NULL,
    notes TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT fk_individual_rooms_branch FOREIGN KEY (branch_id) REFERENCES branches(id),
    CONSTRAINT fk_individual_rooms_owner FOREIGN KEY (owner_id) REFERENCES users(id),
    CONSTRAINT chk_individual_rooms_status CHECK (status IN ('OPEN', 'FULL', 'CANCELLED')),
    CONSTRAINT chk_individual_rooms_slots CHECK (total_slots > 0),
    CONSTRAINT chk_individual_rooms_time CHECK (end_time > start_time)
);

CREATE INDEX IF NOT EXISTS idx_individual_rooms_branch_id ON individual_rooms (branch_id);
CREATE INDEX IF NOT EXISTS idx_individual_rooms_owner_id ON individual_rooms (owner_id);
CREATE INDEX IF NOT EXISTS idx_individual_rooms_scheduled_date ON individual_rooms (scheduled_date);
CREATE INDEX IF NOT EXISTS idx_individual_rooms_status ON individual_rooms (status);
CREATE INDEX IF NOT EXISTS idx_individual_rooms_date_time ON individual_rooms (scheduled_date, start_time, end_time);

-- Individual Room Participants
CREATE TABLE IF NOT EXISTS individual_room_participants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    room_id UUID NOT NULL,
    user_id UUID NOT NULL,
    joined_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT fk_individual_room_participants_room FOREIGN KEY (room_id) REFERENCES individual_rooms(id) ON DELETE CASCADE,
    CONSTRAINT fk_individual_room_participants_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uq_individual_room_participants_room_user UNIQUE (room_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_individual_room_participants_room_id ON individual_room_participants (room_id);
CREATE INDEX IF NOT EXISTS idx_individual_room_participants_user_id ON individual_room_participants (user_id);
