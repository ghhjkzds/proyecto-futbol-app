# ✅ IMPLEMENTADO - Restricción: Una Alineación por Usuario por Equipo por Partido

## 🎯 Funcionalidad Implementada

He modificado la lógica del sistema para que **cada usuario solo pueda crear UNA alineación por equipo por partido**.

### Ejemplo Práctico:

```
Partido: Betis vs Barcelona

Usuario ID 1:
✅ Puede crear 1 alineación para el Betis
✅ Puede crear 1 alineación para el Barcelona
❌ NO puede crear otra alineación para el Betis en este partido
❌ NO puede crear otra alineación para el Barcelona en este partido

Siguiente Partido: Barcelona vs Real Madrid

Usuario ID 1:
✅ Puede crear 1 alineación para el Barcelona (nuevo partido)
✅ Puede crear 1 alineación para el Real Madrid (nuevo partido)
```

---

## 📦 Cambios Implementados

### 1. **Backend (Java)**

#### A. Repositorio Actualizado: `AlineacionRepository.java`

**Métodos nuevos:**

```java
// Buscar alineación específica por usuario, partido y equipo
Optional<Alineacion> findByCreatedByAndPartidoAndEquipo(
    User user, 
    Partido partido, 
    Equipo equipo
);

// Verificar si existe una alineación para usuario, partido y equipo
boolean existsByCreatedByAndPartidoAndEquipo(
    User user, 
    Partido partido, 
    Equipo equipo
);
```

**Propósito:** Consultar la base de datos para verificar si ya existe una alineación antes de crear una nueva.

---

#### B. Modelo Actualizado: `Alineacion.java`

**Índice único modificado:**

```java
// ANTES: Solo partido y equipo
@UniqueConstraint(columnNames = {"partido_id", "equipo_id"})

// AHORA: Usuario, partido y equipo
@UniqueConstraint(
    name = "uk_user_partido_equipo",
    columnNames = {"created_by", "partido_id", "equipo_id"}
)
```

**Ventaja:** Garantiza a nivel de base de datos que no se puedan crear duplicados, incluso si hay error en la lógica del código.

**SQL generado:**
```sql
ALTER TABLE alineaciones
ADD CONSTRAINT uk_user_partido_equipo 
UNIQUE (created_by, partido_id, equipo_id);
```

---

#### C. Controlador Actualizado: `AlineacionController.java`

**Cambio 1: Endpoint POST `/api/alineaciones` mejorado**

```java
@PostMapping
public ResponseEntity<?> crearAlineacion(...) {
    // ... validaciones ...
    
    // ✅ NUEVA VALIDACIÓN
    boolean existeAlineacion = alineacionRepository
        .existsByCreatedByAndPartidoAndEquipo(user, partido, equipo);
    
    if (existeAlineacion) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", "Ya tienes una alineación creada para este equipo en este partido");
        error.put("message", "Solo puedes crear una alineación por equipo por partido...");
        error.put("partidoNombre", ...);
        error.put("equipoNombre", ...);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    
    // ... crear alineación ...
}
```

**Códigos de respuesta:**
- `201 CREATED` - Alineación creada exitosamente
- `409 CONFLICT` - Ya existe una alineación (con detalles)
- `400 BAD REQUEST` - Otros errores

---

**Cambio 2: Nuevo endpoint GET `/api/alineaciones/verificar-existente`**

```java
@GetMapping("/verificar-existente")
public ResponseEntity<?> verificarAlineacionExistente(
    @RequestParam Integer partidoId,
    @RequestParam Integer equipoId,
    Authentication authentication
) {
    // ... validaciones ...
    
    Optional<Alineacion> alineacionExistente = alineacionRepository
        .findByCreatedByAndPartidoAndEquipo(user, partido, equipo);
    
    Map<String, Object> response = new HashMap<>();
    response.put("existe", alineacionExistente.isPresent());
    
    if (alineacionExistente.isPresent()) {
        response.put("alineacion", convertToDTO(alineacionExistente.get()));
        response.put("message", "Ya tienes una alineación...");
    }
    
    return ResponseEntity.ok(response);
}
```

**Propósito:** Permitir al frontend verificar ANTES de que el usuario complete toda la alineación.

**Respuesta cuando existe:**
```json
{
  "existe": true,
  "alineacion": {
    "id": 5,
    "partidoNombre": "Betis vs Barcelona",
    "equipoNombre": "Betis",
    "formacion": "1-4-3-3",
    ...
  },
  "message": "Ya tienes una alineación creada para este equipo en este partido"
}
```

