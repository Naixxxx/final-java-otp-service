CREATE TABLE IF NOT EXISTS app_users (
    id BIGSERIAL PRIMARY KEY,
    login VARCHAR(80) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'USER')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_only_one_admin
    ON app_users ((role))
    WHERE role = 'ADMIN';

CREATE TABLE IF NOT EXISTS otp_settings (
    id SMALLINT PRIMARY KEY DEFAULT 1 CHECK (id = 1),
    code_length INT NOT NULL CHECK (code_length BETWEEN 4 AND 10),
    lifetime_seconds INT NOT NULL CHECK (lifetime_seconds BETWEEN 30 AND 3600),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO otp_settings (id, code_length, lifetime_seconds)
VALUES (1, 6, 300)
ON CONFLICT (id) DO NOTHING;

CREATE TABLE IF NOT EXISTS otp_challenges (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    operation_ref VARCHAR(120) NOT NULL,
    code_value VARCHAR(16) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE', 'EXPIRED', 'USED')),
    delivery_channel VARCHAR(20) NOT NULL CHECK (delivery_channel IN ('EMAIL', 'SMS', 'TELEGRAM', 'FILE')),
    destination VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP NULL
);

CREATE INDEX IF NOT EXISTS ix_otp_user_operation ON otp_challenges(user_id, operation_ref);
CREATE INDEX IF NOT EXISTS ix_otp_status_expires ON otp_challenges(status, expires_at);
