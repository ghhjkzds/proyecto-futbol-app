# ✅ MIGRACIÓN DE VOTOS: DE EQUIPOS A ALINEACIONES INDIVIDUALES

## 🎯 Objetivo

**Antes:** Los votos se guardaban en la tabla `equipos`, lo que hacía que todas las alineaciones del mismo equipo compartieran los mismos votos.

**Ahora:** Cada alineación tiene sus propios votos independientes, permitiendo que diferentes alineaciones del mismo equipo compitan entre sí.

---

## 🔄 Cambios Realizados

### 1. Base de Datos

**Archivo:** `agregar-votos-alineaciones-individuales.sql`

Se agregó la columna `votos` a la tabla `alineaciones`:

```sql
ALTER TABLE alineaciones
    ADD COLUMN votos INT DEFAULT 0 AFTER alineacion;
```

**Estructura anterior:**
```
alineaciones
├── id
├── partido_id
├── equipo_id
├── created_by
├── alineacion (JSON)
└── created_at

equipos
├── id
├── nombre
├── votos ← Los votos estaban aquí
└── ...
```

**Estructura nueva:**
```
alineaciones
├── id
├── partido_id
├── equipo_id
├── created_by
├── alineacion (JSON)
├── votos ← NUEVO: Votos individuales por alineación
└── created_at

equipos
├── id
├── nombre
├── votos ← Opcional: Se puede mantener o eliminar
└── ...
```

---

### 2. Modelo Backend (Java)

#### `Alineacion.java`

**Antes:**
```java
@Entity
public class Alineacion {
    private Integer id;
    private Partido partido;
    private Equipo equipo;
    private EquipoDetalles alineacion;
    private User createdBy;
    private LocalDateTime createdAt;
    // NO había campo votos
}
```

**Ahora:**
```java
@Entity
public class Alineacion {
    private Integer id;
    private Partido partido;
    private Equipo equipo;
    private EquipoDetalles alineacion;
    
    @Column(name = "votos", nullable = false)
    private Integer votos = 0;  // ← NUEVO
    
    private User createdBy;
    private LocalDateTime createdAt;
}
```

---

### 3. DTO de Transferencia

#### `AlineacionDTO.java`

**Antes:**
```java
public class AlineacionDTO {
    // ... otros campos
    private Integer equipoVotos; // Votos del equipo
}
```

**Ahora:**
```java
public class AlineacionDTO {
    // ... otros campos
    private Integer votos; // Votos de esta alineación específica
}
```

**Impacto:** Este cambio afecta cómo el frontend recibe los datos.

---

### 4. Controller (AlineacionController.java)

#### Método: `getAlineacionesPorPartido()`

**Antes:** Ordenaba por `equipo.getVotos()`
```java
.sorted((a1, a2) -> {
    int votos1 = a1.getEquipo().getVotos();
    int votos2 = a2.getEquipo().getVotos();
    return Integer.compare(votos2, votos1);
})
```

**Ahora:** Ordena por `alineacion.getVotos()`
```java
.sorted((a1, a2) -> {
    int votos1 = a1.getVotos() != null ? a1.getVotos() : 0;
    int votos2 = a2.getVotos() != null ? a2.getVotos() : 0;
    return Integer.compare(votos2, votos1); // Descendente
})
```

---

#### Método: `votarAlineacion()`

**Antes:** Incrementaba votos del equipo
```java
@PostMapping("/{id}/votar")
public ResponseEntity<?> votarAlineacion(@PathVariable Integer id) {
    Alineacion alineacion = alineacionRepository.findById(id)...
    
    Equipo equipo = alineacion.getEquipo();
    equipo.setVotos(equipo.getVotos() + 1);
    equipoRepository.save(equipo); // Guardaba en tabla equipos
    
    return ResponseEntity.ok(...);
}
```

**Ahora:** Incrementa votos de la alineación específica
```java
@PostMapping("/{id}/votar")
public ResponseEntity<?> votarAlineacion(@PathVariable Integer id) {
    Alineacion alineacion = alineacionRepository.findById(id)...
    
    int votosActuales = alineacion.getVotos() != null ? alineacion.getVotos() : 0;
    alineacion.setVotos(votosActuales + 1);
    alineacionRepository.save(alineacion); // Guarda en tabla alineaciones
    
    return ResponseEntity.ok(...);
}
```

**Cambios clave:**
- Ya NO modifica la tabla `equipos`
- Incrementa el campo `votos` de la entidad `Alineacion`
- Cada alineación es independiente

---

#### Método: `convertToDTO()`

**Antes:**
```java
private AlineacionDTO convertToDTO(Alineacion alineacion) {
    return new AlineacionDTO(
        // ... otros campos
        equipo.getVotos() != null ? equipo.getVotos() : 0, // Del equipo
        // ... otros campos
    );
}
```