**Respuesta cuando NO existe:**
```json
{
  "existe": false
}
```

---

### 2. **Frontend (HTML/JavaScript)**

#### A. Actualizado: `crear-alineacion.html`

**Cambio 1: Función `seleccionarEquipo()` - Verificación temprana**

```javascript
async function seleccionarEquipo(tipo) {
    equipoSeleccionado = { ... };
    
    // ✅ NUEVA VERIFICACIÓN
    const response = await fetch(
        `${API_URL}/alineaciones/verificar-existente?` +
        `partidoId=${partidoSeleccionado.id}&` +
        `equipoId=${equipoSeleccionado.id}`,
        { headers: { 'Authorization': `Bearer ${token}` } }
    );
    
    const data = await response.json();
    
    if (data.existe) {
        const confirmar = confirm(
            `⚠️ ATENCIÓN\n\n` +
            `Ya tienes una alineación creada para ${equipoNombre}...\n\n` +
            `¿Deseas ir a "Mis Alineaciones" para eliminarla?`
        );
        
        if (confirmar) {
            window.location.href = 'mis-alineaciones.html';
            return;
        } else {
            // Deseleccionar equipo
            equipoSeleccionado = null;
            return;
        }
    }
    
    // Continuar si NO existe
    document.getElementById('btnPaso3').disabled = false;
}
```

**Flujo:**
1. Usuario selecciona equipo en Paso 2
2. Sistema verifica si ya existe alineación
3. Si existe → Muestra alerta con opciones
4. Si NO existe → Permite continuar

---

**Cambio 2: Función `guardarAlineacion()` - Manejo de conflicto**

```javascript
async function guardarAlineacion() {
    // ... validaciones ...
    
    const response = await fetch(`${API_URL}/alineaciones`, {
        method: 'POST',
        body: JSON.stringify(alineacionData)
    });
    
    if (!response.ok) {
        const error = await response.json();
        
        // ✅ MANEJO ESPECIAL PARA CONFLICTO (409)
        if (response.status === 409) {
            const confirmar = confirm(
                `⚠️ YA TIENES UNA ALINEACIÓN CREADA\n\n` +
                `${error.message}\n\n` +
                `Partido: ${error.partidoNombre}\n` +
                `Equipo: ${error.equipoNombre}\n\n` +
                `¿Ir a "Mis Alineaciones"?`
            );
            
            if (confirmar) {
                window.location.href = 'mis-alineaciones.html';
            }
            return;
        }
        
        throw new Error(error.error);
    }
    
    // ... guardar exitoso ...
}
```

**Ventaja:** Doble validación (en selección de equipo y al guardar) para máxima seguridad.

---

## 🔒 Niveles de Validación

El sistema tiene **3 niveles de validación** para garantizar la restricción:

### Nivel 1: Frontend - Verificación al Seleccionar Equipo
```javascript
// En seleccionarEquipo()
GET /api/alineaciones/verificar-existente
→ Si existe: Alerta y opción de ir a "Mis Alineaciones"
→ Si NO existe: Permite continuar
```

**Ventaja:** El usuario sabe ANTES de completar toda la alineación.

---

### Nivel 2: Backend - Validación al Crear
```java
// En crearAlineacion()
boolean existeAlineacion = repository.existsByCreatedByAndPartidoAndEquipo(...);
if (existeAlineacion) {
    return ResponseEntity.status(409).body(error);
}
```

**Ventaja:** Evita procesamiento innecesario si ya existe.

---

### Nivel 3: Base de Datos - Constraint Único
```sql
CONSTRAINT uk_user_partido_equipo 
UNIQUE (created_by, partido_id, equipo_id)
```

**Ventaja:** Garantía absoluta, incluso si hay bug en el código.

---

## 📊 Flujos de Usuario

### Escenario 1: Primera Vez (No Existe Alineación)

```
1. Usuario va a "Crear Alineación"
2. Paso 1: Selecciona partido "Betis vs Barcelona"
3. Paso 2: Selecciona equipo "Betis"
   ↓
4. Sistema verifica: GET /verificar-existente
5. Respuesta: { "existe": false }
   ↓
6. ✅ Permite continuar al Paso 3
7. Usuario completa formación y jugadores
8. Click "Guardar"
   ↓
9. Backend valida: NO existe duplicado
10. ✅ Crea alineación en BD
11. Retorna 201 CREATED
    ↓
12. ✅ "Alineación guardada exitosamente"
13. Redirige a "Mis Alineaciones"
```

---

### Escenario 2: Ya Existe Alineación (Detectado en Paso 2)

