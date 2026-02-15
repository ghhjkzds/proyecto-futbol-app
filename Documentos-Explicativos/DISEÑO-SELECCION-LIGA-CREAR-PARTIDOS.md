# 🌍 DISEÑO: Selección de Liga para Crear Partidos

## 📅 Fecha
15 de febrero de 2026

---

## 🎯 Objetivo

Modificar la funcionalidad de **Crear Partido** para permitir al usuario seleccionar primero la liga europea de la cual desea crear el partido, y luego cargar dinámicamente los equipos de esa liga.

---

## 📋 Requisitos Funcionales

### 1. **Selector de Liga**
- Mostrar un dropdown con las 5 principales ligas europeas:
  - 🇪🇸 **La Liga** (España)
  - 🏴󠁧󠁢󠁥󠁮󠁧󠁿 **Premier League** (Inglaterra)
  - 🇮🇹 **Serie A** (Italia)
  - 🇩🇪 **Bundesliga** (Alemania)
  - 🇫🇷 **Ligue 1** (Francia)

### 2. **Carga Dinámica de Equipos**
- Al seleccionar una liga, cargar automáticamente los equipos de esa liga
- Limpiar las selecciones de equipos anteriores al cambiar de liga

### 3. **Flujo de Usuario**
```
1. Usuario selecciona una liga del dropdown
   ↓
2. Sistema carga equipos de la liga seleccionada
   ↓
3. Usuario selecciona equipo local
   ↓
4. Sistema carga jugadores del equipo local
   ↓
5. Usuario selecciona equipo visitante
   ↓
6. Sistema carga jugadores del equipo visitante
   ↓
7. Usuario selecciona fecha del partido
   ↓
8. Usuario crea el partido
```

---

## 🗺️ IDs de Ligas en API-Football

Según la documentación de API-Football, los IDs de las ligas europeas son:

| Liga | País | ID API-Football | Temporada Actual |
|------|------|-----------------|------------------|
| **La Liga** | España | `140` | 2024 |
| **Premier League** | Inglaterra | `39` | 2024 |
| **Serie A** | Italia | `135` | 2024 |
| **Bundesliga** | Alemania | `78` | 2024 |
| **Ligue 1** | Francia | `61` | 2024 |

---

## 🔧 Cambios Necesarios

### 1. **Backend - PartidoController.java**

#### **Cambio: Endpoint dinámico para obtener equipos por liga**

**Ubicación:** `src/main/java/com/futbol/proyectoacd/controller/PartidoController.java`

**Estado Actual:**
```java
@Operation(summary = "Obtener equipos de La Liga desde API-Football")
@GetMapping("/equipos-laliga")
public ResponseEntity<?> getEquiposLaLiga() {
    try {
        // La Liga ID: 140
        List<TeamData> teams = apiFootballService.searchTeamsByLeague(140, 2024);
        return ResponseEntity.ok(teams);
    } catch (Exception e) {
        log.error("Error al obtener equipos de La Liga", e);
        Map<String, String> error = new HashMap<>();
        error.put("error", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

**Nuevo Endpoint Propuesto:**
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

**Acción:**
- ✅ **Mantener el endpoint actual** `/equipos-laliga` por retrocompatibilidad
- ✅ **Agregar el nuevo endpoint** `/equipos-liga/{leagueId}` con parámetro dinámico
- ✅ **Validar** que solo se permitan las 5 ligas europeas principales

---

### 2. **Frontend - crear-partido.html**

#### **Cambios en el HTML (estructura)**

**Ubicación:** `src/main/resources/static/crear-partido.html`

**Agregar selector de liga ANTES del formulario de equipos:**

```html
<div class="form-section">
    <h2>⚽ Información del Partido</h2>
    
    <!-- NUEVO: Selector de Liga -->
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

    <div class="form-row">
        <div class="form-group">
            <label for="equipoLocal">🏠 Equipo Local</label>
            <select id="equipoLocal" onchange="loadTeamPlayers('local')" disabled>
                <option value="">Primero selecciona una liga...</option>
            </select>
        </div>

        <div class="form-group">
            <label for="equipoVisitante">✈️ Equipo Visitante</label>
            <select id="equipoVisitante" onchange="loadTeamPlayers('visitante')" disabled>
                <option value="">Primero selecciona una liga...</option>
            </select>
        </div>
    </div>

    <!-- resto del formulario... -->
