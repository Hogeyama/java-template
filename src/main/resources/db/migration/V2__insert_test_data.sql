-- テスト用ユーザーのパスワードは 'password' (BCryptでハッシュ化済み)
INSERT INTO users (username, password, email, enabled)
VALUES ('testuser', '$2a$10$YEZoEY8tQnzZNVNxFqXV6.vwY6ld.Qxt5t.2WQhQkdFPRF9JlOPYi', 'test@example.com', true);

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.username = 'testuser' AND r.name = 'ROLE_USER';
