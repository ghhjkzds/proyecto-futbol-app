# 🔧 FIX: Error de Restricción UNIQUE en Alineaciones

## ❌ PROBLEMA IDENTIFICADO

### Error al Guardar Alineación:
```
could not execute statement [Duplicate entry '1-2' for key 'alineaciones.partido_id'] 
[insert into alineaciones (alineacion,created_at,created_by,equipo_id,partido_id) values (cast(? as json),?,?,?,?)]; 
SQL [insert into alineaciones (alineacion,created_at,created_by,equipo_id,partido_id) values (cast(? as json),?,?,?,?)]; 
constraint [alineaciones.partido_id]
```

### Situación:
- **Usuario A** crea una alineación para Barcelona en el partido 1
- **Usuario B** intenta crear una alineación para Barcelona en el mismo partido 1
- **Error:** Se rechaza porque ya existe `partido_id=1, equipo_id=2`

---

## 🔍 CAUSA RAÍZ

### Restricción Actual en la Base de Datos:
```sql
UNIQUE (partido_id, equipo_id)  -- ❌ INCORRECTA
```

Esta restricción permite **solo UNA alineación** por partido/equipo en TOTAL, sin importar el usuario.

### Restricción Definida en el Código Java:
```java
@UniqueConstraint(
    name = "uk_user_partido_equipo",
    columnNames = {"created_by", "partido_id", "equipo_id"}  // ✅ CORRECTA
)
```

Esta restricción permite **una alineación por USUARIO** por partido/equipo.

### Discrepancia:
**La base de datos NO está sincronizada con el modelo JPA.**

---

## ✅ SOLUCIÓN

### 1. Ejecutar Script SQL

Ejecuta el archivo `fix-alineaciones-constraint.sql`:

```sql
-- Eliminar restricción antigua
ALTER TABLE alineaciones DROP INDEX IF EXISTS `partido_id`;

-- Crear restricción correcta
ALTER TABLE alineaciones
ADD CONSTRAINT uk_user_partido_equipo 
UNIQUE (created_by, partido_id, equipo_id);
```

### 2. Pasos para Aplicar:

#### Opción A: Desde MySQL Workbench / phpMyAdmin
```
1. Abrir MySQL Workbench / phpMyAdmin
2. Conectar a la base de datos `futbol_app`
3. Abrir el archivo fix-alineaciones-constraint.sql
4. Ejecutar el script completo
5. Verificar que se aplicó correctamente
```

#### Opción B: Desde Línea de Comandos
```bash
# Windows
mysql -u root -p futbol_app < fix-alineaciones-constraint.sql

# Linux/Mac
mysql -u root -p futbol_app < fix-alineaciones-constraint.sql
```

#### Opción C: Manualmente
```sql
USE futbol_app;

-- Ver restricciones actuales
SHOW CREATE TABLE alineaciones;

-- Eliminar la restricción problemática
ALTER TABLE alineaciones DROP INDEX partido_id;

-- Crear la restricción correcta
ALTER TABLE alineaciones
ADD CONSTRAINT uk_user_partido_equipo 
UNIQUE (created_by, partido_id, equipo_id);

-- Verificar
SHOW INDEX FROM alineaciones WHERE Key_name = 'uk_user_partido_equipo';
```

---

## 📊 ANTES vs DESPUÉS

### ANTES (Incorrecto):
```
Restricción: UNIQUE(partido_id, equipo_id)

Datos permitidos:
✅ Usuario 1 → Partido 1, Equipo Barcelona
❌ Usuario 2 → Partido 1, Equipo Barcelona  (ERROR!)

Resultado: Solo UN usuario puede crear alineación por partido/equipo
```

### DESPUÉS (Correcto):
```
Restricción: UNIQUE(created_by, partido_id, equipo_id)

Datos permitidos:
✅ Usuario 1 → Partido 1, Equipo Barcelona
✅ Usuario 2 → Partido 1, Equipo Barcelona
✅ Usuario 3 → Partido 1, Equipo Barcelona
❌ Usuario 1 → Partido 1, Equipo Barcelona (duplicado del mismo usuario)

Resultado: CADA usuario puede tener UNA alineación por partido/equipo
```

---

## 🎯 COMPORTAMIENTO ESPERADO

### Escenario: Partido Barcelona vs Real Madrid (ID: 1)

| Usuario | Equipo | Alineaciones Permitidas |
|---------|--------|------------------------|
| Juan | Barcelona | ✅ 1 alineación |
| Juan | Real Madrid | ✅ 1 alineación |
| María | Barcelona | ✅ 1 alineación |
| María | Real Madrid | ✅ 1 alineación |
| Pedro | Barcelona | ✅ 1 alineación |

**Total permitido:** Cada usuario puede crear hasta **2 alineaciones** por partido (una por cada equipo).

**Restricción:** Un usuario **NO** puede crear dos alineaciones para el mismo equipo en el mismo partido.

---

## 🔍 CÓMO VERIFICAR