```
1. Usuario va a "Crear Alineación"
2. Paso 1: Selecciona partido "Betis vs Barcelona"
3. Paso 2: Intenta seleccionar equipo "Betis"
   ↓
4. Sistema verifica: GET /verificar-existente
5. Respuesta: { "existe": true, "alineacion": {...} }
   ↓
6. ⚠️ Muestra alerta:
   "Ya tienes una alineación creada para Betis en este partido.
    Solo puedes tener UNA alineación por equipo por partido.
    
    Opciones:
    - Cancelar para elegir otro equipo
    - Aceptar para ir a "Mis Alineaciones" y eliminar la anterior
    
    ¿Ir a "Mis Alineaciones"?"
   ↓
7a. Si usuario acepta:
    → Redirige a "Mis Alineaciones"
    → Usuario puede ver/eliminar la alineación existente
    
7b. Si usuario cancela:
    → Deselecciona el equipo "Betis"
    → Puede seleccionar "Barcelona" (el otro equipo)
```

---

### Escenario 3: Ya Existe pero Bypasea Paso 2 (Detectado al Guardar)

```
(Caso extremo: si hay error en verificación del Paso 2)

1-7. Usuario completa todos los pasos
8. Click "Guardar"
   ↓
9. Backend valida: existsByCreatedByAndPartidoAndEquipo
10. Encuentra duplicado
11. Retorna 409 CONFLICT con detalles
    ↓
12. Frontend detecta status 409
13. ⚠️ Muestra alerta:
    "YA TIENES UNA ALINEACIÓN CREADA
     
     [mensaje del servidor]
     Partido: Betis vs Barcelona
     Equipo: Betis
     
     ¿Ir a "Mis Alineaciones"?"
    ↓
14a. Si acepta → Redirige a "Mis Alineaciones"
14b. Si cancela → Permanece en la página
```

---

## 🎨 Mensajes al Usuario

### Mensaje en Paso 2 (Selección de Equipo):
```
⚠️ ATENCIÓN

Ya tienes una alineación creada para Betis en este partido.

Solo puedes tener UNA alineación por equipo por partido.

Opciones:
- Haz clic en "Cancelar" para volver y elegir otro equipo
- Haz clic en "Aceptar" para ir a "Mis Alineaciones" y eliminar la anterior

¿Deseas ir a "Mis Alineaciones" ahora?

[Cancelar]  [Aceptar]
```

---

### Mensaje al Guardar (Si detecta duplicado):
```
⚠️ YA TIENES UNA ALINEACIÓN CREADA

Ya tienes una alineación creada para este equipo en este partido

Partido: Betis vs Barcelona
Equipo: Betis

Solo puedes tener UNA alineación por equipo por partido.

¿Deseas ir a "Mis Alineaciones" para ver o eliminar la alineación existente?

[Cancelar]  [Aceptar]
```

---

## 💾 Base de Datos

### Constraint Único Agregado:

```sql
-- Antes de ejecutar el servidor, ejecutar en MySQL:
ALTER TABLE alineaciones
DROP INDEX IF EXISTS partido_id;  -- Eliminar constraint viejo si existe

ALTER TABLE alineaciones
ADD CONSTRAINT uk_user_partido_equipo 
UNIQUE (created_by, partido_id, equipo_id);
```

### Estructura Completa:

```sql
CREATE TABLE alineaciones (
    id INT AUTO_INCREMENT PRIMARY KEY,
    partido_id INT NOT NULL,
    equipo_id INT NOT NULL,
    created_by INT,
    alineacion JSON NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (partido_id) REFERENCES partidos(id),
    FOREIGN KEY (equipo_id) REFERENCES equipos(id),
    FOREIGN KEY (created_by) REFERENCES users(id),
    
    -- ✅ CONSTRAINT ÚNICO: Un usuario, un equipo, un partido
    CONSTRAINT uk_user_partido_equipo 
        UNIQUE (created_by, partido_id, equipo_id)
);
```

---

## 🧪 Casos de Prueba

### Caso 1: Crear Primera Alineación
```
Usuario: user1@test.com (ID: 1)
Partido: Betis vs Barcelona (ID: 1)
Equipo: Betis (ID: 10)

Resultado: ✅ Creada exitosamente
```

### Caso 2: Crear Segunda Alineación (Mismo Partido, Otro Equipo)
```
Usuario: user1@test.com (ID: 1)
Partido: Betis vs Barcelona (ID: 1)
Equipo: Barcelona (ID: 11)

Resultado: ✅ Creada exitosamente (diferente equipo)
```

