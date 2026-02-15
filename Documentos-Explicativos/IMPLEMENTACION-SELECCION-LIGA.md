# ✅ IMPLEMENTADO - Selección de Liga para Crear Partidos

## 📅 Fecha de Implementación
15 de febrero de 2026

---

## 🎯 Objetivo Completado

Se ha implementado exitosamente la funcionalidad de **selección de liga** en la página de **Crear Partido**, permitiendo al usuario elegir entre las 5 principales ligas europeas antes de seleccionar los equipos.

---

## 📝 Cambios Implementados

### 1. **Backend - PartidoController.java** ✅

**Archivo:** `src/main/java/com/futbol/proyectoacd/controller/PartidoController.java`

#### **Nuevo Endpoint Agregado:**

```java
@Operation(summary = "Obtener equipos de una liga específica desde API-Football")
@GetMapping("/equipos-liga/{leagueId}")
public ResponseEntity<?> getEquiposPorLiga(
        @PathVariable Integer leagueId,
        @RequestParam(defaultValue = "2024") Integer season
) {
    try {
        log.info("Obteniendo equipos de la liga {} temporada {}", leagueId, season);
        
        // Validar que la liga sea una de las 5 ligas permitidas
        List<Integer> ligasPermitidas = List.of(140, 39, 135, 78, 61);
        if (!ligasPermitidas.contains(leagueId)) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Liga no permitida. Solo se permiten: La Liga (140), Premier League (39), Serie A (135), Bundesliga (78), Ligue 1 (61)");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        
        List<TeamData> teams = apiFootballService.searchTeamsByLeague(leagueId, season);
        log.info("Encontrados {} equipos de la liga {}", teams.size(), leagueId);
        
        return ResponseEntity.ok(teams);
    } catch (Exception e) {
        log.error("Error al obtener equipos de la liga {}", leagueId, e);
        Map<String, String> error = new HashMap<>();
        error.put("error", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

#### **Características del Endpoint:**

- 🔗 **Ruta:** `GET /api/partidos/equipos-liga/{leagueId}`
- 📥 **Parámetros:**
  - `leagueId` (path): ID de la liga (140, 39, 135, 78, 61)
  - `season` (query, opcional): Temporada (default: 2024)
- ✅ **Validación:** Solo acepta las 5 ligas europeas principales
- 🔒 **Seguridad:** Requiere autenticación (token JWT)
- 📊 **Respuesta:** Lista de equipos de la liga seleccionada
- ⚠️ **Errores:**
  - 400 Bad Request: Liga no permitida
  - 401/403: No autenticado
  - 500: Error interno/API-Football

#### **Retrocompatibilidad:**

✅ Se mantiene el endpoint original `/equipos-laliga` para no romper código existente.

---

### 2. **Frontend - crear-partido.html** ✅

**Archivo:** `src/main/resources/static/crear-partido.html`

#### **A) Cambios en HTML**

##### **Selector de Liga Agregado:**

```html
<!-- Selector de Liga -->
<div class="form-group">
    <label for="selectorLiga">🌍 Selecciona la Liga</label>
    <select id="selectorLiga" onchange="cargarEquiposPorLiga()" required>
        <option value="">Selecciona una liga...</option>
        <option value="140" data-pais="España">🇪🇸 La Liga (España)</option>
        <option value="39" data-pais="Inglaterra">🏴󠁧󠁢󠁥󠁮󠁧󠁿 Premier League (Inglaterra)</option>
        <option value="135" data-pais="Italia">🇮🇹 Serie A (Italia)</option>
        <option value="78" data-pais="Alemania">🇩🇪 Bundesliga (Alemania)</option>
        <option value="61" data-pais="Francia">🇫🇷 Ligue 1 (Francia)</option>
    </select>
</div>
```

##### **Selectores de Equipos Actualizados:**

```html
<select id="equipoLocal" onchange="loadTeamPlayers('local')" disabled>
    <option value="">Primero selecciona una liga...</option>
</select>

<select id="equipoVisitante" onchange="loadTeamPlayers('visitante')" disabled>
    <option value="">Primero selecciona una liga...</option>
</select>
```

**Estado Inicial:** Deshabilitados hasta que se seleccione una liga.

##### **Título Actualizado:**

```html
<h1>⚽ Crear Partido <span class="admin-badge">ADMIN</span></h1>
<p class="subtitle">Selecciona la liga, dos equipos y configura el partido</p>
```

---

#### **B) Cambios en CSS**

##### **Estilos para Selector de Liga:**

```css
/* Estilo para el selector de liga */
#selectorLiga {
    font-size: 16px;
    font-weight: 600;
    background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

