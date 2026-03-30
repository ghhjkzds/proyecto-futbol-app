-- ============================================================
-- FIX DEFINITIVO: Eliminar restricción con nombre autogenerado
-- ============================================================

USE futbol_app;

-- PASO 1: Ver todas las restricciones actuales
SHOW INDEX FROM alineaciones;

-- PASO 2: Eliminar la restricción antigua con nombre autogenerado
-- Este es el nombre que apareció en tu error:
ALTER TABLE alineaciones DROP INDEX `UKllrfbasyk3y82orsc0dmc82c9`;

-- PASO 3: Verificar que solo quede la restricción correcta
SHOW INDEX FROM alineaciones WHERE Key_name LIKE 'uk_%' OR Key_name LIKE 'UK%';

-- Deberías ver solo:
-- uk_user_partido_equipo con 3 filas (created_by, partido_id, equipo_id)

SELECT 'Restricción antigua eliminada correctamente' AS Resultado;
