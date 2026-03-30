-- ============================================================
-- SOLUCIÓN DIRECTA: Recrear la tabla sin la restricción problemática
-- ============================================================
-- Esta es la solución más segura y directa
-- ============================================================

USE futbol_app;

-- PASO 1: Hacer backup de los datos existentes
CREATE TABLE alineaciones_backup AS SELECT * FROM alineaciones;

SELECT COUNT(*) as 'Registros respaldados' FROM alineaciones_backup;

-- PASO 2: Eliminar la tabla original
DROP TABLE alineaciones;

-- PASO 3: Recrear la tabla con la estructura correcta
CREATE TABLE alineaciones (
    id INT AUTO_INCREMENT PRIMARY KEY,

    partido_id INT NOT NULL,
    equipo_id INT NOT NULL,
    created_by INT,

    alineacion JSON NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- CLAVE FORÁNEA: Partido
    CONSTRAINT fk_alineacion_partido
        FOREIGN KEY (partido_id)
        REFERENCES partidos(id)
        ON DELETE CASCADE,

    -- CLAVE FORÁNEA: Equipo
    CONSTRAINT fk_alineacion_equipo
        FOREIGN KEY (equipo_id)
        REFERENCES equipos(id)
        ON DELETE CASCADE,

    -- CLAVE FORÁNEA: Usuario
    CONSTRAINT fk_alineacion_created_by
        FOREIGN KEY (created_by)
        REFERENCES users(id)
        ON DELETE SET NULL,

    -- RESTRICCIÓN UNIQUE CORRECTA (incluye created_by)
    CONSTRAINT uk_user_partido_equipo
        UNIQUE (created_by, partido_id, equipo_id)
);

-- PASO 4: Restaurar los datos
INSERT INTO alineaciones (id, partido_id, equipo_id, created_by, alineacion, created_at)
SELECT id, partido_id, equipo_id, created_by, alineacion, created_at
FROM alineaciones_backup;

-- PASO 5: Verificar
SELECT COUNT(*) as 'Registros restaurados' FROM alineaciones;

-- Ver la estructura
SHOW CREATE TABLE alineaciones;

-- PASO 6: Eliminar el backup (OPCIONAL - hazlo después de verificar que todo funciona)
-- DROP TABLE alineaciones_backup;

SELECT 'Tabla recreada correctamente con la restricción UNIQUE correcta' AS Resultado;