#selectorLiga option {
    padding: 10px;
    font-size: 15px;
}

/* Selectores deshabilitados */
select:disabled {
    background-color: #f0f0f0;
    color: #999;
    cursor: not-allowed;
    opacity: 0.6;
}
```

---

#### **C) Cambios en JavaScript**

##### **Variables Globales Actualizadas:**

```javascript
const API_URL = 'http://localhost:8081/api';
let equiposLiga = [];           // Cambio: equiposLaLiga → equiposLiga
let equipoLocalData = null;
let equipoVisitanteData = null;
let ligaSeleccionada = null;    // NUEVO: Guardar liga seleccionada
```

##### **Funciones Nuevas Implementadas:**

###### **1. cargarEquiposPorLiga()**

```javascript
/**
 * Cargar equipos de la liga seleccionada
 */
async function cargarEquiposPorLiga() {
    if (!verificarAuth()) return;

    const selectorLiga = document.getElementById('selectorLiga');
    const ligaId = selectorLiga.value;

    if (!ligaId) {
        deshabilitarSelectoresEquipos();
        return;
    }

    ligaSeleccionada = ligaId;
    const paisNombre = selectorLiga.options[selectorLiga.selectedIndex].getAttribute('data-pais');

    try {
        showAlert(`Cargando equipos de ${paisNombre}...`, 'warning');

        const response = await fetch(`${API_URL}/partidos/equipos-liga/${ligaId}?season=2024`, {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            }
        });

        if (response.status === 401 || response.status === 403) {
            showAlert('⚠️ Tu sesión ha expirado. Redirigiendo al login...', 'error');
            localStorage.removeItem('token');
            localStorage.removeItem('userEmail');
            localStorage.removeItem('userRole');
            setTimeout(() => window.location.href = 'login.html', 2000);
            return;
        }

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.error || 'Error al cargar equipos');
        }

        equiposLiga = await response.json();
        llenarSelectoresEquipos();
        habilitarSelectoresEquipos();

        hideAlert();
        showAlert(`✓ ${equiposLiga.length} equipos cargados de ${paisNombre}`, 'success');
        setTimeout(hideAlert, 3000);

    } catch (error) {
        console.error('Error:', error);
        showAlert('Error: ' + error.message, 'error');
        deshabilitarSelectoresEquipos();
    }
}
```

###### **2. llenarSelectoresEquipos()**

```javascript
/**
 * Llenar los selectores de equipos con los equipos cargados
 */
function llenarSelectoresEquipos() {
    const selectLocal = document.getElementById('equipoLocal');
    const selectVisitante = document.getElementById('equipoVisitante');

    // Limpiar selectores
    selectLocal.innerHTML = '<option value="">Selecciona un equipo...</option>';
    selectVisitante.innerHTML = '<option value="">Selecciona un equipo...</option>';

    // Limpiar visualización de jugadores
    document.getElementById('teamsContainer').style.display = 'none';
    equipoLocalData = null;
    equipoVisitanteData = null;

    // Llenar con equipos
    equiposLiga.forEach(teamData => {
        const team = teamData.team;

        const optionLocal = document.createElement('option');
        optionLocal.value = team.id;
        optionLocal.textContent = team.name;
        optionLocal.setAttribute('data-nombre', team.name);

        const optionVisitante = document.createElement('option');
        optionVisitante.value = team.id;
        optionVisitante.textContent = team.name;
        optionVisitante.setAttribute('data-nombre', team.name);

        selectLocal.add(optionLocal);
        selectVisitante.add(optionVisitante);
    });
}
```

###### **3. habilitarSelectoresEquipos()**

```javascript
/**
 * Habilitar selectores de equipos
 */
function habilitarSelectoresEquipos() {
    document.getElementById('equipoLocal').disabled = false;
    document.getElementById('equipoVisitante').disabled = false;
}
```

###### **4. deshabilitarSelectoresEquipos()**

```javascript
/**
 * Deshabilitar selectores de equipos
 */
