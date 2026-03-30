-- ============================================================
-- AGREGAR COMENTARIOS A ALINEACIONES
-- ============================================================
-- Modificar la tabla comentarios para permitir comentarios
-- tanto en equipos como en alineaciones
-- ============================================================

USE futbol_app;

-- PASO 1: Modificar la tabla comentarios
-- Hacer que equipo_id sea opcional (nullable)
-- y agregar alineacion_id

ALTER TABLE comentarios
    MODIFY COLUMN equipo_id INT NULL,
    ADD COLUMN alineacion_id INT NULL AFTER equipo_id,
    ADD COLUMN user_id INT NULL AFTER alineacion_id;

-- PASO 2: Agregar foreign key para alineacion_id
ALTER TABLE comentarios
    ADD CONSTRAINT fk_comentarios_alineacion
        FOREIGN KEY (alineacion_id)
        REFERENCES alineaciones(id)
        ON DELETE CASCADE;

-- PASO 3: Agregar foreign key para user_id (autor del comentario)
ALTER TABLE comentarios
    ADD CONSTRAINT fk_comentarios_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE SET NULL;

-- PASO 4: Agregar restricción de check para asegurar que el comentario
-- pertenezca a un equipo O a una alineación (no ambos ni ninguno)
ALTER TABLE comentarios
    ADD CONSTRAINT chk_comentario_tipo
        CHECK (
            (equipo_id IS NOT NULL AND alineacion_id IS NULL)
            OR
            (equipo_id IS NULL AND alineacion_id IS NOT NULL)
        );

-- Verificar la estructura
SHOW CREATE TABLE comentarios;

SELECT 'Tabla comentarios actualizada para soportar comentarios en alineaciones' AS Resultado;
