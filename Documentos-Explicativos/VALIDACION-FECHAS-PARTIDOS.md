# 🕐 VALIDACIÓN DE FECHAS DE PARTIDOS

## 📅 Fecha de Implementación: 6 de Febrero de 2026

---

## 🎯 NUEVA FUNCIONALIDAD

### Restricción de Alineaciones por Fecha

Ahora **NO se pueden crear alineaciones para partidos que ya se han jugado**.

---

## ✅ ¿QUÉ SE HA IMPLEMENTADO?

### 1. **Validación en el Backend** 🔐

**Ubicación:** `AlineacionController.java` - método `crearAlineacion()`

**Lógica:**
```java
LocalDateTime ahora = LocalDateTime.now();
if (partido.getFecha().isBefore(ahora)) {
    // Error 403 - Partido ya jugado
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
}
```

**Respuesta de error:**
```json
{
  "error": "El partido ya se ha jugado",
  "message": "No puedes crear alineaciones para partidos que ya se han jugado",
  "fechaPartido": "2026-02-05T20:00:00",
  "fechaActual": "2026-02-06T16:30:00",
  "partidoNombre": "Barcelona vs Real Madrid"
}
```

---

### 2. **Filtrado en el Frontend** 🔍

**Ubicación:** `crear-alineacion.html` - función `cargarPartidos()`

**Lógica:**
```javascript
// Filtrar solo partidos futuros
const ahora = new Date();
partidos = todosPartidos.filter(partido => {
    const fechaPartido = new Date(partido.fecha);
    return fechaPartido > ahora;
});
```

**Resultado:**
- Solo se muestran en el dropdown los partidos que **aún no se han jugado**
- Los partidos pasados no aparecen en la lista

---

### 3. **Manejo de Errores Mejorado** ⚠️

**Ubicación:** `crear-alineacion.html` - función `guardarAlineacion()`

**Si intentas crear una alineación para un partido ya jugado:**

```
⛔ PARTIDO YA JUGADO

No puedes crear alineaciones para partidos que ya se han jugado

Partido: Barcelona vs Real Madrid
Fecha del partido: 5 de febrero de 2026, 20:00:00

Solo puedes crear alineaciones para partidos futuros.
```

Luego te redirige automáticamente al Paso 1 para que selecciones otro partido.

---

## 📊 FLUJO COMPLETO

### Escenario 1: Crear Alineación (Partido Futuro) ✅

```
Usuario → Crear Alineación
    ↓
Selecciona Partido Futuro (fecha > hoy)
    ↓
Selecciona Equipo
    ↓
Configura Formación
    ↓
Selecciona Jugadores
    ↓
Guardar → ✅ ÉXITO
```

---

### Escenario 2: Intentar Crear Alineación (Partido Pasado) ❌

#### **Opción A: Filtrado en Frontend**
```
Usuario → Crear Alineación
    ↓
Dropdown solo muestra partidos futuros
    ↓
Partidos pasados NO aparecen
    ↓
Usuario solo puede seleccionar partidos válidos
```

#### **Opción B: Validación en Backend** (por si alguien manipula el frontend)
```
Usuario manipula frontend y envía ID de partido pasado
    ↓
Backend valida fecha
    ↓
partido.getFecha() < ahora
    ↓
❌ Error 403 Forbidden
    ↓
Frontend muestra alerta "PARTIDO YA JUGADO"
    ↓
Redirige al Paso 1
```

---

## 🔒 SEGURIDAD

### Doble Capa de Validación:

1. **Frontend (Primera Línea)** 🛡️
   - Filtra partidos antes de mostrarlos
   - Mejor UX (usuario no ve partidos inválidos)
   - Previene errores accidentales

2. **Backend (Última Línea)** 🔐
   - Validación definitiva
   - No se puede bypass manipulando el frontend
   - Seguridad robusta

---

## 📝 MENSAJES AL USUARIO

### Cuando no hay partidos futuros:

```
ℹ️ No hay partidos próximos. 
Los partidos ya jugados no permiten crear alineaciones.
```

### Cuando se cargan partidos exitosamente:

```
✅ 5 partidos próximos cargados
```

### Cuando intenta crear alineación de partido pasado:

```
⛔ PARTIDO YA JUGADO

No puedes crear alineaciones para partidos que ya se han jugado

Partido: Barcelona vs Real Madrid
Fecha del partido: 5 de febrero de 2026, 20:00:00

Solo puedes crear alineaciones para partidos futuros.
```

---

## 🎮 EJEMPLOS DE USO

### Ejemplo 1: Hoy es 6 de Febrero, 16:00

**Partidos en la base de datos:**
- ✅ Barcelona vs Real Madrid - 10 Feb, 20:00 → **SE MUESTRA**
- ✅ Sevilla vs Atlético - 8 Feb, 18:00 → **SE MUESTRA**
- ❌ Betis vs Valencia - 5 Feb, 21:00 → **NO SE MUESTRA** (ya jugado)
- ❌ Villarreal vs Athletic - 3 Feb, 19:00 → **NO SE MUESTRA** (ya jugado)

**Dropdown mostrará:**
```
-- Selecciona un partido --
Barcelona vs Real Madrid - 10 de febrero de 2026, 20:00:00
Sevilla vs Atlético - 8 de febrero de 2026, 18:00:00
```

