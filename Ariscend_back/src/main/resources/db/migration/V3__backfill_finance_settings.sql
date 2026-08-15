INSERT INTO finance_settings (
    user_id,
    base_currency,
    payment_alert_days,
    created_at,
    updated_at
)
SELECT
    users.id,
    'MXN',
    3,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM users
WHERE NOT EXISTS (
    SELECT 1
    FROM finance_settings
    WHERE finance_settings.user_id = users.id
)
ON CONFLICT (user_id) DO NOTHING;
