INSERT INTO users (id, username, password, email, enabled)
VALUES (
      gen_random_uuid()
    , 'testuser'
    , '$2y$10$nZ3bb0HpZh6D4t8d51MGIOoB4Sp45kYJ5trdKI.X2ODaPZ1Y0Yaiq' -- 'password'
    , 'test@example.com'
    , true
    );

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.username = 'testuser' AND r.name = 'ROLE_USER';
