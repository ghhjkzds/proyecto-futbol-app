# ✅ SOLUCIÓN FINAL: Recrear Tabla Alineaciones

## 🎯 SOLUCIÓN DEFINITIVA

El problema de la restricción `UKllrfbasyk3y82orsc0dmc82c9` requiere **recrear la tabla** porque:

1. La restricción está siendo usada por claves foráneas
2. No se puede eliminar directamente
3. La estructura actual tiene problemas heredados

---

## 📋 PASOS PARA EJECUTAR

### ⚠️ IMPORTANTE: Hacer Backup Primero

```sql
USE futbol_app;

-- Exportar datos por si acaso
SELECT * FROM alineaciones INTO OUTFILE '/tmp/alineaciones_backup.csv'
FIELDS TERMINATED BY ',' 
ENCLOSED BY '"'
LINES TERMINATED BY '\n';
```

O simplemente confía en el backup automático que hace el script.

---

## 🚀 EJECUTA ESTE SCRIPT

**Archivo:** `recrear-tabla-alineaciones.sql`

Copia y pega COMPLETO en tu cliente MySQL:

```sql
USE futbol_app;

-- ====================
-- PASO 1: BACKUP
-- ====================
CREATE TABLE alineaciones_backup AS SELECT * FROM alineaciones;
SELECT COUNT(*) as 'Registros respaldados' FROM alineaciones_backup;

-- ====================
-- PASO 2: ELIMINAR TABLA
-- ====================
DROP TABLE alineaciones;

-- ====================
-- PASO 3: RECREAR CORRECTAMENTE
-- ====================
CREATE TABLE alineaciones (
    id INT AUTO_INCREMENT PRIMARY KEY,
    
    partido_id INT NOT NULL,
    equipo_id INT NOT NULL,
    created_by INT,
    
    alineacion JSON NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- CLAVES FORÁNEAS
    CONSTRAINT fk_alineacion_partido
        FOREIGN KEY (partido_id)
        REFERENCES partidos(id)
        ON DELETE CASCADE,
    
    CONSTRAINT fk_alineacion_equipo
        FOREIGN KEY (equipo_id)
        REFERENCES equipos(id)
        ON DELETE CASCADE,
    
    CONSTRAINT fk_alineacion_created_by
        FOREIGN KEY (created_by)
        REFERENCES users(id)
        ON DELETE SET NULL,
    
    -- RESTRICCIÓN UNIQUE CORRECTA
    -- Permite múltiples usuarios, una alineación por usuario/partido/equipo
    CONSTRAINT uk_user_partido_equipo
        UNIQUE (created_by, partido_id, equipo_id)
);

-- ====================
-- PASO 4: RESTAURAR DATOS
-- ====================
INSERT INTO alineaciones (id, partido_id, equipo_id, created_by, alineacion, created_at)
SELECT id, partido_id, equipo_id, created_by, alineacion, created_at
FROM alineaciones_backup;

-- ====================
-- PASO 5: VERIFICAR
-- ====================
SELECT COUNT(*) as 'Registros restaurados' FROM alineaciones;

SHOW CREATE TABLE alineaciones;

SELECT 'Tabla recreada correctamente. Ahora múltiples usuarios pueden crear alineaciones.' AS Resultado;

-- ====================
-- PASO 6 (OPCIONAL): ELIMINAR BACKUP
-- Solo ejecuta esto DESPUÉS de verificar que todo funciona
-- ====================
-- DROP TABLE alineaciones_backup;
```

---

## ✅ QUÉ HACE ESTE SCRIPT

1. **Crea backup automático** de tus datos actuales
2. **Elimina la tabla** con problemas
3. **Recrea la tabla** con estructura correcta:
   - Restricción UNIQUE correcta: `(created_by, partido_id, equipo_id)`
   - Claves foráneas bien definidas
   - Sin restricciones problemáticas
4. **Restaura todos los datos** desde el backup
5. **Verifica** que todo se haya restaurado

---

## 🧪 VERIFICACIÓN

Después de ejecutar el script, verifica:

```sql
-- Ver la estructura
SHOW CREATE TABLE alineaciones;

-- Deberías ver SOLO:
-- CONSTRAINT `uk_user_partido_equipo` UNIQUE (`created_by`,`partido_id`,`equipo_id`)

-- NO deberías ver:
-- UKllrfbasyk3y82orsc0dmc82c9
```

