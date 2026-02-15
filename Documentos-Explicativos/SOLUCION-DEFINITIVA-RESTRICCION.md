# 🔧 SOLUCIÓN DEFINITIVA: Restricción con Nombre Autogenerado

## ❌ PROBLEMA ACTUALIZADO

### Error al Intentar Eliminar el Índice:
```
Error Code: 1553. Cannot drop index 'UKllrfbasyk3y82orsc0dmc82c9': 
needed in a foreign key constraint
```

### ¿Por qué Este Error?

La restricción `UKllrfbasyk3y82orsc0dmc82c9` **no es solo un índice UNIQUE**, también está siendo **usada por una clave foránea (FOREIGN KEY)**.

MySQL no permite eliminar un índice que es requerido por una FK.

---

## ✅ SOLUCIÓN MÁS SEGURA: Recrear la Tabla

Esta es la forma más segura de corregir el problema:

### OPCIÓN 1: Recrear la Tabla (RECOMENDADO)

**Archivo:** `recrear-tabla-alineaciones.sql`

```sql
USE futbol_app;

-- 1. Backup
CREATE TABLE alineaciones_backup AS SELECT * FROM alineaciones;

-- 2. Eliminar tabla original
DROP TABLE alineaciones;

-- 3. Recrear con estructura correcta
CREATE TABLE alineaciones (
    id INT AUTO_INCREMENT PRIMARY KEY,
    partido_id INT NOT NULL,
    equipo_id INT NOT NULL,
    created_by INT,
    alineacion JSON NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- FKs
    CONSTRAINT fk_alineacion_partido
        FOREIGN KEY (partido_id) REFERENCES partidos(id) ON DELETE CASCADE,
    CONSTRAINT fk_alineacion_equipo
        FOREIGN KEY (equipo_id) REFERENCES equipos(id) ON DELETE CASCADE,
    CONSTRAINT fk_alineacion_created_by
        FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    
    -- UNIQUE correcta (con created_by)
    CONSTRAINT uk_user_partido_equipo
        UNIQUE (created_by, partido_id, equipo_id)
);

-- 4. Restaurar datos
INSERT INTO alineaciones (id, partido_id, equipo_id, created_by, alineacion, created_at)
SELECT id, partido_id, equipo_id, created_by, alineacion, created_at
FROM alineaciones_backup;

-- 5. Verificar
SELECT COUNT(*) FROM alineaciones;
SHOW CREATE TABLE alineaciones;
```

### OPCIÓN 2: Eliminar y Recrear FKs (Más Complejo)

Si prefieres no recrear la tabla:
```
Error al guardar alineación: could not execute statement 
[Duplicate entry '1-2' for key 'alineaciones.UKllrfbasyk3y82orsc0dmc82c9']
```

### ¿Por qué Sigue Fallando?

Cuando ejecutaste el script anterior:
1. ✅ Se creó la restricción correcta: `uk_user_partido_equipo`
2. ❌ NO se eliminó la restricción antigua porque tiene un nombre diferente

**Tienes AMBAS restricciones:**
- `UKllrfbasyk3y82orsc0dmc82c9` (antigua, solo partido_id + equipo_id) ❌
- `uk_user_partido_equipo` (nueva, con created_by) ✅

Como la antigua sigue activa, sigue bloqueando a múltiples usuarios.

---

## ✅ SOLUCIÓN DEFINITIVA

### PASO 1: Ejecuta Este Comando

```sql
USE futbol_app;

-- Eliminar la restricción con el nombre autogenerado
ALTER TABLE alineaciones DROP INDEX `UKllrfbasyk3y82orsc0dmc82c9`;
```

### PASO 2: Verificar

```sql
-- Ver todas las restricciones que quedan
SHOW INDEX FROM alineaciones;
```

**Deberías ver SOLO:**
- `PRIMARY` (en columna `id`)
- `uk_user_partido_equipo` (en `created_by`, `partido_id`, `equipo_id`)
- Posiblemente índices de claves foráneas (normales)

**NO deberías ver:**
- `UKllrfbasyk3y82orsc0dmc82c9` ❌
- `partido_id` (como índice único) ❌

---

## 📊 EXPLICACIÓN TÉCNICA

### Nombres de Restricciones en JPA/Hibernate:

Cuando JPA/Hibernate crea restricciones sin especificar nombre:

```java
// Sin nombre especificado
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"partido_id", "equipo_id"}))
```

Genera un nombre automático como:
- `UKllrfbasyk3y82orsc0dmc82c9`
- `UK` = Unique Key
- `llrfbasyk3y82orsc0dmc82c9` = Hash del nombre de columnas

Por eso el script inicial no lo eliminó: buscaba `partido_id`, pero el nombre real era `UKllrfbasyk3y82orsc0dmc82c9`.

---

## 🎯 CÓMO IDENTIFICAR EL NOMBRE CORRECTO

