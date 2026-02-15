# 🔄 MEJORA: Obtención Dinámica de Temporada Actual

## 📅 Fecha de Implementación: 6 de Febrero de 2026

---

## 🎯 PROBLEMA RESUELTO

### Antes:
- La temporada (season) estaba **hardcodeada** en el código (2024, 2025, etc.)
- Cada año había que modificar el código manualmente
- No era escalable ni mantenible

### Ahora:
- La temporada se obtiene **dinámicamente** de la API
- El sistema busca automáticamente la temporada marcada como `current: true`
- **No requiere modificaciones anuales**

---

## ✅ IMPLEMENTACIÓN

### 1. **Nuevo Modelo de Datos** 📦

**Archivo:** `LeagueInfoData.java`

Modelo para la respuesta del endpoint `/leagues`:

```java
@Data
public class LeagueInfoData {
    private League league;
    private Country country;
    private List<Season> seasons;  // Lista de temporadas
    
    @Data
    public static class Season {
        private Integer year;      // 2024, 2025, etc.
        private String start;      // Fecha inicio
        private String end;        // Fecha fin
        private Boolean current;   // ¿Es la temporada actual?
        private Coverage coverage;
    }
}
```

---

### 2. **Nuevo Método en ApiFootballService** 🔧

**Método:** `getCurrentSeason(Integer leagueId)`

**Lógica implementada:**

```java
public Integer getCurrentSeason(Integer leagueId) {
    // Paso 1: Llamar a GET /leagues?id=140
    ApiFootballResponse<LeagueInfoData> response = webClient.get()
        .uri("/leagues?id=" + leagueId)
        .retrieve()
        .block();
    
    LeagueInfoData leagueInfo = response.getResponse().get(0);
    
    // Paso 2: Buscar la temporada con current = true
    for (Season season : leagueInfo.getSeasons()) {
        if (Boolean.TRUE.equals(season.getCurrent())) {
            return season.getYear();  // Ej: 2024, 2025
        }
    }
    
    // Fallback: Si no hay current, tomar la última temporada
    return leagueInfo.getSeasons().getLast().getYear();
}
```

---

### 3. **Método Actualizado: getScheduledFixtures** ⚡

**Antes:**
```java
public List<FixtureData> getScheduledFixtures(Integer leagueId, Integer season) {
    // Requería pasar la temporada manualmente
}
```

**Ahora:**
```java
public List<FixtureData> getScheduledFixtures(Integer leagueId) {
    // Paso 1: Obtener temporada actual automáticamente
    Integer currentSeason = getCurrentSeason(leagueId);
    
    log.info("Temporada actual: {}", currentSeason);
    
    // Paso 2: Obtener partidos con status NS
    ApiFootballResponse<FixtureData> response = webClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/fixtures")
            .queryParam("league", leagueId)
            .queryParam("season", currentSeason)  // Usa temporada actual
            .queryParam("status", "NS")
            .build())
        .retrieve()
        .block();
    
    return response.getResponse();
}
```

---

### 4. **Controlador Actualizado** 🌐

**Endpoint:** `GET /api/partidos/api-football/scheduled`

**Antes:**
```java
@GetMapping("/api-football/scheduled")
public ResponseEntity<?> getPartidosProgramados(
    @RequestParam(defaultValue = "140") Integer leagueId,
    @RequestParam(defaultValue = "2024") Integer season  // ❌ Hardcoded
) {
    List<FixtureData> fixtures = apiFootballService.getScheduledFixtures(leagueId, season);
    return ResponseEntity.ok(fixtures);
}
```

**Ahora:**
```java
@GetMapping("/api-football/scheduled")
public ResponseEntity<?> getPartidosProgramados(
    @RequestParam(defaultValue = "140") Integer leagueId
) {
    // El servicio obtiene automáticamente la temporada actual
    List<FixtureData> fixtures = apiFootballService.getScheduledFixtures(leagueId);
    return ResponseEntity.ok(fixtures);
}
```

---

### 5. **Frontend Actualizado** 💻

**Archivo:** `crear-alineacion.html`

**Antes:**
```javascript
const response = await fetch(
    `${API_URL}/partidos/api-football/scheduled?leagueId=140&season=2024`  // ❌ Hardcoded
);
```