---

### Ejemplo 2: Partido de hoy que aún no empieza

**Hoy:** 6 de Febrero, 16:00
**Partido:** Getafe vs Mallorca - 6 Feb, 21:00

**Resultado:** ✅ **SE MUESTRA** (es futuro, aún no ha empezado)

---

### Ejemplo 3: Partido de hoy que ya empezó

**Hoy:** 6 de Febrero, 21:30
**Partido:** Getafe vs Mallorca - 6 Feb, 21:00

**Resultado:** ❌ **NO SE MUESTRA** (ya empezó hace 30 minutos)

---

## 🔧 CAMBIOS TÉCNICOS

### Backend - AlineacionController.java

**Import añadido:**
```java
import java.time.LocalDateTime;
```

**Validación añadida:**
```java
// Verificar que el partido no se haya jugado todavía
LocalDateTime ahora = LocalDateTime.now();
if (partido.getFecha().isBefore(ahora)) {
    Map<String, Object> error = new HashMap<>();
    error.put("error", "El partido ya se ha jugado");
    error.put("message", "No puedes crear alineaciones para partidos que ya se han jugado");
    error.put("fechaPartido", partido.getFecha());
    error.put("fechaActual", ahora);
    error.put("partidoNombre", partido.getEquipoLocal().getNombre() + " vs " + partido.getEquipoVisitante().getNombre());
    
    log.warn("Intento de crear alineación para partido ya jugado. Partido ID: {}, Fecha: {}, Usuario: {}",
            partido.getId(), partido.getFecha(), user.getEmail());
    
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
}
```

---

### Frontend - crear-alineacion.html

**Filtrado de partidos:**
```javascript
// Filtrar solo partidos futuros
const ahora = new Date();
partidos = todosPartidos.filter(partido => {
    const fechaPartido = new Date(partido.fecha);
    return fechaPartido > ahora;
});

console.log(`📅 Total: ${todosPartidos.length}, Futuros: ${partidos.length}`);
```

**Manejo de error 403:**
```javascript
if (response.status === 403) {
    alert(
        `⛔ PARTIDO YA JUGADO\n\n` +
        `${error.message}\n\n` +
        `Partido: ${error.partidoNombre}\n` +
        `Fecha: ${new Date(error.fechaPartido).toLocaleString('es-ES')}\n\n` +
        `Solo puedes crear alineaciones para partidos futuros.`
    );
    
    // Volver al paso 1
    setTimeout(() => {
        cambiarPaso(1);
        showAlert('⚠️ Selecciona un partido que aún no se haya jugado', 'warning');
    }, 500);
    return;
}
```

---

## ✨ BENEFICIOS

### Para los Usuarios:
- ✅ No pueden cometer el error de crear alineaciones para partidos pasados
- ✅ Mensajes claros cuando algo no está permitido
- ✅ Solo ven partidos válidos en el dropdown
- ✅ Mejor experiencia de usuario

### Para el Sistema:
- ✅ Datos más coherentes en la base de datos
- ✅ Validación robusta (frontend + backend)
- ✅ Logs de intentos de crear alineaciones inválidas
- ✅ Prevención de errores

---

## 🐛 CASOS ESPECIALES

### Caso 1: Usuario tiene página abierta durante horas

**Situación:**
- Usuario abre "Crear Alineación" a las 18:00
- Partido es a las 20:00 (aparece en lista)
- Usuario crea alineación a las 21:00 (partido ya empezó)

**Solución:**
- ✅ Backend valida la fecha AL MOMENTO DE GUARDAR
- ❌ Rechaza la alineación con error 403
- ℹ️ Muestra mensaje "PARTIDO YA JUGADO"

---

### Caso 2: Diferencia de zona horaria

**Situación:**
- Servidor en UTC
- Cliente en CET (UTC+1)

**Solución:**
- ✅ Backend usa `LocalDateTime.now()` (hora del servidor)
- ✅ Frontend usa `new Date()` (hora local del cliente)
- ✅ Fechas se comparan correctamente en cada lado

---

### Caso 3: Partido cancelado o reprogramado

**Situación:**
- Admin cambia la fecha del partido

**Resultado:**
- ✅ Alineaciones existentes se mantienen
- ✅ Si nueva fecha es futura, vuelve a aparecer en dropdown
- ✅ Validación se hace siempre al momento de crear

---

## 📊 LOGS DEL SISTEMA

Cuando alguien intenta crear alineación de partido pasado:

```
WARN 12345 --- [AlineacionController] : 
Intento de crear alineación para partido ya jugado. 
Partido ID: 1, 
Fecha: 2026-02-05T20:00:00, 
Usuario: usuario@email.com
```

Esto ayuda a:
- 🔍 Detectar intentos de manipulación
- 📊 Analizar comportamiento de usuarios
- 🐛 Debugging de problemas

---

## 🎯 CONCLUSIÓN

La validación de fechas asegura que:

1. ✅ Solo se crean alineaciones para partidos futuros
2. ✅ Mejor experiencia de usuario (no ven opciones inválidas)
3. ✅ Sistema más robusto (validación en 2 capas)
4. ✅ Datos coherentes en la base de datos
5. ✅ Mensajes claros cuando algo falla

---

**¡Tu sistema ahora es más inteligente y previene errores automáticamente! 🚀**
