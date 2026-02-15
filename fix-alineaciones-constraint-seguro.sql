-- ============================================================
-- CORRECCIÓN: Restricción UNIQUE en tabla alineaciones (VERSIÓN SEGURA)
-- ============================================================
-- Este script es compatible con todas las versiones de MySQL
-- ============================================================

USE futbol_app;

-- PASO 1: Ver las restricciones actuales
SELECT
    TABLE_NAME,
    CONSTRAINT_NAME,
    CONSTRAINT_TYPE
FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
WHERE TABLE_SCHEMA = 'futbol_app'
  AND TABLE_NAME = 'alineaciones';

-- Ver los índices actuales
SHOW INDEX FROM alineaciones;

-- ============================================================
-- PASO 2: ELIMINAR LA RESTRICCIÓN ANTIGUA
-- ============================================================
-- Copia la línea que corresponda según lo que viste arriba:

-- Opción A: Si la restricción se llama 'partido_id'
ALTER TABLE alineaciones DROP INDEX `partido_id`;

-- Opción B: Si la restricción se llama 'uk_partido_equipo'
-- ALTER TABLE alineaciones DROP INDEX `uk_partido_equipo`;

-- Opción C: Si la restricción se llama algo diferente, usa ese nombre:
-- ALTER TABLE alineaciones DROP INDEX `nombre_que_viste_arriba`;

-- ============================================================
-- PASO 3: CREAR LA RESTRICCIÓN CORRECTA
-- ============================================================

ALTER TABLE alineaciones
ADD CONSTRAINT uk_user_partido_equipo
UNIQUE (created_by, partido_id, equipo_id);

-- ============================================================
-- PASO 4: VERIFICAR QUE SE APLICÓ CORRECTAMENTE
-- ============================================================

-- Ver la nueva restricción
SHOW INDEX FROM alineaciones WHERE Key_name = 'uk_user_partido_equipo';

-- Debe mostrar 3 filas (una por cada columna):
-- uk_user_partido_equipo | created_by
-- uk_user_partido_equipo | partido_id
-- uk_user_partido_equipo | equipo_id

SELECT 'Restricción actualizada correctamente' AS Resultado;

-- ============================================================
-- PRUEBA (OPCIONAL)
-- ============================================================
-- Intenta insertar datos de prueba para verificar que funciona:

-- Esto debería funcionar (diferentes usuarios):
-- INSERT INTO alineaciones (partido_id, equipo_id, created_by, alineacion, created_at)
-- VALUES (1, 2, 1, '{}', NOW());

-- INSERT INTO alineaciones (partido_id, equipo_id, created_by, alineacion, created_at)
-- VALUES (1, 2, 2, '{}', NOW());

-- Esto debería dar error (mismo usuario, mismo partido, mismo equipo):
-- INSERT INTO alineaciones (partido_id, equipo_id, created_by, alineacion, created_at)
-- VALUES (1, 2, 1, '{}', NOW());
