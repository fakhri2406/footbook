-- Notifications
CREATE TABLE IF NOT EXISTS notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    related_entity_type VARCHAR(50),
    related_entity_id UUID,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at TIMESTAMP WITHOUT TIME ZONE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_notifications_type CHECK (type IN (
        'ROOM_JOINED', 
        'ROOM_FULL', 
        'TEAM_INVITATION', 
        'BOOKING_CONFIRMATION', 
        'MATCH_REMINDER', 
        'TEAM_UPDATE'
    ))
);

CREATE INDEX IF NOT EXISTS idx_notifications_user_id ON notifications (user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_is_read ON notifications (is_read);
CREATE INDEX IF NOT EXISTS idx_notifications_created_at ON notifications (created_at);
CREATE INDEX IF NOT EXISTS idx_notifications_user_unread ON notifications (user_id, is_read) WHERE is_read = FALSE;
