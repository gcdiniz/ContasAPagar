CREATE TABLE usuario (
    id BIGSERIAL PRIMARY KEY,
    nm_usuario VARCHAR(100) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'USER'
);

-- Senha: admin123 (BCrypt)
INSERT INTO usuario (nm_usuario, senha, role)
VALUES ('admin', '$2b$10$4dmFH3eOaxOhGGfF83ofK.hoO8cUwlyhCn7RH5GPsMwFK4PLuer9O', 'ADMIN');