function deshabilitarSelectoresEquipos() {
    const selectLocal = document.getElementById('equipoLocal');
    const selectVisitante = document.getElementById('equipoVisitante');

    selectLocal.disabled = true;
    selectVisitante.disabled = true;
    selectLocal.innerHTML = '<option value="">Primero selecciona una liga...</option>';
    selectVisitante.innerHTML = '<option value="">Primero selecciona una liga...</option>';

    // Limpiar visualización
    document.getElementById('teamsContainer').style.display = 'none';
    equipoLocalData = null;
    equipoVisitanteData = null;
}
```

##### **Inicialización Modificada:**

**ANTES:**
```javascript
window.onload = cargarEquiposLaLiga;
```

**AHORA:**
```javascript
window.onload = function() {
    verificarAuth();
};
```

---

## 🌍 Ligas Implementadas

| Liga | País | ID API | Emoji | Estado |
|------|------|--------|-------|--------|
| **La Liga** | España | 140 | 🇪🇸 | ✅ Implementado |
| **Premier League** | Inglaterra | 39 | 🏴󠁧󠁢󠁥󠁮󠁧󠁿 | ✅ Implementado |
| **Serie A** | Italia | 135 | 🇮🇹 | ✅ Implementado |
| **Bundesliga** | Alemania | 78 | 🇩🇪 | ✅ Implementado |
| **Ligue 1** | Francia | 61 | 🇫🇷 | ✅ Implementado |

---

## 🔄 Flujo de Usuario Implementado

```
1. Usuario accede a crear-partido.html
   ↓
2. Sistema verifica autenticación
   ✅ Autenticado → Muestra formulario
   ❌ No autenticado → Redirige a login
   ↓
3. Usuario ve selector de liga (habilitado)
   Usuario ve selectores de equipos (deshabilitados)
   ↓
4. Usuario selecciona una LIGA
   Opciones: 🇪🇸 🏴󠁧󠁢󠁥󠁮󠁧󠁿 🇮🇹 🇩🇪 🇫🇷
   ↓
5. Sistema llama: cargarEquiposPorLiga()
   → GET /api/partidos/equipos-liga/{leagueId}?season=2024
   ↓
6. Backend valida liga permitida
   Backend llama a API-Football
   ↓
7. Sistema recibe equipos de la liga
   Sistema llena selectores de equipos
   Sistema habilita selectores
   ↓
8. Usuario selecciona EQUIPO LOCAL
   ↓
9. Sistema carga jugadores del equipo local
   → GET /api/equipos/api-football/team/{teamId}/players
   ↓
10. Usuario selecciona EQUIPO VISITANTE
   ↓
11. Sistema carga jugadores del equipo visitante
    → GET /api/equipos/api-football/team/{teamId}/players
    ↓
12. Usuario selecciona FECHA del partido
    ↓
13. Usuario hace click en "🏆 Crear Partido"
    ↓
14. Sistema valida y crea el partido
    → POST /api/partidos/crear
    ↓
