# Integración con API-Football

## 🎯 Visión General

Este proyecto ahora está completamente integrado con **API-Football (v3)** de RapidAPI, permitiendo:

- ✅ Buscar equipos reales de fútbol
- ✅ Obtener jugadores de equipos específicos
- ✅ Crear equipos con alineaciones automáticas
- ✅ Personalizar alineaciones seleccionando jugadores
- ✅ Obtener alineaciones de partidos reales
- ✅ Actualizar equipos con datos de partidos

---

## 🔑 Configuración de la API Key

### 1. Obtener una API Key Gratuita

1. Visita: https://www.api-football.com/
2. Crea una cuenta gratuita
3. El plan gratuito incluye:
   - ✅ 100 peticiones/día
   - ✅ Acceso a todos los endpoints principales
   - ✅ Datos en tiempo real

### 2. Configurar en el Proyecto

Edita `src/main/resources/application.properties`:

```properties
# API-Football Configuration
api.football.key=TU_API_KEY_AQUI
api.football.base-url=https://v3.football.api-sports.io
```

---

## 📦 Nuevas Clases Creadas

### Modelos de API-Football (`model/apifootball/`)

| Clase | Descripción | Endpoint API |
|-------|-------------|--------------|
| `ApiFootballResponse<T>` | Respuesta genérica de la API | Todos |
| `TeamData` | Información de equipos | `/teams` |
| `PlayerData` | Información de jugadores | `/players` |
| `LineupData` | Alineaciones de partidos | `/fixtures/lineups` |
| `FixtureData` | Información de partidos | `/fixtures` |

### Servicios

| Servicio | Función |
|----------|---------|
| `ApiFootballService` | Cliente HTTP para consumir la API |
| `ApiFootballMapperService` | Convierte datos de API a nuestro modelo |
| `EquipoIntegrationService` | Integración completa para gestión de equipos |

### Controlador REST

- `EquipoController` - Endpoints REST para gestión de equipos

---

## 🚀 Endpoints Disponibles

### 1. Buscar Equipos en API-Football

```http
GET /api/equipos/api-football/search?name={nombre}
```

**Ejemplo:**
```bash
curl "http://localhost:8081/api/equipos/api-football/search?name=Barcelona"
```

**Respuesta:**
```json
[
  {
    "team": {
      "id": 529,
      "name": "Barcelona",
      "code": "BAR",
      "country": "Spain",
      "logo": "https://media.api-sports.io/football/teams/529.png"
    },
    "venue": {
      "id": 655,
      "name": "Camp Nou",
      "city": "Barcelona"
    }
  }
]
```

---

### 2. Obtener Jugadores de un Equipo

```http
GET /api/equipos/api-football/team/{teamId}/players?season={year}
```

**Ejemplo:**
```bash
curl "http://localhost:8081/api/equipos/api-football/team/529/players?season=2024"
```

**Respuesta:** Lista de jugadores con estadísticas

---

### 3. Obtener Alineaciones de un Partido

```http
GET /api/equipos/api-football/fixture/{fixtureId}/lineups
```

**Ejemplo:**
```bash
curl "http://localhost:8081/api/equipos/api-football/fixture/867946/lineups"
```

**Respuesta:**
```json
[
  {
    "formacion": "4-3-3",
    "entrenador": "Xavi Hernández",
    "apiTeamId": 529,
    "logoEquipo": "https://...",
    "titulares": [
      {
        "id": 306,
        "nombre": "Marc-André ter Stegen",
        "numero": 1,
        "posicion": "GK",
        "grid": "1:1"
      }
      // ... más jugadores
    ],
    "suplentes": [...]
  }
]
```

---

### 4. Crear Equipo desde API-Football (Automático)

```http
POST /api/equipos/create-from-api
Content-Type: application/json
Authorization: Bearer {token}

{
  "apiTeamId": 529,
  "season": 2024
}
```

**Descripción:** Crea un equipo automáticamente con los jugadores más titulares del equipo.

---

### 5. Crear Equipo Personalizado

```http
POST /api/equipos/create-custom
Content-Type: application/json
Authorization: Bearer {token}

{
  "apiTeamId": 529,
  "season": 2024,
  "formation": "4-3-3",
  "titularesIds": [306, 2128, 1459, 30701, 30846, 276, 30902, 1486, 156, 30964, 882],
  "suplentesIds": [25759, 31086, 1531]
}
```

**Descripción:** Crea un equipo seleccionando manualmente los jugadores.

---

### 6. Actualizar Alineación desde un Partido

```http
PUT /api/equipos/{equipoId}/update-lineup?fixtureId={fixtureId}
Authorization: Bearer {token}
```

**Descripción:** Actualiza la alineación de un equipo existente con datos de un partido real.

---

## 💡 Flujo de Uso Típico

### Opción A: Crear Equipo Automáticamente

```bash
# 1. Buscar tu equipo favorito
curl "http://localhost:8081/api/equipos/api-football/search?name=Real Madrid"

# 2. Anotar el "team.id" (ej: 541)

# 3. Crear equipo automáticamente
curl -X POST "http://localhost:8081/api/equipos/create-from-api?apiTeamId=541&season=2024" \
  -H "Authorization: Bearer TU_TOKEN_JWT"
```

### Opción B: Crear Equipo Personalizado

