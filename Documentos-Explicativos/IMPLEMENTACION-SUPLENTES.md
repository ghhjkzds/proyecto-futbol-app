# 🔄 IMPLEMENTACIÓN DE SUPLENTES/RESERVAS EN CREAR ALINEACIÓN

## 📅 Fecha: 10 Febrero 2026

---

## 🎯 OBJETIVO

Añadir un apartado para seleccionar **5 jugadores suplentes/reservas** en la página de crear alineación, sin restricción de posición y de manera opcional.

---

## ✅ CAMBIOS IMPLEMENTADOS

### 1. **Estilos CSS para Sección de Suplentes**

#### Nuevos estilos añadidos:

```css
.suplentes-section {
    margin-top: 40px;
    padding: 25px;
    background: linear-gradient(135deg, rgba(118, 75, 162, 0.05) 0%, rgba(102, 126, 234, 0.05) 100%);
    border-radius: 15px;
    border: 2px dashed #764ba2;
}

.suplentes-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
    gap: 15px;
}

.suplente-wrapper {
    position: relative;
}

.suplente-wrapper label {
    font-size: 13px;
    color: #764ba2;
    margin-bottom: 5px;
    font-weight: 600;
}
```

**Características visuales:**
- 🎨 Fondo degradado sutil con bordes discontinuos (dashed)
- 🟣 Color morado (#764ba2) para distinguir de titulares
- 📱 Grid responsive con diseño adaptativo

---

### 2. **Modificación en `mostrarSelectoresJugadores()`**

#### Nueva sección añadida al final:

```javascript
// Crear sección de suplentes
const suplientesSection = document.createElement('div');
suplientesSection.className = 'suplentes-section';

// Título
const suplientesTitle = document.createElement('h3');
suplientesTitle.innerHTML = '🔄 Suplentes / Reservas';

// Información
const suplientesInfo = document.createElement('p');
suplientesInfo.innerHTML = 'Selecciona hasta 5 jugadores suplentes (cualquier posición). <strong>Estos campos son opcionales.</strong>';

// Grid de 5 selectores
for (let i = 1; i <= 5; i++) {
    // Crear select con TODOS los jugadores (sin filtro de posición)
    // Añade clase 'suplente-select' para identificarlos
}
```

**Características:**
- 🔄 **5 selectores** para suplentes
- 🌐 **Sin restricción de posición** - Muestra todos los jugadores disponibles
- ✅ **Opcional** - No es obligatorio llenar todos los campos
- 🏷️ **Identificador visual** - Muestra nombre, número y posición del jugador

---

### 3. **Modificación en `guardarAlineacion()`**

#### Lógica actualizada:

**Antes:**
```javascript
// Recogía TODOS los .player-select (titulares + suplentes mezclados)
const selects = document.querySelectorAll('.player-select');
// ...
suplentes: [] // Siempre vacío
```

**Ahora:**
```javascript
// PASO 1: Recoger solo titulares
const selects = document.querySelectorAll('.player-select:not(.suplente-select)');
// Validar 11 titulares obligatorios

// PASO 2: Recoger suplentes (opcionales)
const suplenteSelects = document.querySelectorAll('.suplente-select');
const suplentesSeleccionados = [];

for (const select of suplenteSelects) {
    if (select.value) { // Solo si hay selección
        // Validar que no sea titular
        // Validar que no haya duplicados
        suplentesSeleccionados.push(playerData);
    }
}

// PASO 3: Crear estructura con ambos
const alineacionDetalles = {
    formacion: formacion,
    titulares: [...],
    suplentes: [...] // Ahora se incluyen los suplentes
};
```

---

## 🔍 VALIDACIONES IMPLEMENTADAS

### 1. **Titulares (Obligatorios)**
- ✅ Deben ser exactamente 11 jugadores
- ✅ No pueden repetirse
- ✅ Todos deben estar seleccionados

### 2. **Suplentes (Opcionales)**
- ✅ Pueden ser de 0 a 5 jugadores
- ✅ No pueden ser titulares al mismo tiempo
- ✅ No pueden repetirse entre sí
- ✅ Pueden dejarse vacíos sin problema

### 3. **Mensajes de Error**

| Error | Mensaje |
|-------|---------|
| Falta titular | ❌ Debes seleccionar todos los jugadores titulares |
| Titular duplicado | ❌ No puedes seleccionar el mismo jugador dos veces |
| No hay 11 titulares | ❌ Debes seleccionar exactamente 11 jugadores titulares (tienes X) |
| Suplente es titular | ❌ Un suplente no puede ser también titular |
| Suplente duplicado | ❌ No puedes seleccionar el mismo suplente dos veces |

---

## 📊 ESTRUCTURA DE DATOS GUARDADA

### Estructura JSON de Alineación:

```json
{
  "partidoId": 1,
  "equipoId": 2,
  "alineacion": {
    "formacion": "1-4-3-3",
    "titulares": [
      {
        "idJugador": 123,
        "nombre": "Jugador 1",
        "numero": 1,
        "posicion": "Goalkeeper"
      },
      // ... 10 jugadores más (11 total)
    ],
    "suplentes": [
      {
        "idJugador": 456,
        "nombre": "Suplente 1",
        "numero": 12,
        "posicion": "Defender"
      },
      {
        "idJugador": 789,
        "nombre": "Suplente 2",
        "numero": 13,
        "posicion": "Midfielder"
      }
      // ... hasta 5 suplentes (0-5, opcional)
    ]
  }
}
```

---

## 🎨 INTERFAZ DE USUARIO

### Vista del Paso 4 (Selección de Jugadores):

```
╔════════════════════════════════════════════════════════╗
║  🧤 Porteros                                          ║
║  [Dropdown: Portero 1] ▼                              ║
╠════════════════════════════════════════════════════════╣
║  🛡️ Defensas                                          ║
║  [Dropdown: Defensa 1] ▼  [Dropdown: Defensa 2] ▼    ║
║  [Dropdown: Defensa 3] ▼  [Dropdown: Defensa 4] ▼    ║
╠════════════════════════════════════════════════════════╣
║  ⚙️ Centrocampistas                                   ║
║  [Dropdown: Centrocampista 1] ▼  [...] ▼  [...] ▼    ║
╠════════════════════════════════════════════════════════╣
║  ⚡ Delanteros                                         ║
║  [Dropdown: Delantero 1] ▼  [...] ▼  [...] ▼         ║
╠════════════════════════════════════════════════════════╣
║  ╔══════════════════════════════════════════════╗    ║
║  ║  🔄 Suplentes / Reservas                     ║    ║
║  ║  Selecciona hasta 5 jugadores suplentes      ║    ║
║  ║  (cualquier posición). Estos campos son      ║    ║
║  ║  opcionales.                                 ║    ║
║  ║                                              ║    ║
║  ║  [Dropdown: Suplente 1 - Opcional] ▼        ║    ║
║  ║  [Dropdown: Suplente 2 - Opcional] ▼        ║    ║
║  ║  [Dropdown: Suplente 3 - Opcional] ▼        ║    ║
║  ║  [Dropdown: Suplente 4 - Opcional] ▼        ║    ║
║  ║  [Dropdown: Suplente 5 - Opcional] ▼        ║    ║
║  ╚══════════════════════════════════════════════╝    ║
╠════════════════════════════════════════════════════════╣
║  [← Anterior]  [💾 Guardar Alineación]               ║
╚════════════════════════════════════════════════════════╝
```

---

## 🔄 FLUJO DE USUARIO

### Escenario 1: Usuario completa todo

```
1. Usuario selecciona 11 titulares ✅
2. Usuario selecciona 5 suplentes ✅
3. Hace clic en "Guardar Alineación"
4. Sistema valida todo
5. Mensaje: "💾 Guardando alineación (11 titulares + 5 suplentes)..."
6. ✅ Alineación guardada correctamente
```

### Escenario 2: Usuario solo completa titulares

```
1. Usuario selecciona 11 titulares ✅
2. Usuario NO selecciona suplentes (los deja vacíos) ✅
3. Hace clic en "Guardar Alineación"
4. Sistema valida titulares
5. Mensaje: "💾 Guardando alineación (11 titulares + 0 suplentes)..."
6. ✅ Alineación guardada correctamente con suplentes: []
```

### Escenario 3: Usuario selecciona algunos suplentes

```
1. Usuario selecciona 11 titulares ✅
2. Usuario selecciona 2 suplentes, deja 3 vacíos ✅
3. Hace clic en "Guardar Alineación"
4. Sistema valida todo
5. Mensaje: "💾 Guardando alineación (11 titulares + 2 suplentes)..."
6. ✅ Alineación guardada correctamente
```

### Escenario 4: Usuario intenta duplicar jugador

```
1. Usuario selecciona jugador X como titular
2. Usuario selecciona jugador X como suplente
3. Hace clic en "Guardar Alineación"
4. ❌ Error: "Un suplente no puede ser también titular"
5. Campo se resalta en rojo
6. Usuario debe corregir
```

---

## 🎯 DIFERENCIAS CLAVE: TITULARES vs SUPLENTES

| Aspecto | Titulares | Suplentes |
|---------|-----------|-----------|
| **Cantidad** | Exactamente 11 | De 0 a 5 (opcional) |
| **Restricción de posición** | Sí (Portero, Defensa, etc.) | No (cualquier posición) |
| **Obligatoriedad** | Obligatorio completar todos | Opcional |
| **Clase CSS** | `.player-select` | `.suplente-select` |
| **Selector querySelector** | `.player-select:not(.suplente-select)` | `.suplente-select` |
| **Validación** | Error si falta alguno | Sin error si están vacíos |
| **Color en UI** | Azul (#667eea) | Morado (#764ba2) |
| **Icono** | Según posición (🧤🛡️⚙️⚡) | 🔄 |

---

## 🧪 CASOS DE PRUEBA

### ✅ Prueba 1: Guardar solo titulares
- Seleccionar 11 titulares
- Dejar suplentes vacíos
- Resultado esperado: ✅ Guardado exitoso

### ✅ Prueba 2: Guardar titulares + 1 suplente
- Seleccionar 11 titulares
- Seleccionar 1 suplente en primer campo
- Resultado esperado: ✅ Guardado exitoso con 1 suplente

### ✅ Prueba 3: Guardar titulares + 5 suplentes
- Seleccionar 11 titulares
- Seleccionar 5 suplentes
- Resultado esperado: ✅ Guardado exitoso con 5 suplentes

### ❌ Prueba 4: Intentar duplicar jugador titular-suplente
- Seleccionar jugador X como portero
- Seleccionar jugador X como suplente
- Resultado esperado: ❌ Error de validación

### ❌ Prueba 5: Intentar duplicar suplentes
- Seleccionar jugador X como suplente 1
- Seleccionar jugador X como suplente 2
- Resultado esperado: ❌ Error de validación

### ✅ Prueba 6: Suplentes no consecutivos
- Seleccionar suplente 1: Jugador A
- Dejar suplente 2 vacío
- Seleccionar suplente 3: Jugador B
- Dejar suplentes 4 y 5 vacíos
- Resultado esperado: ✅ Guardado exitoso con 2 suplentes

---

## 📱 RESPONSIVE DESIGN

La grid de suplentes se adapta automáticamente:

```css
.suplentes-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
    gap: 15px;
}
```

**Comportamiento:**
- 📱 Móvil (< 768px): 1 columna
- 💻 Tablet (768px - 1024px): 2 columnas
- 🖥️ Desktop (> 1024px): 3-4 columnas

---

## 🔮 COMPATIBILIDAD CON BACKEND

### Endpoint usado:
```
POST /api/alineaciones
```

### Body enviado:
```json
{
  "partidoId": number,
  "equipoId": number,
  "alineacion": {
    "formacion": string,
    "titulares": Array<Jugador>,
    "suplentes": Array<Jugador>  // ← NUEVO
  }
}
```

**Nota:** El campo `suplentes` puede ser un array vacío `[]` si no se seleccionaron suplentes, y el backend debe manejarlo correctamente.

---

## 📄 ARCHIVOS MODIFICADOS

| Archivo | Líneas Modificadas | Tipo de Cambio |
|---------|-------------------|----------------|
| `crear-alineacion.html` | ~160-198 | Nuevos estilos CSS |
| `crear-alineacion.html` | ~1095-1175 | Lógica de suplentes en UI |
| `crear-alineacion.html` | ~1178-1230 | Validación en guardar |

---

## 🚀 PRÓXIMAS MEJORAS SUGERIDAS

### 1. **Drag & Drop**
- Permitir arrastrar jugadores entre titulares y suplentes
- Reordenar suplentes

### 2. **Sugerencias Inteligentes**
- Sugerir suplentes por posición (balance)
- Mostrar alerta si faltan suplentes de una posición clave

### 3. **Vista Previa Visual**
- Mostrar mini-campo con disposición de titulares
- Mostrar banquillo con suplentes

### 4. **Comparación de Estadísticas**
- Mostrar stats del jugador al pasar el mouse
- Comparar stats entre jugadores similares

### 5. **Plantillas Guardadas**
- Guardar formaciones favoritas
- Reutilizar alineaciones de partidos anteriores

---

## ✅ CHECKLIST DE IMPLEMENTACIÓN

- [x] Añadir estilos CSS para sección de suplentes
- [x] Modificar `mostrarSelectoresJugadores()` para incluir suplentes
- [x] Crear 5 selectores sin filtro de posición
- [x] Modificar `guardarAlineacion()` para procesar suplentes
- [x] Validar que suplentes no sean titulares
- [x] Validar que suplentes no se repitan
- [x] Permitir suplentes vacíos (opcionales)
- [x] Mostrar contador en mensaje de guardado
- [x] Incluir suplentes en estructura JSON
- [x] Documentar cambios

---

## 🎉 RESUMEN EJECUTIVO

Se ha implementado exitosamente la funcionalidad de **suplentes/reservas** en la página de crear alineación:

**Características principales:**
- ✅ **5 selectores opcionales** para suplentes
- ✅ **Sin restricción de posición** - muestra todos los jugadores
- ✅ **Validación robusta** - evita duplicados y conflictos
- ✅ **Interfaz distintiva** - diseño morado con bordes dashed
- ✅ **Responsive** - se adapta a todos los dispositivos
- ✅ **Compatible** - se integra con el sistema existente

**Impacto en UX:**
- 🎯 Alineaciones más completas y realistas
- 🔄 Flexibilidad para el usuario
- ⚡ Proceso intuitivo y sin fricciones
- 📊 Mejor representación de equipos reales

---

**Estado:** ✅ COMPLETADO Y LISTO PARA USAR

**Fecha de implementación:** 10 Febrero 2026

**Tiempo estimado de desarrollo:** ~45 minutos

**Archivos modificados:** 1 (crear-alineacion.html)

---

¡Funcionalidad implementada con éxito! 🚀⚽