</div>
```

**Estado inicial:**
- Los selectores de equipos están **deshabilitados** hasta que se seleccione una liga
- Mensaje placeholder: "Primero selecciona una liga..."

---

#### **Cambios en JavaScript**

**Ubicación:** `src/main/resources/static/crear-partido.html` (sección `<script>`)

##### **A) Modificar variables globales**

```javascript
const API_URL = 'http://localhost:8081/api';
let equiposLiga = [];  // Cambiar nombre: equiposLaLiga → equiposLiga
let equipoLocalData = null;
let equipoVisitanteData = null;
let ligaSeleccionada = null;  // NUEVO: Guardar liga seleccionada
```

##### **B) Eliminar/Modificar carga automática al inicio**

**ANTES:**
```javascript
// Cargar equipos de La Liga al iniciar
window.onload = cargarEquiposLaLiga;
```

**AHORA:**
```javascript
// Solo verificar autenticación al iniciar
window.onload = function() {
    verificarAuth();
};
```

##### **C) Nueva función: cargarEquiposPorLiga()**

```javascript
/**
 * Cargar equipos de la liga seleccionada
 */
async function cargarEquiposPorLiga() {
    if (!verificarAuth()) return;

    const selectorLiga = document.getElementById('selectorLiga');
    const ligaId = selectorLiga.value;

    // Si no hay liga seleccionada, deshabilitar equipos
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

        // Detectar si el token ha expirado
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

        // Llenar los selectores de equipos
        llenarSelectoresEquipos();

        // Habilitar selectores
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

##### **D) Nueva función: llenarSelectoresEquipos()**

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

##### **E) Nuevas funciones: habilitar/deshabilitar selectores**

```javascript
/**
 * Habilitar selectores de equipos
 */
function habilitarSelectoresEquipos() {
    document.getElementById('equipoLocal').disabled = false;
    document.getElementById('equipoVisitante').disabled = false;
}

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

##### **F) Modificar función loadTeamPlayers()**

**NO REQUIERE CAMBIOS** - La función `loadTeamPlayers()` seguirá funcionando igual porque:
- Sigue usando el mismo endpoint: `/api/equipos/api-football/team/{teamId}/players`
- El endpoint de jugadores NO depende de la liga, solo del `teamId`
- Los jugadores se obtienen por equipo, no por liga

**Función actual (MANTENER):**
```javascript
async function loadTeamPlayers(tipo) {
    const selectId = tipo === 'local' ? 'equipoLocal' : 'equipoVisitante';
    const teamId = document.getElementById(selectId).value;

    if (!teamId) {
        document.getElementById('teamsContainer').style.display = 'none';
        return;
    }

    try {
        showAlert(`Cargando jugadores del equipo ${tipo}...`, 'warning');

        const response = await fetch(
            `${API_URL}/equipos/api-football/team/${teamId}/players?season=2024`,
            {
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('token')}`
                }
            }
        );

        if (!response.ok) {
            throw new Error('Error al cargar jugadores');
        }

        const players = await response.json();

        // Guardar datos del equipo
        if (tipo === 'local') {
            equipoLocalData = {
                teamId: teamId,
                teamName: document.getElementById(selectId).options[document.getElementById(selectId).selectedIndex].text,
                players: players
            };
        } else {
            equipoVisitanteData = {
                teamId: teamId,
                teamName: document.getElementById(selectId).options[document.getElementById(selectId).selectedIndex].text,
                players: players
            };
        }

        // Renderizar jugadores
        renderTeamPlayers(tipo, players);

        // Mostrar contenedor si ambos equipos tienen datos
        if (equipoLocalData && equipoVisitanteData) {
            document.getElementById('teamsContainer').style.display = 'grid';
        }

        hideAlert();

    } catch (error) {
        console.error('Error:', error);
        showAlert('Error al cargar jugadores: ' + error.message, 'error');
    }
}
```

**✅ NO SE MODIFICA** - Esta función ya funciona correctamente con cualquier equipo, independientemente de la liga.

---

### 3. **Estilos CSS (opcional)**

Agregar estilos para mejorar la visualización del selector de liga:

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

## ✅ Validaciones Implementadas

### 1. **Backend**
- ✅ Validar que el `leagueId` sea uno de los 5 permitidos (140, 39, 135, 78, 61)
- ✅ Retornar error 400 si se intenta usar una liga no permitida
- ✅ Mantener validación de token JWT
- ✅ Log de todas las operaciones

### 2. **Frontend**
- ✅ Deshabilitar selectores de equipos hasta que se seleccione una liga
- ✅ Limpiar selecciones al cambiar de liga
- ✅ Validar que se hayan seleccionado equipos diferentes
- ✅ Validar que se haya seleccionado una fecha
- ✅ Manejo de errores de autenticación (token expirado)

---

## 🔄 Flujo Completo de Interacción

### Diagrama de Flujo:

```
┌─────────────────────────────────────────────────────┐
│ 1. Usuario accede a crear-partido.html             │
└────────────────────┬────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────┐
│ 2. Sistema verifica autenticación                  │
│    - Si NO autenticado → Redirige a login.html     │
│    - Si autenticado → Muestra formulario           │
└────────────────────┬────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────┐
│ 3. Usuario selecciona una LIGA del dropdown        │
│    Opciones: La Liga, Premier, Serie A,            │
│              Bundesliga, Ligue 1                    │
└────────────────────┬────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────┐
│ 4. JavaScript llama: cargarEquiposPorLiga()        │
│    → GET /api/partidos/equipos-liga/{leagueId}     │
└────────────────────┬────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────┐
│ 5. Backend valida liga y llama a API-Football      │
│    → ApiFootballService.searchTeamsByLeague()      │
└────────────────────┬────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────┐
│ 6. Sistema llena selectores de equipos             │
│    - Habilita selectores                            │
│    - Muestra equipos de la liga                     │
└────────────────────┬────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────┐
│ 7. Usuario selecciona EQUIPO LOCAL                 │
└────────────────────┬────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────┐
│ 8. JavaScript llama: loadTeamPlayers('local')      │
│    → GET /api/equipos/api-football/team/{id}/...   │
└────────────────────┬────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────┐
│ 9. Backend obtiene jugadores usando /players/squads│
│    → ApiFootballService.getTeamPlayers()           │
└────────────────────┬────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────┐
│ 10. Sistema renderiza jugadores del equipo local   │
└────────────────────┬────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────┐
│ 11. Usuario selecciona EQUIPO VISITANTE            │
└────────────────────┬────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────┐
│ 12. Repite pasos 8-10 para equipo visitante        │
└────────────────────┬────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────┐
│ 13. Usuario selecciona FECHA del partido           │
└────────────────────┬────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────┐
│ 14. Usuario hace click en "Crear Partido"          │
└────────────────────┬────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────┐
│ 15. Sistema valida y crea partido                  │
│     → POST /api/partidos/crear                      │
└────────────────────┬────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────┐
│ 16. ✅ Partido creado exitosamente                  │
└─────────────────────────────────────────────────────┘
```

---

## 📊 Impacto en la Obtención de Jugadores

### ❓ ¿Necesita cambios la lógica de obtener jugadores?

### ✅ **RESPUESTA: NO REQUIERE CAMBIOS**

#### **Razones:**

1. **Endpoint independiente de la liga:**
   ```
   GET /api/equipos/api-football/team/{teamId}/players?season=2024
   ```
   - Este endpoint solo necesita el `teamId`
   - NO depende de la liga (`leagueId`)
   - Funciona para cualquier equipo de cualquier liga

2. **API-Football endpoint `/players/squads`:**
   ```
   GET /players/squads?team={teamId}
   ```
   - Solo requiere el parámetro `team`
   - Retorna TODOS los jugadores de la plantilla
   - NO requiere información de liga

3. **Flujo actual ya funciona:**
   - El usuario selecciona un equipo (da igual la liga)
   - El sistema obtiene el `teamId`
   - El backend consulta `/players/squads?team={teamId}`
   - Retorna los jugadores del equipo

4. **Compatibilidad total:**
   - Los equipos de La Liga, Premier, Serie A, Bundesliga y Ligue 1 están todos en API-Football
   - El endpoint `/players/squads` funciona para todos ellos
   - No hay diferencia en la respuesta según la liga

### 📝 Código Actual (NO SE MODIFICA)

**ApiFootballService.java:**
```java
public List<PlayerData> getTeamPlayers(Integer teamId, Integer season) {
    try {
        log.info("Obteniendo plantilla del equipo {} usando /players/squads", teamId);

        ApiFootballResponse<SquadData> response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/players/squads")
                        .queryParam("team", teamId)  // ← Solo necesita teamId
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiFootballResponse<SquadData>>() {})
                .timeout(Duration.ofSeconds(15))
                .block();

        // ... resto del código
    }
}
```

**Funcionará igual para:**
- ✅ Real Madrid (La Liga, teamId: 541)
- ✅ Manchester City (Premier League, teamId: 50)
- ✅ Inter Milan (Serie A, teamId: 505)
- ✅ Bayern Munich (Bundesliga, teamId: 157)
- ✅ PSG (Ligue 1, teamId: 85)

### ✅ **Conclusión**

**NO SE REQUIEREN CAMBIOS** en:
- ❌ `ApiFootballService.getTeamPlayers()`
- ❌ `EquipoIntegrationService.getAvailablePlayers()`
- ❌ `EquipoController.getTeamPlayers()`
- ❌ Función JavaScript `loadTeamPlayers()`
- ❌ Modelo `SquadData.java`
- ❌ Modelo `PlayerData.java`

La lógica actual de obtención de jugadores **ya es compatible** con equipos de cualquier liga europea.

---

## 🎨 Wireframe de la Interfaz

### Estado Inicial (sin liga seleccionada):

```
┌────────────────────────────────────────────────────┐
│  ⚽ Crear Partido - La Liga                        │
├────────────────────────────────────────────────────┤
│                                                    │
│  🌍 Selecciona la Liga                             │
│  ┌──────────────────────────────────────────────┐ │
│  │ Selecciona una liga...                    ▼ │ │
│  └──────────────────────────────────────────────┘ │
│                                                    │
│  🏠 Equipo Local            ✈️ Equipo Visitante    │
│  ┌──────────────────────┐  ┌──────────────────┐  │
│  │ Primero selecciona   │  │ Primero selecciona│  │
│  │ una liga...       ▼ │  │ una liga...    ▼ │  │
│  └──────────────────────┘  └──────────────────┘  │
│  (deshabilitado)           (deshabilitado)        │
│                                                    │
│  📅 Fecha y Hora del Partido                       │
│  ┌──────────────────────────────────────────────┐ │
│  │ dd/mm/yyyy hh:mm                             │ │
│  └──────────────────────────────────────────────┘ │
│                                                    │
│  [ 🏆 Crear Partido ]  [ ← Volver al Inicio ]    │
└────────────────────────────────────────────────────┘
```

### Estado con Liga Seleccionada (La Liga):

```
┌────────────────────────────────────────────────────┐
│  ⚽ Crear Partido - La Liga                        │
├────────────────────────────────────────────────────┤
│                                                    │
│  🌍 Selecciona la Liga                             │
│  ┌──────────────────────────────────────────────┐ │
│  │ 🇪🇸 La Liga (España)                      ▼ │ │
│  └──────────────────────────────────────────────┘ │
│  ✓ 20 equipos cargados de España                  │
│                                                    │
│  🏠 Equipo Local            ✈️ Equipo Visitante    │
│  ┌──────────────────────┐  ┌──────────────────┐  │
│  │ Real Madrid       ▼ │  │ FC Barcelona  ▼ │  │
│  └──────────────────────┘  └──────────────────┘  │
│  (habilitado)              (habilitado)           │
│                                                    │
│  [Visualización de jugadores...]                  │
│                                                    │
│  📅 Fecha y Hora del Partido                       │
│  ┌──────────────────────────────────────────────┐ │
│  │ 20/02/2026 21:00                             │ │
│  └──────────────────────────────────────────────┘ │
│                                                    │
│  [ 🏆 Crear Partido ]  [ ← Volver al Inicio ]    │
└────────────────────────────────────────────────────┘
```

---

## 📊 Resumen de Archivos a Modificar

| Archivo | Ubicación | Tipo de Cambio | Complejidad |
|---------|-----------|----------------|-------------|
| **PartidoController.java** | `src/main/java/.../controller/` | Agregar nuevo endpoint | 🟢 Baja |
| **crear-partido.html** | `src/main/resources/static/` | Agregar selector + JS | 🟡 Media |

### Total de Archivos a Modificar: **2**

---

## 🧪 Casos de Prueba

### 1. **Seleccionar Liga**
- ✅ Seleccionar La Liga → Carga 20 equipos españoles
- ✅ Seleccionar Premier League → Carga 20 equipos ingleses
- ✅ Seleccionar Serie A → Carga 20 equipos italianos
- ✅ Seleccionar Bundesliga → Carga 18 equipos alemanes
- ✅ Seleccionar Ligue 1 → Carga 18 equipos franceses

### 2. **Cambiar de Liga**
- ✅ Seleccionar La Liga → Seleccionar equipos → Cambiar a Premier → Limpiar selecciones
- ✅ Verificar que los equipos se actualicen correctamente

### 3. **Validaciones**
- ✅ Intentar seleccionar equipos sin liga → Selectores deshabilitados
- ✅ Intentar crear partido sin seleccionar liga → Error
- ✅ Intentar crear partido con el mismo equipo local y visitante → Error

### 4. **Obtención de Jugadores**
- ✅ Seleccionar equipo de La Liga → Cargar jugadores correctamente
- ✅ Seleccionar equipo de Premier League → Cargar jugadores correctamente
- ✅ Seleccionar equipo de Serie A → Cargar jugadores correctamente
- ✅ Seleccionar equipo de Bundesliga → Cargar jugadores correctamente
- ✅ Seleccionar equipo de Ligue 1 → Cargar jugadores correctamente

### 5. **Seguridad**
- ✅ Intentar acceder con liga ID no permitida → Error 400
- ✅ Token expirado → Redirige a login
- ✅ Usuario sin rol ADMIN → Error al crear partido

---

## 🚀 Plan de Implementación

### Fase 1: Backend (15-20 min)
1. ✅ Agregar nuevo endpoint `/equipos-liga/{leagueId}` en `PartidoController.java`
2. ✅ Implementar validación de ligas permitidas
3. ✅ Probar endpoint con Postman/Swagger

### Fase 2: Frontend (30-40 min)
1. ✅ Agregar selector de liga en HTML
2. ✅ Implementar función `cargarEquiposPorLiga()`
3. ✅ Implementar funciones `habilitar/deshabilitarSelectoresEquipos()`
4. ✅ Implementar función `llenarSelectoresEquipos()`
5. ✅ Modificar inicialización de la página
6. ✅ Ajustar estilos CSS

### Fase 3: Pruebas (20-30 min)
1. ✅ Probar selección de cada liga
2. ✅ Probar cambio entre ligas
3. ✅ Probar carga de jugadores de diferentes ligas
4. ✅ Probar creación de partidos de diferentes ligas
5. ✅ Verificar validaciones

### Fase 4: Documentación (10 min)
1. ✅ Actualizar README si es necesario
2. ✅ Documentar nuevos endpoints en Swagger

**Tiempo Total Estimado: 75-100 minutos (1.5 - 2 horas)**

---

## 📌 Notas Importantes

### 1. **Retrocompatibilidad**
- Mantener el endpoint `/equipos-laliga` para no romper integraciones existentes
- El nuevo endpoint `/equipos-liga/{leagueId}` es adicional

### 2. **API-Football Rate Limits**
- Cada llamada a `/teams?league={id}&season={year}` consume 1 request
- Con 5 ligas, el usuario podría hacer hasta 5 requests si cambia de liga múltiples veces
- **Solución:** Implementar caché en el frontend para no recargar la misma liga

### 3. **Temporada**
- Actualmente hardcodeado a 2024
- **Mejora futura:** Hacer dinámico basado en la fecha actual

### 4. **Seguridad**
- Solo usuarios con rol `ADMIN` pueden crear partidos
- Validación en backend de ligas permitidas

---

## ✅ Checklist de Implementación

### Backend
- [ ] Agregar endpoint `GET /api/partidos/equipos-liga/{leagueId}`
- [ ] Implementar validación de ligas permitidas (140, 39, 135, 78, 61)
- [ ] Agregar parámetro opcional `season` con default 2024
- [ ] Agregar logs de operaciones
- [ ] Probar endpoint con todas las ligas

### Frontend - HTML
- [ ] Agregar selector de liga con las 5 opciones
- [ ] Modificar selectores de equipos (disabled por defecto)
- [ ] Actualizar placeholders de los selectores

### Frontend - JavaScript
- [ ] Crear función `cargarEquiposPorLiga()`
- [ ] Crear función `llenarSelectoresEquipos()`
- [ ] Crear función `habilitarSelectoresEquipos()`
- [ ] Crear función `deshabilitarSelectoresEquipos()`
- [ ] Modificar `window.onload` para no cargar equipos automáticamente
- [ ] Actualizar variable global `equiposLaLiga` → `equiposLiga`
- [ ] Agregar variable global `ligaSeleccionada`
- [ ] Verificar que `loadTeamPlayers()` siga funcionando (no modificar)

### Frontend - CSS
- [ ] Agregar estilos para selector de liga
- [ ] Agregar estilos para selectores deshabilitados
- [ ] Verificar responsive design

### Testing
- [ ] Probar selección de cada liga
- [ ] Probar cambio entre ligas
- [ ] Probar carga de jugadores
- [ ] Probar creación de partidos
- [ ] Probar validaciones de seguridad

### Documentación
- [ ] Actualizar documentación de API si existe
- [ ] Agregar anotaciones Swagger al nuevo endpoint

---

## 🎯 Resultado Esperado

Después de implementar estos cambios:

1. ✅ Usuario puede elegir entre 5 ligas europeas
2. ✅ Sistema carga dinámicamente equipos de la liga seleccionada
3. ✅ Usuario puede crear partidos de cualquiera de las 5 ligas
4. ✅ Lógica de obtención de jugadores sigue funcionando sin cambios
5. ✅ Validaciones de seguridad implementadas
6. ✅ Experiencia de usuario mejorada

---

**📝 ESTE ES UN DOCUMENTO DE DISEÑO - NO IMPLEMENTAR SIN APROBACIÓN**

**Próximos pasos:**
1. Revisar este diseño
2. Aprobar cambios
3. Implementar según checklist
4. Probar exhaustivamente
5. Documentar cambios finales

---

**Última actualización:** 15 de febrero de 2026

