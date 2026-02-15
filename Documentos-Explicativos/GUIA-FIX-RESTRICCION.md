# 🔧 GUÍA PASO A PASO: Corregir Restricción de Alineaciones

## 📋 PASOS PARA EJECUTAR EL FIX

### PASO 1: Identificar la Restricción Actual

Ejecuta este comando primero:

```sql
USE futbol_app;
SHOW INDEX FROM alineaciones;
```

**Busca en los resultados:**
- Una fila que tenga `Non_unique = 0` (es un índice único)
- Probablemente se llame `partido_id` o algo similar
- **Anota el nombre exacto** que aparece en la columna `Key_name`

**Ejemplo de resultado:**
```
Table       | Key_name    | Column_name | Non_unique
------------|-------------|-------------|------------
alineaciones| PRIMARY     | id          | 0
alineaciones| partido_id  | partido_id  | 0    ← Este es el problemático
alineaciones| partido_id  | equipo_id   | 0    ← Mismo índice
```

---

### PASO 2: Eliminar la Restricción Antigua

Usa el nombre que encontraste en el PASO 1:

```sql
-- Reemplaza 'partido_id' con el nombre que encontraste
ALTER TABLE alineaciones DROP INDEX `partido_id`;
```

**Si obtienes error "Can't DROP":**
- Significa que la restricción no existe con ese nombre
- Intenta con otro nombre que viste en el PASO 1
- O continúa al PASO 3 directamente

---

### PASO 3: Crear la Restricción Correcta

```sql
ALTER TABLE alineaciones
ADD CONSTRAINT uk_user_partido_equipo
UNIQUE (created_by, partido_id, equipo_id);
```

---

### PASO 4: Verificar

```sql
SHOW INDEX FROM alineaciones WHERE Key_name = 'uk_user_partido_equipo';
```

**Deberías ver 3 filas:**
```
Table       | Key_name              | Column_name
------------|----------------------|-------------
alineaciones| uk_user_partido_equipo| created_by
alineaciones| uk_user_partido_equipo| partido_id
alineaciones| uk_user_partido_equipo| equipo_id
```

---

## 🚨 SI HAY PROBLEMAS

### Problema 0: Error "Duplicate entry '1-2' for key 'alineaciones.UKllrfbasyk3y82orsc0dmc82c9'"

**Significa:** Después de crear la restricción correcta, la antigua TODAVÍA existe.

**Causa:** La restricción antigua tiene un nombre autogenerado por Hibernate (como `UKllrfbasyk3y82orsc0dmc82c9`).

**Solución - EJECUTA ESTO:**

```sql
USE futbol_app;

-- Ver todas las restricciones
SHOW INDEX FROM alineaciones;

-- Eliminar la restricción antigua (usa el nombre que aparece en TU error)
ALTER TABLE alineaciones DROP INDEX `UKllrfbasyk3y82orsc0dmc82c9`;

-- Verificar que solo quede uk_user_partido_equipo
SHOW INDEX FROM alineaciones WHERE Key_name LIKE 'uk_%' OR Key_name LIKE 'UK%';
```

**Nota:** El nombre `UKllrfbasyk3y82orsc0dmc82c9` puede ser diferente en tu caso. Usa el nombre exacto que aparece en el mensaje de error.

---

### Problema 1: "Duplicate entry" al crear la restricción

**Significa:** Ya tienes datos duplicados en la tabla.

**Solución:** Ver y limpiar duplicados:

```sql
-- Ver duplicados
SELECT partido_id, equipo_id, created_by, COUNT(*) as total
FROM alineaciones
GROUP BY partido_id, equipo_id, created_by
HAVING COUNT(*) > 1;

-- Si hay duplicados del MISMO usuario, eliminar los sobrantes:
-- (Ajusta los IDs según lo que veas)
DELETE FROM alineaciones WHERE id = ID_DEL_DUPLICADO;

-- Luego intenta crear la restricción de nuevo
```

### Problema 2: "Table doesn't exist"

**Solución:** Verifica que estás en la base de datos correcta:

```sql
USE futbol_app;
SHOW TABLES; -- Debería aparecer 'alineaciones'
```

### Problema 3: No puedo eliminar la restricción

**Solución:** Busca el nombre exacto:

```sql
SELECT CONSTRAINT_NAME
FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
WHERE TABLE_SCHEMA = 'futbol_app' 
  AND TABLE_NAME = 'alineaciones'
  AND CONSTRAINT_TYPE = 'UNIQUE';

-- Usa el nombre que aparezca:
ALTER TABLE alineaciones DROP INDEX `nombre_que_apareció`;
```

---

## ✅ COMANDO COMPLETO (COPIA Y PEGA)

```sql
USE futbol_app;

-- 1. Ver restricciones actuales
SHOW INDEX FROM alineaciones;

-- 2. Eliminar la restricción antigua (ajusta el nombre si es diferente)
ALTER TABLE alineaciones DROP INDEX `partido_id`;

-- 3. Crear la restricción correcta
ALTER TABLE alineaciones
ADD CONSTRAINT uk_user_partido_equipo
UNIQUE (created_by, partido_id, equipo_id);

-- 4. Verificar
SHOW INDEX FROM alineaciones WHERE Key_name = 'uk_user_partido_equipo';

SELECT 'Fix aplicado correctamente' AS Resultado;
```

---

## 🧪 PRUEBA DESPUÉS DEL FIX

### Desde la Aplicación:

1. **Usuario 1:**
   - Ir a /crear-alineacion.html
   - Crear alineación para Barcelona en Partido 1
   - ✅ Debe funcionar

2. **Usuario 2:**
   - Ir a /crear-alineacion.html
   - Crear alineación para Barcelona en Partido 1
   - ✅ Debe funcionar (antes daba error)

3. **Usuario 1 de nuevo:**
   - Intentar crear otra alineación para Barcelona en Partido 1
   - ❌ Debe dar error "Ya tienes una alineación..."

---

## 📞 AYUDA ADICIONAL

Si algo sale mal:

1. **Hacer backup primero:**
   ```sql
   -- Exportar solo la tabla alineaciones
   SELECT * FROM alineaciones INTO OUTFILE '/tmp/alineaciones_backup.csv';
   ```

2. **Ver estructura de la tabla:**
   ```sql
   SHOW CREATE TABLE alineaciones;
   ```

3. **Ver todos los índices:**
   ```sql
   SHOW INDEX FROM alineaciones;
   ```

4. **Contactar con los logs del error:**
   - Copia el error completo
   - Copia el resultado de `SHOW INDEX FROM alineaciones;`
   - Copia el resultado de `SHOW CREATE TABLE alineaciones;`

---

**¡Con estos pasos deberías poder corregir la restricción sin problemas! 🎉**
