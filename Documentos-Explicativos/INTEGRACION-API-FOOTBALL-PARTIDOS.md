# ⚽ INTEGRACIÓN CON API-FOOTBALL PARA PARTIDOS PROGRAMADOS

## 📅 Fecha de Implementación: 6 de Febrero de 2026

---

## 🎯 NUEVA FUNCIONALIDAD IMPLEMENTADA

### Obtener Partidos Programados desde API-Football

Ahora el sistema obtiene automáticamente los **partidos programados (scheduled)** de La Liga directamente desde la API de API-Football.

---

## ✅ ¿QUÉ SE HA IMPLEMENTADO?

### 1. **Nuevo Método en ApiFootballService** 🔧

**Ubicación:** `ApiFootballService.java`

**Método:** `getScheduledFixtures(Integer leagueId, Integer season)`

**Funcionalidad:**
- Consulta el endpoint `/fixtures` de API-Football
- Filtra por liga y temporada
- Filtra por estado `NS` (Not Started = Scheduled)
- Retorna solo partidos que aún no han comenzado

**Código:**
```java
public List<FixtureData> getScheduledFixtures(Integer leagueId, Integer season) {
    ApiFootballResponse<FixtureData> response = webClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/fixtures")
            .queryParam("league", leagueId)  // La Liga = 140
            .queryParam("season", season)     // 2024
            .queryParam("status", "NS")       // Not Started
            .build())
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<>() {})
        .block();
    
    // Filtrar solo los que tienen status NS
    return response.getResponse().stream()
        .filter(f -> "NS".equals(f.getFixture().getStatus().getShortStatus()))
        .collect(Collectors.toList());
}
```

---

### 2. **Nuevo Endpoint en PartidoController** 🌐

**Ubicación:** `PartidoController.java`

**Endpoint:** `GET /api/partidos/api-football/scheduled`

**Parámetros:**
- `leagueId` (opcional, default: 140 = La Liga)
- `season` (opcional, default: 2024)

**Ejemplo de uso:**
```
GET http://localhost:8081/api/partidos/api-football/scheduled?leagueId=140&season=2024
```

**Respuesta:**
```json
[
  {
    "fixture": {
      "id": 12345,
      "date": "2026-02-10T20:00:00+00:00",
      "status": {
        "short": "NS",
        "long": "Not Started"
      },
      "venue": {
        "name": "Camp Nou",
        "city": "Barcelona"
      }
    },
    "teams": {
      "home": {
        "id": 529,
        "name": "Barcelona"
      },
      "away": {
        "id": 541,
        "name": "Real Madrid"
      }
    },
    "league": {
      "id": 140,
      "name": "La Liga"
    }
  }
]
```

---

### 3. **Nuevo Endpoint para Crear Alineaciones desde API** 📝

**Ubicación:** `AlineacionController.java`

**Endpoint:** `POST /api/alineaciones/from-api-football`

**Funcionalidad:**
- Recibe datos de partidos de API-Football
- Crea automáticamente equipos si no existen
- Crea automáticamente partidos si no existen
- Evita duplicados (busca por equipos y fecha)
- Valida que el partido no se haya jugado
- Valida que no exista alineación previa del usuario

**Request Body:**
```json
{
  "apiFixtureId": 12345,
  "apiTeamId": 529,
  "teamName": "Barcelona",
  "homeTeamName": "Barcelona",
  "awayTeamName": "Real Madrid",
  "matchDate": "2026-02-10T20:00:00",
  "alineacion": {
    "formacion": "4-3-3",
    "titulares": [...],
    "suplentes": []
  }
}
```

---

### 4. **Frontend Actualizado** 💻

**Ubicación:** `crear-alineacion.html`

**Cambios:**

#### A) Carga de Partidos desde API-Football
```javascript
async function cargarPartidos() {
    // Obtiene partidos programados de La Liga
    const response = await fetch(
        `${API_URL}/partidos/api-football/scheduled?leagueId=140&season=2024`
    );
    
    const fixturesData = await response.json();
    
    // Convierte fixtures de API a formato interno
    partidos = fixturesData.map(fixture => ({
        apiFixtureId: fixture.fixture.id,
        equipoLocalId: fixture.teams.home.id,
        equipoLocalNombre: fixture.teams.home.name,
        equipoVisitanteId: fixture.teams.away.id,
        equipoVisitanteNombre: fixture.teams.away.name,
        fecha: fixture.fixture.date,
        estado: fixture.fixture.status.shortStatus,
        venue: fixture.fixture.venue?.name || 'Por confirmar'
    }));
}
```