### Caso 3: Intentar Duplicar (Mismo Usuario, Partido, Equipo)
```
Usuario: user1@test.com (ID: 1)
Partido: Betis vs Barcelona (ID: 1)
Equipo: Betis (ID: 10)

Resultado: ❌ 409 CONFLICT
Mensaje: "Ya tienes una alineación creada para este equipo en este partido"
```

### Caso 4: Otro Usuario Puede Crear Para Mismo Partido/Equipo
```
Usuario: user2@test.com (ID: 2)
Partido: Betis vs Barcelona (ID: 1)
Equipo: Betis (ID: 10)

Resultado: ✅ Creada exitosamente (diferente usuario)
```

### Caso 5: Mismo Usuario, Mismo Equipo, Otro Partido
```
Usuario: user1@test.com (ID: 1)
Partido: Barcelona vs Real Madrid (ID: 2)
Equipo: Barcelona (ID: 11)

Resultado: ✅ Creada exitosamente (diferente partido)
```

---

## 🔄 Workflow Completo

```
┌─────────────────────────────────────────────────┐
│  Usuario selecciona Partido y Equipo           │
└──────────────────┬──────────────────────────────┘
                   │
                   ▼
         ┌─────────────────────┐
         │ Verificar Existente │
         │ (GET /verificar)    │
         └──────┬──────┬───────┘
                │      │
         NO     │      │     SÍ
         existe │      │     existe
                ▼      ▼
         ┌──────┐  ┌──────────────────┐
         │ OK   │  │ Alerta + Opción  │
         │      │  │ ir a "Mis Aline" │
         └──┬───┘  └────────┬─────────┘
            │               │
            │            Acepta
            │               │
            │               ▼
            │      ┌───────────────────┐
            │      │ Mis Alineaciones  │
            │      │ (ver/eliminar)    │
            │      └───────────────────┘
            │
            ▼
   ┌────────────────────┐
   │ Usuario completa   │
   │ formación y        │
   │ jugadores          │
   └──────────┬─────────┘
              │
              ▼
   ┌────────────────────┐
   │ Click "Guardar"    │
   │ (POST /alineaciones)│
   └──────┬───────┬─────┘
          │       │
    SIN   │       │   CON
    error │       │   409
          ▼       ▼
   ┌──────┐  ┌──────────┐
   │ 201  │  │ Alerta   │
   │ OK   │  │ Conflict │
   └──┬───┘  └────┬─────┘
      │           │
      ▼           ▼
   "Guardado"  "Ya existe"
```

---

## ✅ Estado Final

```
╔════════════════════════════════════════════════╗
║                                                ║
║  ✅ RESTRICCIÓN IMPLEMENTADA                  ║
║                                                ║
║  Regla:                                        ║
║  1 Usuario + 1 Partido + 1 Equipo             ║
║  = MÁXIMO 1 Alineación                        ║
║                                                ║
║  Validaciones:                                 ║
║  • Frontend (Paso 2)         ✅               ║
║  • Backend (POST)            ✅               ║
║  • Base de Datos (UNIQUE)    ✅               ║
║                                                ║
║  Endpoints:                                    ║
║  • GET /verificar-existente  ✅               ║
║  • POST /alineaciones (409)  ✅               ║
║                                                ║
║  UX:                                           ║
║  • Alerta temprana (Paso 2)  ✅               ║
║  • Mensajes claros           ✅               ║
║  • Opción ir a "Mis Aline"   ✅               ║
║                                                ║
║  Compilación:  BUILD SUCCESS ✅               ║
║  Estado:       FUNCIONANDO 🚀                 ║
║                                                ║
╚════════════════════════════════════════════════╝
```

---

## 📝 Resumen

**Restricción implementada:**
- ✅ Un usuario puede crear **máximo 1 alineación** por equipo por partido
- ✅ Puede crear alineaciones para **ambos equipos** del mismo partido
- ✅ Puede crear **múltiples alineaciones** para el mismo equipo en **diferentes partidos**

**Ejemplo práctico:**
```
Partido 1: Betis vs Barcelona
  User1 → Betis: ✅ 1 alineación
  User1 → Barcelona: ✅ 1 alineación
  User1 → Betis: ❌ NO (ya existe)

Partido 2: Barcelona vs Real Madrid
  User1 → Barcelona: ✅ 1 alineación (nuevo partido)
  User1 → Real Madrid: ✅ 1 alineación
```

**¡La restricción está completamente implementada con validación en frontend, backend y base de datos! 🎉**
