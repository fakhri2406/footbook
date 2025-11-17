-- Stadium Branches
CREATE TABLE IF NOT EXISTS branches (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    address TEXT NOT NULL,
    google_maps_url TEXT,
    operating_hours_start TIME NOT NULL,
    operating_hours_end TIME NOT NULL,
    contact_phone VARCHAR(50),
    contact_email VARCHAR(255),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_branches_is_active ON branches (is_active);
CREATE INDEX IF NOT EXISTS idx_branches_name ON branches (name);
