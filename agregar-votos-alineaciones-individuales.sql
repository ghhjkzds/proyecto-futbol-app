-- ============================================================
-- MIGRAR VOTOS DE EQUIPOS A ALINEACIONES INDIVIDUALES
-- ============================================================
-- Agregar columna votos a la tabla alineaciones
-- y opcionalmente eliminarla de equipos si no se necesita
-- ============================================================

USE futbol_app;

-- PASO 1: Agregar columna votos a alineaciones
ALTER TABLE alineaciones
    ADD COLUMN votos INT DEFAULT 0 AFTER alineacion;

-- PASO 2 (Opcional): Si ya no necesitas la columna votos en equipos, puedes eliminarla
-- Descomenta la siguiente línea si quieres eliminarla:
-- ALTER TABLE equipos DROP COLUMN votos;

-- PASO 3: Verificar la estructura
SHOW CREATE TABLE alineaciones;

SELECT 'Columna votos agregada a la tabla alineaciones' AS Resultado;