**Ahora:**
```javascript
// El backend obtiene automáticamente la temporada actual
const response = await fetch(
    `${API_URL}/partidos/api-football/scheduled?leagueId=140`  // ✅ Dinámico
);
```

---

## 🔄 FLUJO COMPLETO

### Cuando un usuario carga partidos programados:

```
1. Usuario → /crear-alineacion.html
   ↓
2. Frontend → GET /api/partidos/api-football/scheduled?leagueId=140
   ↓
3. Backend → getCurrentSeason(140)
   ↓
4. Backend → GET API-Football /leagues?id=140
   ↓
5. API-Football → Retorna info de La Liga con temporadas
   ↓
6. Backend → Busca season con "current": true
   ↓
7. Backend → Encuentra: { "year": 2025, "current": true }
   ↓
8. Backend → GET API-Football /fixtures?league=140&season=2025&status=NS
   ↓
9. API-Football → Retorna partidos programados de la temporada 2025
   ↓
10. Frontend → Muestra partidos al usuario
```

---

## 📊 EJEMPLO DE RESPUESTA DE /leagues

```json
{
  "response": [
    {
      "league": {
        "id": 140,
        "name": "La Liga",
        "type": "League"
      },
      "country": {
        "name": "Spain",
        "code": "ES"
      },
      "seasons": [
        {
          "year": 2023,
          "start": "2023-08-11",
          "end": "2024-05-26",
          "current": false
        },
        {
          "year": 2024,
          "start": "2024-08-15",
          "end": "2025-05-25",
          "current": false
        },
        {
          "year": 2025,
          "start": "2025-08-14",
          "end": "2026-05-24",
          "current": true  ← El sistema toma esta
        }
      ]
    }
  ]
}
```

---

## 🛡️ VALIDACIONES Y FALLBACKS

### 1. Si hay temporada con `current: true` ✅
```java
// Retorna el año de esa temporada
return season.getYear();  // Ej: 2025
```

### 2. Si NO hay temporada con `current: true` ⚠️
```java
// Toma la última temporada de la lista
LeagueInfoData.Season lastSeason = leagueInfo.getSeasons().getLast();
log.warn("No hay temporada 'current', usando la última: {}", lastSeason.getYear());
return lastSeason.getYear();
```

### 3. Si NO hay temporadas en la respuesta ❌
```java
// Lanza excepción
throw new RuntimeException("No se encontró temporada actual para la liga " + leagueId);
```

---

## 📝 LOGS DEL SISTEMA

### Logs normales (todo OK):
```
INFO [ApiFootballService] : Obteniendo temporada actual de la liga 140
INFO [ApiFootballService] : Temporada actual encontrada: 2025
INFO [ApiFootballService] : Obteniendo partidos programados de la liga 140 temporada 2025
INFO [ApiFootballService] : Encontrados 15 partidos programados para la temporada 2025
```

### Logs con fallback (no hay current):
```
INFO [ApiFootballService] : Obteniendo temporada actual de la liga 140
WARN [ApiFootballService] : No se encontró temporada marcada como 'current', usando la última: 2025
INFO [ApiFootballService] : Obteniendo partidos programados de la liga 140 temporada 2025
```

### Logs con error:
```
INFO [ApiFootballService] : Obteniendo temporada actual de la liga 140
ERROR [ApiFootballService] : No se pudo obtener la temporada actual de la liga 140
ERROR [ApiFootballService] : No se encontró temporada actual para la liga 140
```

---

## ✨ VENTAJAS DE ESTA IMPLEMENTACIÓN

### 1. **Mantenibilidad** 🔧
- ✅ No requiere modificaciones anuales
- ✅ El código funciona año tras año automáticamente
- ✅ Menos intervención humana

### 2. **Precisión** 🎯
- ✅ Siempre usa la temporada correcta
- ✅ La API marca qué temporada está activa
- ✅ No hay desfase entre el código y la realidad

### 3. **Escalabilidad** 📈
- ✅ Funciona para cualquier liga (solo cambiar leagueId)
- ✅ Se adapta automáticamente a cambios en la API
- ✅ Preparado para el futuro

### 4. **Robustez** 🛡️
- ✅ Tiene fallback si no hay temporada marcada como actual
- ✅ Maneja errores apropiadamente
- ✅ Logs claros para debugging

