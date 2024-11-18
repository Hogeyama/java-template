-- テスト用ユーザーのパスワードは 'password' (BCryptでハッシュ化済み)
INSERT INTO users (username, password, email, enabled)
VALUES ('testuser', '$2y$10$nZ3bb0HpZh6D4t8d51MGIOoB4Sp45kYJ5trdKI.X2ODaPZ1Y0Yaiq', 'test@example.com', true);

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.username = 'testuser' AND r.name = 'ROLE_USER';