#### B) Guardar Alineación con Datos de API
```javascript
async function guardarAlineacion() {
    const alineacionData = {
        apiFixtureId: partidoSeleccionado.apiFixtureId,
        apiTeamId: equipoSeleccionado.apiTeamId,
        teamName: equipoSeleccionado.nombre,
        homeTeamName: partidoSeleccionado.equipoLocalNombre,
        awayTeamName: partidoSeleccionado.equipoVisitanteNombre,
        matchDate: new Date(partidoSeleccionado.fecha).toISOString(),
        alineacion: alineacionDetalles
    };
    
    // Usa el nuevo endpoint
    const response = await fetch(`${API_URL}/alineaciones/from-api-football`, {
        method: 'POST',
        body: JSON.stringify(alineacionData)
    });
}
```

---

### 5. **Nuevo Método en PartidoRepository** 🗄️

**Método:** `findByEquipoLocalAndEquipoVisitanteAndFecha()`

**Funcionalidad:**
- Busca un partido existente por equipos y fecha
- Evita duplicados al crear partidos desde la API

**Código:**
```java
Optional<Partido> findByEquipoLocalAndEquipoVisitanteAndFecha(
    Equipo equipoLocal, 
    Equipo equipoVisitante, 
    LocalDateTime fecha
);
```

---

## 🔄 FLUJO COMPLETO

### Usuario Crea Alineación

```
1. Usuario → Crear Alineación
   ↓
2. Frontend → GET /api/partidos/api-football/scheduled
   ↓
3. Backend → API-Football (fixtures con status=NS)
   ↓
4. API-Football → Retorna partidos programados de La Liga
   ↓
5. Frontend → Muestra dropdown con partidos
   ↓
6. Usuario → Selecciona partido, equipo y jugadores
   ↓
7. Frontend → POST /api/alineaciones/from-api-football
   ↓
8. Backend → Busca/Crea equipos en BD
   ↓
9. Backend → Busca/Crea partido en BD
   ↓
10. Backend → Valida fecha (partido no jugado)
    ↓
11. Backend → Valida unicidad (1 alineación/equipo/partido/usuario)
    ↓
12. Backend → Guarda alineación
    ↓
13. Frontend → ✅ Alineación guardada exitosamente
```

---

## 📊 VENTAJAS DE ESTA IMPLEMENTACIÓN

### ✅ Datos Reales y Actualizados
- Los partidos vienen directamente de API-Football
- Siempre están actualizados
- No requiere que un admin cree partidos manualmente

### ✅ Validación Automática de Estado
- Solo muestra partidos con estado "Not Started"
- Los partidos en juego o finalizados no aparecen
- Validación doble (API + Backend)

### ✅ Creación Automática de Datos
- Crea equipos automáticamente si no existen
- Crea partidos automáticamente si no existen
- Evita duplicados inteligentemente

### ✅ Experiencia de Usuario Mejorada
- El usuario ve partidos reales de La Liga
- No ve partidos que ya se jugaron
- Información completa (equipos, fecha, estadio)

---

## 🎮 EJEMPLO DE USO

### 1. Usuario va a "Crear Alineación"

**Frontend carga partidos programados:**
```
📅 Cargando partidos programados desde API-Football...
🏟️ Partidos recibidos de API-Football: 15
📊 Total de partidos programados: 15
✅ 15 partidos programados de La Liga cargados desde API-Football
```

### 2. Dropdown muestra:

```
-- Selecciona un partido programado --
Barcelona vs Real Madrid - 10/02/2026 20:00 - Camp Nou
Sevilla vs Atlético Madrid - 08/02/2026 18:00 - Ramón Sánchez-Pizjuán
Real Betis vs Valencia - 09/02/2026 21:00 - Benito Villamarín
...
```

### 3. Usuario selecciona, configura y guarda

**Backend procesa:**
```
INFO: Creando alineación desde API-Football: Fixture 12345, Team 529
INFO: Equipo 'Barcelona' encontrado/creado
INFO: Equipo 'Real Madrid' encontrado/creado  
INFO: Partido encontrado/creado: Barcelona vs Real Madrid
INFO: Validando fecha... OK (partido futuro)
INFO: Validando unicidad... OK (primera alineación)
INFO: Alineación creada exitosamente
```

---

## 🔒 VALIDACIONES IMPLEMENTADAS

### 1. Validación de Estado del Partido ✅
- Solo acepta partidos con `status.short = "NS"`
- Filtrado en la API y en el backend

### 2. Validación de Fecha ✅
- Verifica que `partido.getFecha() > LocalDateTime.now()`
- Error 403 si el partido ya se jugó