**Ahora:**
```java
private AlineacionDTO convertToDTO(Alineacion alineacion) {
    return new AlineacionDTO(
        // ... otros campos
        alineacion.getVotos() != null ? alineacion.getVotos() : 0, // De la alineación
        // ... otros campos
    );
}
```

---

### 5. Frontend (ver-alineaciones.html)

#### Función: `crearCardAlineacion()`

**Antes:**
```javascript
const votos = alineacion.equipoVotos || 0;
console.log('Votos:', alineacion.equipoVotos);
```

**Ahora:**
```javascript
const votos = alineacion.votos || 0;
console.log('Votos:', alineacion.votos);
```

**Impacto:** El frontend ahora lee `votos` en lugar de `equipoVotos` del DTO.

---

## 📊 Flujo de Votación Actualizado

### Antes (Votos por Equipo):

```
Usuario vota alineación X
         ↓
Backend recibe alineacionId
         ↓
Busca la alineación
         ↓
Obtiene el equipo de la alineación
         ↓
Incrementa equipo.votos
         ↓
Guarda en tabla EQUIPOS
         ↓
TODAS las alineaciones del mismo equipo muestran los mismos votos
```

**Problema:** Si el equipo "Real Madrid" tiene 3 alineaciones diferentes, todas mostraban los mismos votos acumulados.

---

### Ahora (Votos por Alineación Individual):

```
Usuario vota alineación X
         ↓
Backend recibe alineacionId
         ↓
Busca la alineación X
         ↓
Incrementa alineacion.votos
         ↓
Guarda en tabla ALINEACIONES
         ↓
Solo la alineación X muestra el voto
```

**Ventaja:** Cada alineación tiene su propia puntuación independiente.

---

## 🎯 Escenario de Ejemplo

### Situación:

- **Partido:** Real Madrid vs Barcelona
- **Usuario1** crea alineación para Real Madrid → Alineación A
- **Usuario2** crea alineación para Real Madrid → Alineación B
- **Usuario3** crea alineación para Barcelona → Alineación C

### Antes (Sistema Antiguo):

| Alineación | Equipo | Votos Mostrados |
|------------|--------|-----------------|
| A | Real Madrid | 5 |
| B | Real Madrid | 5 |
| C | Barcelona | 3 |

Si votabas por A o por B, ambas mostraban el mismo contador (porque votabas al equipo).

### Ahora (Sistema Nuevo):

| Alineación | Equipo | Votos Individuales |
|------------|--------|--------------------|
| A | Real Madrid | 3 |
| B | Real Madrid | 2 |
| C | Barcelona | 3 |

Cada alineación tiene su propia puntuación. Las alineaciones A y B compiten entre sí aunque sean del mismo equipo.

---

## 🚀 Pasos para Aplicar los Cambios

### 1. Ejecutar Script SQL ⚠️

**IMPORTANTE:** Debes ejecutar este script en tu base de datos MySQL.

Archivo: `agregar-votos-alineaciones-individuales.sql`

```sql
USE futbol_app;

-- Agregar columna votos a alineaciones
ALTER TABLE alineaciones
    ADD COLUMN votos INT DEFAULT 0 AFTER alineacion;

-- Verificar
SHOW CREATE TABLE alineaciones;
```

**Opciones:**

1. **Abre MySQL Workbench** → Nueva Query → Pega el script → Ejecuta
2. **Línea de comandos:**
   ```bash
   mysql -u root -p futbol_app < agregar-votos-alineaciones-individuales.sql
   ```

---

### 2. Reiniciar la Aplicación

Después de ejecutar el SQL:

1. **Detener la aplicación** (si está corriendo)
2. **Recompilar (opcional):**
   ```bash
   mvnw clean compile
   ```
3. **Iniciar nuevamente:**
   ```bash
   mvnw spring-boot:run
   ```

O simplemente hacer clic en "Run" en tu IDE.

---

### 3. Verificar el Cambio

1. **Ir a:** `http://localhost:8081/ver-alineaciones.html`
2. **Seleccionar un partido**
3. **Ver alineaciones**
4. **Votar por una alineación**
5. **Verificar que:**
   - Solo esa alineación incrementa sus votos
   - Otras alineaciones del mismo equipo NO cambian
   - Los votos se mantienen al refrescar la página

---

## 🔍 Verificación en Base de Datos

### Comprobar que la columna existe:

```sql
USE futbol_app;
DESC alineaciones;
```

**Deberías ver:**
```
+-------------+--------------+------+-----+
| Field       | Type         | Null | Key |
+-------------+--------------+------+-----+
| id          | int          | NO   | PRI |
| partido_id  | int          | NO   | MUL |
| equipo_id   | int          | NO   | MUL |
| created_by  | int          | YES  | MUL |
| alineacion  | json         | NO   |     |
| votos       | int          | NO   |     |  ← NUEVA COLUMNA
| created_at  | timestamp    | NO   |     |
+-------------+--------------+------+-----+
```

---

### Ver votos actuales:

