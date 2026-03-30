-- ============================================================
-- CORRECCIÓN: Restricción UNIQUE en tabla alineaciones
-- ============================================================
-- Problema: La restricción actual solo permite una alineación
--           por (partido_id, equipo_id), sin considerar el usuario.
--
-- Solución: Cambiar la restricción para permitir que cada usuario
--           tenga su propia alineación por partido/equipo.
-- ============================================================

USE futbol_app;

-- Paso 1: Ver las restricciones actuales para identificar cuál eliminar
SHOW INDEX FROM alineaciones;

-- Paso 2: Eliminar la restricción antigua
-- Nota: Si aparece error "Can't DROP...", significa que no existe y puedes continuar

-- Intenta ejecutar UNA de estas líneas (la que corresponda a tu restricción):
-- Si tienes una restricción llamada 'partido_id':
-- ALTER TABLE alineaciones DROP INDEX `partido_id`;

-- Si tienes una restricción llamada 'uk_partido_equipo':
-- ALTER TABLE alineaciones DROP INDEX `uk_partido_equipo`;

-- Si tienes una restricción llamada 'unique_partido_equipo':
-- ALTER TABLE alineaciones DROP INDEX `unique_partido_equipo`;

-- EJECUTA SOLO LA SIGUIENTE LÍNEA (la más común):
ALTER TABLE alineaciones DROP INDEX `partido_id`;

-- Paso 3: Crear la restricción correcta que incluye created_by
-- Esto permite que CADA USUARIO tenga UNA alineación por partido/equipo

ALTER TABLE alineaciones
ADD CONSTRAINT uk_user_partido_equipo
UNIQUE (created_by, partido_id, equipo_id);

-- Verificar que la restricción se creó correctamente
SHOW INDEX FROM alineaciones WHERE Key_name = 'uk_user_partido_equipo';

-- ============================================================
-- RESULTADO ESPERADO:
-- ============================================================
-- Ahora cada usuario puede crear:
-- - UNA alineación para Barcelona en el partido Barcelona vs Real Madrid
-- - UNA alineación para Real Madrid en el partido Barcelona vs Real Madrid
-- - Etc.
--
-- Pero NO puede crear:
-- - DOS alineaciones para Barcelona en el mismo partido
-- - DOS alineaciones para Real Madrid en el mismo partido
-- ============================================================

SELECT 'Restricción actualizada correctamente' AS Resultado;