### 1. Ver Restricciones Actuales:
```sql
SHOW CREATE TABLE alineaciones;
```

### 2. Ver Índices:
```sql
SHOW INDEX FROM alineaciones;
```

### 3. Buscar la Restricción Correcta:
```sql
SELECT 
    CONSTRAINT_NAME,
    COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE 
    TABLE_SCHEMA = 'futbol_app' 
    AND TABLE_NAME = 'alineaciones'
    AND CONSTRAINT_NAME = 'uk_user_partido_equipo';
```

**Resultado esperado:**
```
uk_user_partido_equipo | created_by
uk_user_partido_equipo | partido_id
uk_user_partido_equipo | equipo_id
```

---

## 🧪 PRUEBAS

### Después de Aplicar el Fix:

#### Prueba 1: Diferentes Usuarios, Mismo Equipo
```
1. Usuario1 → Crea alineación para Barcelona (Partido 1)
   ✅ Debe funcionar

2. Usuario2 → Crea alineación para Barcelona (Partido 1)
   ✅ Debe funcionar (ANTES fallaba)

3. Usuario1 → Intenta crear otra para Barcelona (Partido 1)
   ❌ Debe dar error (duplicado del mismo usuario)
```

#### Prueba 2: Mismo Usuario, Diferentes Equipos
```
1. Usuario1 → Crea alineación para Barcelona (Partido 1)
   ✅ Debe funcionar

2. Usuario1 → Crea alineación para Real Madrid (Partido 1)
   ✅ Debe funcionar

3. Usuario1 → Intenta crear otra para Barcelona (Partido 1)
   ❌ Debe dar error (duplicado)
```

---

## 📝 CÓMO OCURRIÓ ESTE PROBLEMA

### Posibles Causas:

1. **Script SQL inicial incorrecto:**
   - El script de creación de BD tenía `UNIQUE(partido_id, equipo_id)`
   - No incluía `created_by`

2. **Base de datos creada antes de actualizar el modelo:**
   - El modelo JPA se actualizó después
   - La BD no se regeneró

3. **Migración manual de esquema:**
   - Se creó la tabla manualmente
   - No se siguió el modelo JPA

---

## 🛡️ PREVENCIÓN FUTURA

### Opciones para Mantener Sincronización:

#### 1. Usar `spring.jpa.hibernate.ddl-auto=update`
```properties
# En application.properties (SOLO DESARROLLO)
spring.jpa.hibernate.ddl-auto=update
```
✅ Ventaja: Sincroniza automáticamente
❌ Desventaja: Peligroso en producción

#### 2. Usar Flyway / Liquibase
```xml
<!-- En pom.xml -->
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
```
✅ Ventaja: Migraciones controladas y versionadas
✅ Desventaja: Requiere configuración inicial

#### 3. Scripts SQL Manuales
- Mantener scripts SQL actualizados
- Ejecutarlos manualmente al actualizar modelos
- Documentar cambios

---

## ⚠️ IMPORTANTE

### Antes de Ejecutar el Fix:

1. **Hacer backup de la base de datos:**
   ```sql
   mysqldump -u root -p futbol_app > backup_antes_fix.sql
   ```

2. **Verificar datos existentes:**
   ```sql
   -- Ver si hay alineaciones duplicadas (mismo partido/equipo, diferentes usuarios)
   SELECT partido_id, equipo_id, COUNT(*) as total
   FROM alineaciones
   GROUP BY partido_id, equipo_id
   HAVING COUNT(*) > 1;
   ```

3. **Si hay duplicados legítimos (diferentes usuarios):**
   - El fix funcionará sin problemas
   - Se conservarán todos los registros

4. **Si la restricción antigua no se puede eliminar:**
   ```sql
   -- Ver el nombre real de la restricción
   SHOW CREATE TABLE alineaciones;
   
   -- Eliminar usando el nombre correcto
   ALTER TABLE alineaciones DROP INDEX nombre_real_de_la_restriccion;
   ```

---

## 📚 REFERENCIAS

### Documentos Relacionados:
- `RESUMEN-SESION-FINAL.md` - Estado actual del sistema
- `REVERSION-A-PARTIDOS-BD.md` - Cambios recientes
- Modelo JPA: `src/main/java/com/futbol/proyectoacd/model/Alineacion.java`

### Lógica de Negocio:
- Un usuario puede crear múltiples alineaciones en diferentes partidos
- Un usuario puede crear una alineación para cada equipo en un partido
- Un usuario NO puede crear dos alineaciones para el mismo equipo en el mismo partido

---

## ✅ RESULTADO ESPERADO

Después de aplicar el fix:

1. ✅ Múltiples usuarios pueden crear alineaciones para el mismo equipo/partido
2. ✅ Cada usuario sigue limitado a una alineación por equipo por partido
3. ✅ La restricción de la BD coincide con el modelo JPA
4. ✅ No más errores de "Duplicate entry"

---

**¡Aplica el fix y el sistema funcionará correctamente para múltiples usuarios! 🎉**