### 3. Validación de Unicidad ✅
- Un usuario solo puede crear una alineación por equipo por partido
- Error 409 si ya existe

### 4. Creación Inteligente de Datos ✅
- Busca equipos por nombre antes de crear
- Busca partidos por equipos y fecha antes de crear
- Evita duplicados en la base de datos

---

## 🗄️ CAMBIOS EN BASE DE DATOS

### Partidos Creados Automáticamente

Cuando un usuario crea una alineación desde un partido de API-Football:

**Tabla `equipos`:**
```sql
INSERT INTO equipos (nombre, id_user, votos) 
VALUES ('Barcelona', 1, 0);

INSERT INTO equipos (nombre, id_user, votos) 
VALUES ('Real Madrid', 1, 0);
```

**Tabla `partidos`:**
```sql
INSERT INTO partidos (equipo_local_id, equipo_visitante_id, fecha, creado_por) 
VALUES (1, 2, '2026-02-10 20:00:00', 1);
```

**Tabla `alineaciones`:**
```sql
INSERT INTO alineaciones (partido_id, equipo_id, alineacion, created_by) 
VALUES (1, 1, '{"formacion":"4-3-3",...}', 1);
```

---

## 📝 ESTADOS DE PARTIDOS EN API-FOOTBALL

| Código | Descripción | ¿Se muestra? |
|--------|-------------|--------------|
| TBD | Time To Be Defined | ❌ No |
| NS | Not Started | ✅ **Sí** |
| 1H | First Half | ❌ No |
| HT | Halftime | ❌ No |
| 2H | Second Half | ❌ No |
| ET | Extra Time | ❌ No |
| P | Penalty | ❌ No |
| FT | Match Finished | ❌ No |
| AET | Match Finished After Extra Time | ❌ No |
| PEN | Match Finished After Penalty | ❌ No |
| BT | Break Time | ❌ No |
| SUSP | Match Suspended | ❌ No |
| INT | Match Interrupted | ❌ No |
| PST | Match Postponed | ❌ No |
| CANC | Match Cancelled | ❌ No |
| ABD | Match Abandoned | ❌ No |
| AWD | Technical Loss | ❌ No |
| WO | WalkOver | ❌ No |

**Solo los partidos con estado `NS` (Not Started) se muestran al usuario.**

---

## 🔍 LOGS DEL SISTEMA

### Al cargar partidos:
```
INFO [PartidoController] : Obteniendo partidos programados de La Liga desde API-Football
INFO [ApiFootballService] : Obteniendo partidos programados de la liga 140 temporada 2024
INFO [ApiFootballService] : Encontrados 15 partidos programados
INFO [PartidoController] : Encontrados 15 partidos programados
```

### Al crear alineación:
```
INFO [AlineacionController] : Creando alineación desde API-Football: Fixture 12345, Team 529
INFO [AlineacionController] : Alineación creada desde API-Football: Usuario user@email.com, Fixture 12345, Team 529
```

### Si intenta partido ya jugado:
```
WARN [AlineacionController] : Intento de crear alineación para partido ya jugado desde API. 
Fixture ID: 12345, Usuario: user@email.com
```

---

## 🎯 BENEFICIOS PARA EL PROYECTO

1. ✅ **Automatización**: No requiere crear partidos manualmente
2. ✅ **Datos Reales**: Información directa de API-Football
3. ✅ **Siempre Actualizado**: Los partidos se obtienen en tiempo real
4. ✅ **Validación Robusta**: Múltiples capas de validación
5. ✅ **Evita Duplicados**: Lógica inteligente de creación
6. ✅ **Mejor UX**: Usuario ve solo partidos válidos
7. ✅ **Escalable**: Funciona para cualquier liga (cambiando leagueId)

---

## 🚀 PRÓXIMOS PASOS SUGERIDOS

### Corto Plazo:
1. 🔄 **Actualización periódica**: Job que sincronice partidos cada hora
2. 📊 **Más ligas**: Permitir seleccionar otras ligas (Premier, Serie A, etc.)
3. 🎨 **Escudos de equipos**: Mostrar logos desde API-Football

### Medio Plazo:
1. 📅 **Calendario visual**: Vista de calendario con todos los partidos
2. 🔔 **Notificaciones**: Avisar cuando un partido esté por comenzar
3. 📈 **Estadísticas**: Mostrar stats de equipos desde la API

### Largo Plazo:
1. 🤖 **IA Predictiva**: Sugerir alineaciones basadas en datos históricos
2. 🏆 **Resultados reales**: Actualizar con resultados reales de partidos
3. 📱 **App móvil**: Versión nativa con push notifications

---

**¡Sistema completamente integrado con API-Football! ⚽🚀**