```bash
# 1. Buscar equipo
curl "http://localhost:8081/api/equipos/api-football/search?name=Liverpool"

# 2. Obtener jugadores del equipo (id: 40)
curl "http://localhost:8081/api/equipos/api-football/team/40/players?season=2024"

# 3. Seleccionar IDs de jugadores para titulares y suplentes

# 4. Crear equipo personalizado
curl -X POST "http://localhost:8081/api/equipos/create-custom" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TU_TOKEN_JWT" \
  -d '{
    "apiTeamId": 40,
    "season": 2024,
    "formation": "4-3-3",
    "titularesIds": [306, 2128, 1459, 30701, 30846, 276, 30902, 1486, 156, 30964, 882],
    "suplentesIds": [25759, 31086]
  }'
```

### Opción C: Usar Alineación de un Partido Real

```bash
# 1. Buscar próximos partidos de una liga (ej: Premier League = 39)
# (Requiere llamada directa a API-Football o implementar endpoint adicional)

# 2. Obtener alineaciones del partido
curl "http://localhost:8081/api/equipos/api-football/fixture/867946/lineups"

# 3. Actualizar equipo existente con esa alineación
curl -X PUT "http://localhost:8081/api/equipos/5/update-lineup?fixtureId=867946" \
  -H "Authorization: Bearer TU_TOKEN_JWT"
```

---

## 🔧 Modelo de Datos Actualizado

### `EquipoDetalles` (Almacenado como JSON en BD)

```json
{
  "formacion": "4-3-3",
  "entrenador": "Carlo Ancelotti",
  "apiTeamId": 541,
  "apiFixtureId": 867946,
  "logoEquipo": "https://media.api-sports.io/football/teams/541.png",
  "titulares": [
    {
      "id": 276,
      "nombre": "Thibaut Courtois",
      "numero": 1,
      "posicion": "GK",
      "grid": "1:1"
    },
    {
      "id": 1460,
      "nombre": "Dani Carvajal",
      "numero": 2,
      "posicion": "DEF",
      "grid": "2:4"
    }
    // ... 9 jugadores más (11 total)
  ],
  "suplentes": [
    {
      "id": 1486,
      "nombre": "Eden Hazard",
      "numero": 7,
      "posicion": "FWD",
      "grid": null
    }
    // ... más suplentes
  ]
}
```

---

## 📊 Ventajas de la Integración

### ✅ Datos Reales

- Equipos reales con logos oficiales
- Jugadores actualizados con estadísticas
- Alineaciones de partidos oficiales
- Formaciones tácticas reales

### ✅ Flexibilidad

- Crear equipos automáticamente o personalizados
- Actualizar alineaciones dinámicamente
- Mantener histórico de alineaciones por partido

### ✅ Escalabilidad

- Cacheable con Redis (ya configurado en el proyecto)
- Manejo de errores robusto
- Logs detallados para debugging

---

## 🎮 Casos de Uso

### 1. **Fantasy Football**
Los usuarios crean sus equipos ideales combinando jugadores de diferentes equipos reales.

### 2. **Análisis Táctico**
Comparar alineaciones de diferentes partidos del mismo equipo.

### 3. **Predicciones**
Crear equipos para próximos partidos y votarlos.

### 4. **Competencias**
Los usuarios compiten creando el mejor equipo posible con restricciones (presupuesto, liga, etc.).

---

## 🛠️ Personalización Adicional

### Agregar Endpoint de Búsqueda de Partidos

Para buscar partidos próximos, puedes usar:

```java
@GetMapping("/api-football/fixtures/upcoming")
public ResponseEntity<List<FixtureData>> getUpcomingFixtures(
    @RequestParam Integer leagueId,
    @RequestParam(defaultValue = "2024") Integer season,
    @RequestParam(defaultValue = "10") Integer next
) {
    // Ya implementado en ApiFootballService.getUpcomingFixtures()
}
```

### Agregar Caché con Redis

```java
@Cacheable(value = "teams", key = "#teamId")
public TeamData getTeamById(Integer teamId) {
    // ... código existente
}
```

---

## 📝 IDs de Ligas Principales

| Liga | ID | País |
|------|----|----- |
| Premier League | 39 | Inglaterra |
| La Liga | 140 | España |
| Serie A | 135 | Italia |
| Bundesliga | 78 | Alemania |
| Ligue 1 | 61 | Francia |
| Champions League | 2 | UEFA |
| Copa del Rey | 143 | España |

---

## 🚨 Límites y Consideraciones

### Plan Gratuito de API-Football

- ✅ 100 peticiones/día
- ⚠️ No incluye datos históricos completos
- ⚠️ Delay de ~15 minutos en datos en vivo

### Recomendaciones

1. **Implementa caché** para reducir llamadas
2. **Maneja errores** cuando se alcanza el límite
3. **Considera plan de pago** para producción

---

## 🔍 Testing

### Verificar Conexión a la API

```bash
curl "https://v3.football.api-sports.io/status" \
  -H "x-rapidapi-key: TU_API_KEY" \
  -H "x-rapidapi-host: v3.football.api-sports.io"
```

### Swagger UI

Una vez iniciada la aplicación, visita:
```
http://localhost:8081/swagger-ui.html
```

Allí encontrarás todos los endpoints documentados y podrás probarlos interactivamente.

---

## 📚 Recursos Adicionales

- **Documentación oficial:** https://www.api-football.com/documentation-v3
- **Portal de desarrolladores:** https://dashboard.api-football.com/
- **Status de la API:** https://status.api-football.com/

---

**¡Integración completada! 🎉**

Tu aplicación ahora puede consumir datos reales de fútbol y crear equipos profesionales con alineaciones oficiales.
