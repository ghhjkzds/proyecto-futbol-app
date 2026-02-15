-- ============================================================
-- FIX DEFINITIVO: Eliminar restricción usada por Foreign Key
-- ============================================================
-- Error: Cannot drop index 'UKllrfbasyk3y82orsc0dmc82c9':
--        needed in a foreign key constraint
-- ============================================================

USE futbol_app;

-- PASO 1: Ver la estructura actual de la tabla
SHOW CREATE TABLE alineaciones;

-- PASO 2: Identificar las claves foráneas que dependen del índice
SELECT
    CONSTRAINT_NAME,
    TABLE_NAME,
    COLUMN_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE
    TABLE_SCHEMA = 'futbol_app'
    AND TABLE_NAME = 'alineaciones'
    AND REFERENCED_TABLE_NAME IS NOT NULL;

-- ============================================================
-- PASO 3: Eliminar las claves foráneas temporalmente
-- ============================================================
-- Nota: Ajusta los nombres según lo que viste en el PASO 2

-- Eliminar FK de partido_id (si existe)
-- ALTER TABLE alineaciones DROP FOREIGN KEY fk_alineacion_partido;

-- Eliminar FK de equipo_id (si existe)
-- ALTER TABLE alineaciones DROP FOREIGN KEY fk_alineacion_equipo;

-- Eliminar FK de created_by (si existe)
-- ALTER TABLE alineaciones DROP FOREIGN KEY nombre_fk_created_by;

-- ============================================================
-- PASO 4: Ahora eliminar el índice UNIQUE problemático
-- ============================================================

ALTER TABLE alineaciones DROP INDEX `UKllrfbasyk3y82orsc0dmc82c9`;

-- ============================================================
-- PASO 5: Recrear las claves foráneas
-- ============================================================

ALTER TABLE alineaciones
ADD CONSTRAINT fk_alineacion_partido
FOREIGN KEY (partido_id) REFERENCES partidos(id) ON DELETE CASCADE;

ALTER TABLE alineaciones
ADD CONSTRAINT fk_alineacion_equipo
FOREIGN KEY (equipo_id) REFERENCES equipos(id) ON DELETE CASCADE;

ALTER TABLE alineaciones
ADD CONSTRAINT fk_alineacion_created_by
FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL;

-- ============================================================
-- PASO 6: Verificar que solo quede la restricción correcta
-- ============================================================

SHOW INDEX FROM alineaciones;

-- Deberías ver:
-- - PRIMARY (id)
-- - uk_user_partido_equipo (created_by, partido_id, equipo_id)
-- - Índices normales para las FK (no únicos)

SELECT 'Fix aplicado correctamente. Múltiples usuarios ya pueden crear alineaciones.' AS Resultado;
