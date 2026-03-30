-- ❌ ESTE SCRIPT NO ES NECESARIO ❌
--
-- La funcionalidad de votos usa la columna 'votos' que YA EXISTE en la tabla 'equipos'
-- NO se requiere agregar ninguna columna nueva
--
-- Cuando los usuarios votan por una alineación, el voto se registra en el equipo asociado
-- Ejemplo: Si votas por una alineación del Real Madrid, se incrementa equipos.votos
--          donde nombre = 'Real Madrid'
--
-- Este archivo se mantiene solo como documentación de que la funcionalidad
-- está implementada usando la estructura de base de datos existente.

USE futbol_app;

-- Verificar que la tabla equipos tiene la columna votos
DESCRIBE equipos;

-- Verificar votos actuales de equipos
SELECT id, nombre, votos FROM equipos;

SELECT 'La columna votos ya existe en la tabla EQUIPOS - No se requiere modificación' AS info;