---

## 🎮 PRUEBA EN LA APLICACIÓN

1. **Usuario 1:**
   - Ir a /crear-alineacion.html
   - Crear alineación para Barcelona, Partido 1
   - ✅ Debe funcionar

2. **Usuario 2:**
   - Ir a /crear-alineacion.html
   - Crear alineación para Barcelona, Partido 1
   - ✅ Debe funcionar (ANTES fallaba)

3. **Usuario 1 de nuevo:**
   - Intentar crear otra para Barcelona, Partido 1
   - ❌ Debe dar error "Ya tienes una alineación..."

---

## ⚠️ POSIBLES PROBLEMAS

### Problema 1: Error al crear alineaciones_backup

**Error:** `Table 'alineaciones_backup' already exists`

**Solución:**
```sql
DROP TABLE IF EXISTS alineaciones_backup;
-- Luego ejecuta el script de nuevo
```

### Problema 2: Error en INSERT (datos duplicados)

**Significa:** Ya tienes datos que violan la nueva restricción (mismo usuario, mismo partido, mismo equipo duplicado).

**Solución:**
```sql
-- Ver duplicados
SELECT created_by, partido_id, equipo_id, COUNT(*) 
FROM alineaciones_backup
GROUP BY created_by, partido_id, equipo_id
HAVING COUNT(*) > 1;

-- Eliminar duplicados del backup (deja solo el más reciente)
DELETE t1 FROM alineaciones_backup t1
INNER JOIN alineaciones_backup t2 
WHERE 
    t1.created_by = t2.created_by AND
    t1.partido_id = t2.partido_id AND
    t1.equipo_id = t2.equipo_id AND
    t1.id < t2.id;

-- Ahora ejecuta el INSERT de nuevo
```

### Problema 3: Error de Foreign Key

**Error:** `Cannot add foreign key constraint`

**Significa:** Las tablas referenciadas (partidos, equipos, users) no existen o tienen nombres diferentes.

**Solución:**
```sql
-- Verificar que existan las tablas
SHOW TABLES LIKE 'partidos';
SHOW TABLES LIKE 'equipos';
SHOW TABLES LIKE 'users';

-- Si alguna tiene otro nombre, ajusta el script
```

---

## 📊 ESTRUCTURA CORRECTA FINAL

```sql
CREATE TABLE `alineaciones` (
  `id` int NOT NULL AUTO_INCREMENT,
  `partido_id` int NOT NULL,
  `equipo_id` int NOT NULL,
  `created_by` int DEFAULT NULL,
  `alineacion` json NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  
  PRIMARY KEY (`id`),
  
  UNIQUE KEY `uk_user_partido_equipo` (`created_by`,`partido_id`,`equipo_id`),
  
  KEY `fk_alineacion_partido` (`partido_id`),
  KEY `fk_alineacion_equipo` (`equipo_id`),
  
  CONSTRAINT `fk_alineacion_created_by` 
    FOREIGN KEY (`created_by`) REFERENCES `users` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_alineacion_equipo` 
    FOREIGN KEY (`equipo_id`) REFERENCES `equipos` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_alineacion_partido` 
    FOREIGN KEY (`partido_id`) REFERENCES `partidos` (`id`) ON DELETE CASCADE
)
```

---

## 💡 POR QUÉ ESTA SOLUCIÓN

**Ventajas de recrear la tabla:**
1. ✅ Elimina TODOS los problemas de restricciones antiguas
2. ✅ Garantiza estructura limpia y correcta
3. ✅ Mantiene todos los datos
4. ✅ Sincroniza con el modelo JPA
5. ✅ Evita problemas futuros

**Vs. intentar arreglar restricciones:**
1. ❌ Complejo y propenso a errores
2. ❌ Puede dejar restos de restricciones antiguas
3. ❌ Requiere conocer nombres exactos de todas las FKs
4. ❌ Puede fallar por dependencias

---

## 📞 SI NECESITAS AYUDA

Envía el resultado de:

```sql
-- Antes de ejecutar el fix
SHOW CREATE TABLE alineaciones;

-- Si hay error
SELECT * FROM alineaciones_backup LIMIT 5;
```

---

**¡Ejecuta el script y el problema estará 100% resuelto! 🎉**