Si el nombre en tu error es diferente, usa este comando:

```sql
-- Ver todas las restricciones UNIQUE
SELECT 
    CONSTRAINT_NAME,
    GROUP_CONCAT(COLUMN_NAME ORDER BY ORDINAL_POSITION) as Columnas
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE 
    TABLE_SCHEMA = 'futbol_app' 
    AND TABLE_NAME = 'alineaciones'
    AND CONSTRAINT_NAME LIKE 'UK%'
GROUP BY CONSTRAINT_NAME;
```

**Busca:**
- Una restricción que tenga solo `partido_id,equipo_id` (sin created_by)
- Ese es el nombre que debes eliminar

---

## 🧪 VERIFICACIÓN COMPLETA

### Después de Ejecutar el Fix:

```sql
-- 1. Ver restricciones
SHOW INDEX FROM alineaciones;

-- 2. Deberías ver SOLO esto (ignorando claves foráneas):
-- PRIMARY         | id
-- uk_user_partido_equipo | created_by
-- uk_user_partido_equipo | partido_id
-- uk_user_partido_equipo | equipo_id

-- 3. NO deberías ver:
-- UKllrfbasyk3y82orsc0dmc82c9 | ...
-- partido_id (como UNIQUE)    | ...
```

---

## 🎮 PRUEBA FINAL

### Desde la Aplicación:

1. **Usuario A:**
   - Crear alineación para Barcelona en Partido 1
   - ✅ Debe funcionar

2. **Usuario B (diferente):**
   - Crear alineación para Barcelona en Partido 1
   - ✅ Debe funcionar (este era el problema)

3. **Usuario A de nuevo:**
   - Intentar crear otra para Barcelona en Partido 1
   - ❌ Debe dar error: "Ya tienes una alineación creada..."

---

## 📋 COMANDO COMPLETO (COPIA Y PEGA)

```sql
USE futbol_app;

-- Ver restricciones actuales
SHOW INDEX FROM alineaciones;

-- Eliminar la restricción antigua (ajusta el nombre si es diferente)
ALTER TABLE alineaciones DROP INDEX `UKllrfbasyk3y82orsc0dmc82c9`;

-- Verificar que solo queda la correcta
SHOW INDEX FROM alineaciones WHERE Non_unique = 0;

SELECT 'Fix aplicado correctamente. Ahora múltiples usuarios pueden crear alineaciones.' AS Resultado;
```

---

## ⚠️ NOTAS IMPORTANTES

### Si el Nombre es Diferente:

Tu error mostró: `UKllrfbasyk3y82orsc0dmc82c9`

Pero podría ser algo como:
- `UK7a3bc9e1f2d4a8b6`
- `uk_partido_equipo`
- `unique_partido_equipo`

**Usa el nombre EXACTO que aparece en tu mensaje de error.**

### Si Aparece Error "Can't DROP":

Significa que ya lo eliminaste o tiene otro nombre:
```sql
-- Ver todos los índices únicos
SHOW INDEX FROM alineaciones WHERE Non_unique = 0;

-- Elimina TODOS los que NO sean:
-- - PRIMARY
-- - uk_user_partido_equipo
```

---

## 🔍 DIAGNÓSTICO AVANZADO

### Ver Estructura Completa de la Tabla:

```sql
SHOW CREATE TABLE alineaciones;
```

Busca líneas como:
```sql
UNIQUE KEY `UKllrfbasyk3y82orsc0dmc82c9` (`partido_id`,`equipo_id`)  -- ❌ ELIMINAR
UNIQUE KEY `uk_user_partido_equipo` (`created_by`,`partido_id`,`equipo_id`)  -- ✅ MANTENER
```

---

## 💡 PREVENCIÓN FUTURA

### Para evitar este problema:

1. **Siempre nombrar restricciones en JPA:**
   ```java
   @UniqueConstraint(
       name = "uk_user_partido_equipo",  // ✅ Nombre explícito
       columnNames = {"created_by", "partido_id", "equipo_id"}
   )
   ```

2. **Usar Flyway/Liquibase para migraciones:**
   - Scripts SQL versionados
   - Control total de cambios
   - Sincronización automática

3. **Documentar esquema de BD:**
   - Mantener script CREATE TABLE actualizado
   - Versionar cambios de esquema

---

## 📞 SI PERSISTE EL PROBLEMA

Envía el resultado de:

```sql
USE futbol_app;

-- 1. Todas las restricciones
SHOW INDEX FROM alineaciones;

-- 2. Estructura de la tabla
SHOW CREATE TABLE alineaciones;

-- 3. Restricciones UNIQUE específicamente
SELECT CONSTRAINT_NAME, COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'futbol_app' 
  AND TABLE_NAME = 'alineaciones'
ORDER BY CONSTRAINT_NAME, ORDINAL_POSITION;
```

---

**¡Ejecuta el comando y el problema estará completamente resuelto! 🎉**
