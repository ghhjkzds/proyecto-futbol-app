-- ============================================================
-- DIAGNÓSTICO Y FIX: Restricción con Foreign Keys
-- ============================================================
-- EJECUTA ESTE SCRIPT PASO POR PASO
-- ============================================================

USE futbol_app;

-- ============================================================
-- PARTE 1: DIAGNÓSTICO
-- ============================================================

-- Ver la estructura completa
SELECT '=== ESTRUCTURA DE LA TABLA ===' AS Info;
SHOW CREATE TABLE alineaciones;

-- Ver todas las claves foráneas
SELECT '=== CLAVES FORÁNEAS ===' AS Info;
SELECT
    CONSTRAINT_NAME as 'Nombre FK',
    COLUMN_NAME as 'Columna',
    REFERENCED_TABLE_NAME as 'Tabla Referenciada',
    REFERENCED_COLUMN_NAME as 'Columna Referenciada'
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE
    TABLE_SCHEMA = 'futbol_app'
    AND TABLE_NAME = 'alineaciones'
    AND REFERENCED_TABLE_NAME IS NOT NULL
ORDER BY CONSTRAINT_NAME;

-- Ver todos los índices UNIQUE
SELECT '=== ÍNDICES UNIQUE ===' AS Info;
SHOW INDEX FROM alineaciones WHERE Non_unique = 0;

-- ============================================================
-- INSTRUCCIONES:
-- ============================================================
-- 1. Copia el resultado del comando SHOW CREATE TABLE
-- 2. Busca líneas que digan CONSTRAINT ... FOREIGN KEY
-- 3. Anota los nombres de las constraints
-- 4. Ve a la PARTE 2 abajo
-- ============================================================

SELECT '=== CONTINÚA EN LA PARTE 2 ===' AS Info;
