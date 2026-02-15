-- ================================================
-- SCRIPT PARA GESTIONAR ROLES DE USUARIOS
-- ================================================

USE futbol_app;

-- ================================================
-- 1. VER TODOS LOS USUARIOS Y SUS ROLES
-- ================================================
SELECT
    id,
    email,
    role,
    created_at
FROM users
ORDER BY created_at DESC;

-- ================================================
-- 2. CONVERTIR UN USUARIO A ADMIN
-- ================================================
-- Reemplaza 'tu@email.com' con el email del usuario

UPDATE users
SET role = 'ADMIN'
WHERE email = 'tu@email.com';

-- Verificar el cambio
SELECT id, email, role FROM users WHERE email = 'tu@email.com';

-- ================================================
-- 3. CONVERTIR MÚLTIPLES USUARIOS A ADMIN
-- ================================================
UPDATE users
SET role = 'ADMIN'
WHERE email IN ('user1@email.com', 'user2@email.com', 'user3@email.com');

-- ================================================
-- 4. CONVERTIR UN USUARIO DE ADMIN A USER
-- ================================================
UPDATE users
SET role = 'USER'
WHERE email = 'tu@email.com';

-- ================================================
-- 5. HACER ADMIN AL PRIMER USUARIO REGISTRADO
-- ================================================
UPDATE users
SET role = 'ADMIN'
WHERE id = 1;

-- ================================================
-- 6. CONTAR USUARIOS POR ROL
-- ================================================
SELECT
    role,
    COUNT(*) as total_usuarios
FROM users
GROUP BY role;

-- ================================================
-- 7. LISTAR SOLO ADMINS
-- ================================================
SELECT
    id,
    email,
    created_at
FROM users
WHERE role = 'ADMIN'
ORDER BY created_at;

-- ================================================
-- 8. LISTAR SOLO USERS
-- ================================================
SELECT
    id,
    email,
    created_at
FROM users
WHERE role = 'USER'
ORDER BY created_at;

-- ================================================
-- EJEMPLOS DE USO RÁPIDO
-- ================================================

-- Hacer ADMIN al último usuario registrado
UPDATE users
SET role = 'ADMIN'
WHERE id = (SELECT MAX(id) FROM (SELECT id FROM users) AS temp);

-- Hacer ADMIN a todos los usuarios (¡CUIDADO!)
-- UPDATE users SET role = 'ADMIN';

-- Hacer USER a todos los usuarios (¡CUIDADO!)
-- UPDATE users SET role = 'USER';

-- ================================================
-- VERIFICACIÓN FINAL
-- ================================================
SELECT
    'Total Usuarios' as Tipo,
    COUNT(*) as Cantidad
FROM users
UNION ALL
SELECT
    'Usuarios ADMIN' as Tipo,
    COUNT(*) as Cantidad
FROM users
WHERE role = 'ADMIN'
UNION ALL
SELECT
    'Usuarios USER' as Tipo,
    COUNT(*) as Cantidad
FROM users
WHERE role = 'USER';