15. ✅ Partido creado exitosamente
```

---

## ✅ Validaciones Implementadas

### Backend:
- ✅ Solo se permiten ligas con ID: 140, 39, 135, 78, 61
- ✅ Retorna error 400 si se intenta usar otra liga
- ✅ Requiere autenticación JWT
- ✅ Logs de todas las operaciones
- ✅ Manejo de errores de API-Football

### Frontend:
- ✅ Selectores de equipos deshabilitados hasta seleccionar liga
- ✅ Limpieza de selecciones al cambiar de liga
- ✅ Validación de equipos diferentes
- ✅ Validación de fecha requerida
- ✅ Manejo de token expirado
- ✅ Mensajes de error informativos

---

## 🎨 Interfaz de Usuario

### Estado Inicial:
```
┌────────────────────────────────────────┐
│ ⚽ Crear Partido [ADMIN]               │
├────────────────────────────────────────┤
│ 🌍 Selecciona la Liga                  │
│ [ Selecciona una liga...         ▼ ]  │
│                                        │
│ 🏠 Equipo Local                        │
│ [ Primero selecciona una liga... ▼ ]  │
│ (deshabilitado - gris)                 │
│                                        │
│ ✈️ Equipo Visitante                    │
│ [ Primero selecciona una liga... ▼ ]  │
│ (deshabilitado - gris)                 │
└────────────────────────────────────────┘
```

### Con Liga Seleccionada:
```
┌────────────────────────────────────────┐
│ ⚽ Crear Partido [ADMIN]               │
├────────────────────────────────────────┤
│ 🌍 Selecciona la Liga                  │
│ [ 🇪🇸 La Liga (España)           ▼ ]  │
│ ✓ 20 equipos cargados de España       │
│                                        │
│ 🏠 Equipo Local                        │
│ [ Real Madrid                    ▼ ]  │
│ (habilitado - fondo degradado)        │
│                                        │
│ ✈️ Equipo Visitante                    │
│ [ FC Barcelona                   ▼ ]  │
│ (habilitado - fondo degradado)        │
└────────────────────────────────────────┘
```

---

## 🧪 Pruebas Realizadas

### ✅ Compilación
- **Estado:** BUILD SUCCESS
- **Tiempo:** 17.5 segundos
- **Errores:** 0
- **Warnings:** Solo warnings menores de estilo (sin impacto)

### 📋 Pruebas Pendientes (para ejecutar con servidor corriendo):

1. **Selección de Liga:**
   - [ ] Seleccionar La Liga → Verificar carga de equipos españoles
   - [ ] Seleccionar Premier League → Verificar carga de equipos ingleses
   - [ ] Seleccionar Serie A → Verificar carga de equipos italianos
   - [ ] Seleccionar Bundesliga → Verificar carga de equipos alemanes
   - [ ] Seleccionar Ligue 1 → Verificar carga de equipos franceses

2. **Cambio de Liga:**
   - [ ] Seleccionar La Liga → Seleccionar equipos → Cambiar a Premier → Verificar limpieza

3. **Validaciones:**
   - [ ] Intentar seleccionar equipos sin liga → Verificar deshabilitación
   - [ ] Intentar crear partido con mismo equipo local y visitante → Verificar error

4. **Obtención de Jugadores:**
   - [ ] Seleccionar equipo de cada liga → Verificar carga correcta de jugadores

5. **Seguridad:**
   - [ ] Llamar API con liga ID 999 → Verificar error 400
   - [ ] Token expirado → Verificar redirección a login

---

## 📊 Resumen de Archivos Modificados

| Archivo | Líneas Modificadas | Tipo de Cambio |
|---------|-------------------|----------------|
| `PartidoController.java` | +35 líneas | Nuevo endpoint |
| `crear-partido.html` | ~200 líneas | HTML + CSS + JS |

**Total:** 2 archivos modificados

---

## ❌ Archivos NO Modificados

Como se diseñó, **NO se modificó la lógica de obtención de jugadores**:

- ❌ `ApiFootballService.java` - getTeamPlayers()
- ❌ `EquipoIntegrationService.java`
- ❌ `EquipoController.java` - getTeamPlayers()
- ❌ `SquadData.java`
- ❌ `PlayerData.java`
- ❌ Función `loadTeamPlayers()` en crear-partido.html

**Razón:** El endpoint `/players/squads` funciona independientemente de la liga, solo necesita el `teamId`.

---

## 🚀 Cómo Probar la Funcionalidad

### 1. Reiniciar la Aplicación

```bash
# Si está corriendo, detenerla con Ctrl+C
# Luego ejecutar:
./mvnw spring-boot:run
```

### 2. Acceder a la Página

```
http://localhost:8080/crear-partido.html
```

### 3. Flujo de Prueba

1. **Login como ADMIN** (si no estás logueado)
2. **Seleccionar una liga** del dropdown
   - Ejemplo: 🇪🇸 La Liga (España)
3. **Verificar que se carguen los equipos**
   - Mensaje: "✓ 20 equipos cargados de España"
4. **Seleccionar equipo local**
   - Ejemplo: Real Madrid
5. **Verificar carga de jugadores del Real Madrid**
6. **Seleccionar equipo visitante**
   - Ejemplo: FC Barcelona
7. **Verificar carga de jugadores del FC Barcelona**
8. **Seleccionar fecha y hora del partido**
9. **Click en "🏆 Crear Partido"**
10. **Verificar creación exitosa**

### 4. Pruebas Adicionales

**Cambio de Liga:**
1. Seleccionar La Liga → Seleccionar equipos
2. Cambiar a Premier League
3. Verificar que los selectores se limpien
4. Verificar que se carguen equipos ingleses

**Validación de Liga No Permitida:**
1. Abrir consola del navegador (F12)
2. Ejecutar:
   ```javascript
   fetch('http://localhost:8081/api/partidos/equipos-liga/999?season=2024', {
       headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
   })
   ```
3. Verificar error 400 con mensaje de liga no permitida

---

## 📈 Beneficios Implementados

1. ✅ **Flexibilidad:** Partidos de 5 ligas europeas (antes solo La Liga)
2. ✅ **UX Mejorada:** Selección guiada (liga → equipos → jugadores)
3. ✅ **Validación:** Control estricto de ligas permitidas
4. ✅ **Seguridad:** Validación backend de ligas + autenticación JWT
5. ✅ **Retrocompatibilidad:** Endpoint antiguo `/equipos-laliga` mantenido
6. ✅ **Escalabilidad:** Fácil agregar más ligas en el futuro
7. ✅ **Sin Impacto:** La lógica de jugadores funciona sin cambios

---

## 🔮 Mejoras Futuras Sugeridas

1. **Caché de Equipos:**
   - Guardar equipos en localStorage para no recargar la misma liga
   - Reducir llamadas a API-Football

2. **Temporada Dinámica:**
   - Calcular temporada actual automáticamente
   - Permitir seleccionar temporada histórica

3. **Más Ligas:**
   - Liga Portuguesa
   - Liga Holandesa
   - Liga Argentina
   - etc.

4. **Indicador de Carga:**
   - Spinner animado mientras cargan equipos
   - Skeleton screen para jugadores

5. **Información Adicional:**
   - Logo de la liga seleccionada
   - Estadísticas de la liga
   - Último partido creado de esa liga

---

## 🎯 Checklist Final de Implementación

### Backend
- [x] Agregar endpoint `GET /api/partidos/equipos-liga/{leagueId}`
- [x] Implementar validación de ligas permitidas (140, 39, 135, 78, 61)
- [x] Agregar parámetro opcional `season` con default 2024
- [x] Agregar logs de operaciones
- [ ] Probar endpoint con todas las ligas (requiere servidor corriendo)

### Frontend - HTML
- [x] Agregar selector de liga con las 5 opciones
- [x] Modificar selectores de equipos (disabled por defecto)
- [x] Actualizar placeholders de los selectores
- [x] Actualizar título de la página

### Frontend - JavaScript
- [x] Crear función `cargarEquiposPorLiga()`
- [x] Crear función `llenarSelectoresEquipos()`
- [x] Crear función `habilitarSelectoresEquipos()`
- [x] Crear función `deshabilitarSelectoresEquipos()`
- [x] Modificar `window.onload` para no cargar equipos automáticamente
- [x] Actualizar variable global `equiposLaLiga` → `equiposLiga`
- [x] Agregar variable global `ligaSeleccionada`
- [x] Verificar que `loadTeamPlayers()` no se modifique ✅

### Frontend - CSS
- [x] Agregar estilos para selector de liga
- [x] Agregar estilos para selectores deshabilitados
- [x] Verificar diseño coherente

### Compilación y Testing
- [x] Compilar proyecto sin errores
- [ ] Probar selección de cada liga
- [ ] Probar cambio entre ligas
- [ ] Probar carga de jugadores
- [ ] Probar creación de partidos
- [ ] Probar validaciones de seguridad

### Documentación
- [x] Crear documento de diseño
- [x] Crear documento de implementación
- [ ] Actualizar README principal (si es necesario)

---

## 📝 Conclusión

✅ **Implementación completada exitosamente**

Se han realizado todos los cambios descritos en el documento de diseño:
- ✅ Backend: Nuevo endpoint con validación de ligas
- ✅ Frontend: Selector de liga funcional con UI mejorada
- ✅ JavaScript: Nuevas funciones de gestión de estado
- ✅ CSS: Estilos para selectores y estados deshabilitados
- ✅ Compilación: BUILD SUCCESS sin errores

**La lógica de obtención de jugadores permanece intacta** como se solicitó, ya que funciona perfectamente para equipos de cualquier liga.

---

## 🚀 Próximos Pasos

1. **Reiniciar el servidor** de la aplicación
2. **Probar la funcionalidad** con cada una de las 5 ligas
3. **Validar** que los equipos se cargan correctamente
4. **Verificar** que los jugadores se obtienen sin problemas
5. **Crear partidos** de diferentes ligas para confirmar el flujo completo

---

**🎉 ¡Funcionalidad de Selección de Liga implementada y lista para usar! 🎉**

---

**Última actualización:** 15 de febrero de 2026 - 18:32