---

## 🔍 CASOS DE USO

### Caso 1: Cambio de Temporada
**Situación:** Es agosto 2026 y comienza la temporada 2026-2027

**Antes (hardcoded):**
```java
// ❌ Seguiría mostrando partidos de 2025
Integer season = 2025;  // Hay que cambiar esto manualmente
```

**Ahora (dinámico):**
```java
// ✅ Automáticamente obtiene 2026
Integer season = getCurrentSeason(140);  // Retorna 2026
```

---

### Caso 2: Múltiples Ligas
**Situación:** Quieres soportar Premier League (ID: 39)

**Antes:**
```java
// Tendrías que hardcodear también la temporada de Premier
getScheduledFixtures(39, 2024);  // ❌ Y si cambió a 2025?
```

**Ahora:**
```java
// ✅ Funciona automáticamente para cualquier liga
getScheduledFixtures(39);  // Obtiene temporada actual de Premier
```

---

### Caso 3: Ligas en Hemisferio Sur
**Situación:** Liga argentina empieza en febrero (mitad de año)

**Antes:**
```java
// Confuso: ¿2024 o 2025?
getScheduledFixtures(128, ???);
```

**Ahora:**
```java
// ✅ La API sabe qué temporada está activa
getScheduledFixtures(128);  // Obtiene la correcta automáticamente
```

---

## 🚀 IMPACTO EN EL PROYECTO

### Código Eliminado:
- ❌ Parámetro `season` en múltiples lugares
- ❌ Valores hardcodeados (2024, 2025, etc.)
- ❌ Configuración manual de temporadas

### Código Añadido:
- ✅ Método `getCurrentSeason()` - 45 líneas
- ✅ Modelo `LeagueInfoData.java` - 100 líneas
- ✅ Lógica de fallback robusta

### Llamadas a API:
- **Antes:** 1 llamada por carga de partidos
- **Ahora:** 2 llamadas por carga de partidos (1 para season + 1 para fixtures)
- **Impacto:** Mínimo, se puede cachear la temporada

---

## 💡 OPTIMIZACIONES FUTURAS

### 1. **Caché de Temporada** 📦
```java
// Guardar la temporada en memoria por 24 horas
private Integer cachedSeason;
private LocalDateTime cacheExpiry;

public Integer getCurrentSeason(Integer leagueId) {
    if (cachedSeason != null && LocalDateTime.now().isBefore(cacheExpiry)) {
        return cachedSeason;  // Usar caché
    }
    
    // Obtener de la API y actualizar caché
    cachedSeason = fetchCurrentSeasonFromAPI(leagueId);
    cacheExpiry = LocalDateTime.now().plusHours(24);
    
    return cachedSeason;
}
```

### 2. **Configuración por Liga** ⚙️
```properties
# application.properties
api.football.leagues.laliga.id=140
api.football.leagues.premier.id=39
api.football.cache.season.hours=24
```

### 3. **Endpoint de Info** ℹ️
```java
@GetMapping("/api/leagues/{leagueId}/current-season")
public ResponseEntity<?> getCurrentSeasonInfo(@PathVariable Integer leagueId) {
    Integer season = apiFootballService.getCurrentSeason(leagueId);
    return ResponseEntity.ok(Map.of("season", season));
}
```

---

## 🎯 RESUMEN

| Aspecto | Antes | Ahora |
|---------|-------|-------|
| **Temporada** | Hardcoded (2024) | Dinámica (API) |
| **Mantenimiento** | Manual anual | Automático |
| **Escalabilidad** | 1 liga | N ligas |
| **Precisión** | Puede desfasar | Siempre correcta |
| **Llamadas API** | 1 | 2 (optimizable) |

---

## ✅ CHECKLIST DE VERIFICACIÓN

- [x] Modelo `LeagueInfoData.java` creado
- [x] Método `getCurrentSeason()` implementado
- [x] Método `getScheduledFixtures()` actualizado
- [x] Controlador actualizado (sin parámetro season)
- [x] Frontend actualizado (sin parámetro season)
- [x] Logs informativos añadidos
- [x] Fallback implementado
- [x] Manejo de errores robusto

---

**¡El sistema ahora obtiene la temporada dinámicamente y funcionará año tras año sin modificaciones! 🎉**