```sql
SELECT 
    a.id AS alineacion_id,
    e.nombre AS equipo,
    u.email AS usuario,
    a.votos,
    a.created_at
FROM alineaciones a
JOIN equipos e ON a.equipo_id = e.id
JOIN users u ON a.created_by = u.id
ORDER BY a.votos DESC;
```

**Ejemplo de resultado:**
```
+---------------+--------------+------------------+-------+---------------------+
| alineacion_id | equipo       | usuario          | votos | created_at          |
+---------------+--------------+------------------+-------+---------------------+
| 5             | Real Madrid  | user1@email.com  | 8     | 2026-02-09 10:30:00 |
| 3             | Barcelona    | user2@email.com  | 5     | 2026-02-09 09:15:00 |
| 7             | Real Madrid  | user3@email.com  | 2     | 2026-02-09 11:45:00 |
+---------------+--------------+------------------+-------+---------------------+
```

---

## 📈 Ventajas del Nuevo Sistema

### ✅ Competencia justa
Cada usuario puede crear su propia alineación y competir independientemente, incluso si eligen el mismo equipo.

### ✅ Granularidad
Los votos reflejan la calidad de cada alineación específica, no del equipo en general.

### ✅ Escalabilidad
Puedes tener múltiples alineaciones del mismo equipo en el mismo partido sin conflictos.

### ✅ Claridad
Los usuarios entienden que están votando por una alineación específica, no por un equipo.

---

## 🔄 Migración de Datos Existentes (Opcional)

Si ya tienes votos en la tabla `equipos` y quieres migrarlos:

```sql
-- Distribuir los votos del equipo entre todas sus alineaciones
UPDATE alineaciones a
JOIN equipos e ON a.equipo_id = e.id
SET a.votos = FLOOR(e.votos / (
    SELECT COUNT(*) 
    FROM alineaciones a2 
    WHERE a2.equipo_id = e.id
))
WHERE e.votos > 0;
```

**Nota:** Esto distribuye los votos equitativamente. Si prefieres resetear a 0, simplemente no hagas nada (el default es 0).

---

## 🐛 Troubleshooting

### Problema: "Cannot resolve column 'votos'"
**Causa:** El script SQL no se ejecutó  
**Solución:** Ejecuta `agregar-votos-alineaciones-individuales.sql`

---

### Problema: Los votos siguen siendo los mismos para todas las alineaciones
**Causa:** Estás viendo datos cacheados o no reiniciaste la aplicación  
**Solución:**
1. Ejecuta el script SQL
2. Reinicia la aplicación
3. Limpia la caché del navegador (Ctrl + Shift + R)

---

### Problema: Error 500 al votar
**Causa:** La columna `votos` no existe en la base de datos  
**Solución:** Ejecuta el script SQL y reinicia

---

## 📊 Comparativa de Respuestas API

### Antes:

```json
{
  "id": 1,
  "equipoId": 5,
  "equipoNombre": "Real Madrid",
  "equipoVotos": 10,  ← Votos del equipo (compartidos)
  "alineacion": {...}
}
```

### Ahora:

```json
{
  "id": 1,
  "equipoId": 5,
  "equipoNombre": "Real Madrid",
  "votos": 3,  ← Votos de ESTA alineación específica
  "alineacion": {...}
}
```

---

## ✅ Checklist de Implementación

- [x] **Modelo actualizado** - Campo `votos` en `Alineacion.java`
- [x] **DTO actualizado** - Campo `votos` en `AlineacionDTO.java`
- [x] **Controller actualizado** - Métodos modificados
- [x] **Frontend actualizado** - Usa `votos` en lugar de `equipoVotos`
- [x] **Compilación exitosa** - Sin errores
- [ ] **Script SQL ejecutado** - **PENDIENTE DE EJECUTAR**
- [ ] **Aplicación reiniciada** - Después de ejecutar SQL
- [ ] **Pruebas funcionales** - Verificar que funciona correctamente

---

## 🎉 Resultado Final

**Cada alineación ahora tiene su propia puntuación independiente**, permitiendo:

1. **Múltiples alineaciones del mismo equipo** en un partido
2. **Competencia justa** entre todas las alineaciones
3. **Votos específicos** por la calidad de cada alineación
4. **Ranking preciso** basado en mérito individual

---

## 📝 Archivos Modificados

| Archivo | Tipo | Cambios |
|---------|------|---------|
| `agregar-votos-alineaciones-individuales.sql` | SQL | **NUEVO** - Script de migración |
| `src/.../model/Alineacion.java` | Backend | Campo `votos` agregado |
| `src/.../dto/AlineacionDTO.java` | Backend | `equipoVotos` → `votos` |
| `src/.../controller/AlineacionController.java` | Backend | Lógica de votación actualizada |
| `src/.../static/ver-alineaciones.html` | Frontend | `equipoVotos` → `votos` |

---

**Fecha:** 2026-02-09  
**Versión:** 2.0  
**Estado:** ✅ Código listo - ⏳ Pendiente ejecución SQL
