-- ================================================
-- SCRIPT DE VERIFICACIÓN Y TROUBLESHOOTING
-- ================================================

USE futbol_app;

-- 1. VERIFICAR USUARIOS EXISTENTES
-- ================================================
SELECT
    id,
    email,
    role,
    created_at,
    SUBSTRING(password, 1, 20) as password_hash_preview
FROM users
ORDER BY created_at DESC;

-- 2. CONTAR USUARIOS
-- ================================================
SELECT
    role,
    COUNT(*) as total
FROM users
GROUP BY role;

-- 3. ELIMINAR TODOS LOS USUARIOS (SI NECESITAS EMPEZAR DE CERO)
-- ================================================
-- CUIDADO: Esto borrará TODOS los usuarios
-- DELETE FROM users;

-- 4. CREAR USUARIO DE PRUEBA CON CONTRASEÑA CONOCIDA
-- ================================================
-- Contraseña: "password123"
-- Hash BCrypt de "password123": $2a$10$N9qo8uLOickgx2ZMRZoMye1J2sOVUPbLvE1BgGzWV.FKv7z7X/w2C

INSERT INTO users (email, password, role) VALUES
('test@futbol.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J2sOVUPbLvE1BgGzWV.FKv7z7X/w2C', 'USER');

-- 5. CREAR USUARIO ADMIN DE PRUEBA
-- ================================================
-- Contraseña: "admin123"
-- Hash BCrypt de "admin123": $2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.

INSERT INTO users (email, password, role) VALUES
('admin@futbol.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'ADMIN');

-- 6. ACTUALIZAR UN USUARIO EXISTENTE A ADMIN
-- ================================================
-- Cambiar el primer parámetro por el email del usuario que quieres hacer admin
UPDATE users
SET role = 'ADMIN'
WHERE email = 'tu@email.com';

-- 7. CAMBIAR CONTRASEÑA DE UN USUARIO
-- ================================================
-- Nota: Necesitas generar el hash BCrypt desde Java o una herramienta online
-- Ejemplo con "password123"
UPDATE users
SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMye1J2sOVUPbLvE1BgGzWV.FKv7z7X/w2C'
WHERE email = 'tu@email.com';

-- 8. VERIFICAR ESTRUCTURA DE LA TABLA
-- ================================================
DESCRIBE users;

-- 9. VERIFICAR CONSTRAINTS
-- ================================================
SHOW CREATE TABLE users;

-- 10. LIMPIAR DATOS DE PRUEBA (SOLO SI ES NECESARIO)
-- ================================================
-- DELETE FROM users WHERE email LIKE '%test%';
-- DELETE FROM users WHERE email LIKE '%@futbol.com';
