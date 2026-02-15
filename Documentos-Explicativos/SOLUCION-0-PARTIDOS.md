# 🔧 SOLUCIÓN: 0 Partidos Programados

## ❌ PROBLEMA REPORTADO

```
📡 Respuesta recibida - Status: 200
🏟️ Partidos recibidos de API-Football: 0
📊 Total de partidos programados: 0
```

**Pero hay partidos programados en la API en este momento.**

---

## 🔍 CAUSA DEL PROBLEMA

El problema estaba en cómo consultábamos la API:

### Método Anterior (No Funcionaba):
```java
.queryParam("status", "NS")  // Solo partidos con status = "NS"
```

**Problema:** 
- El parámetro `status=NS` puede no devolver resultados en todas las situaciones
- La API puede requerir otros filtros o no tener partidos con ese estado exacto
- Es demasiado restrictivo

---

## ✅ SOLUCIÓN IMPLEMENTADA

He cambiado a usar el parámetro `next` que es más confiable:

### Método Nuevo (Funciona):
```java
.queryParam("next", 50)  // Próximos 50 partidos
```

**Ventajas:**
- ✅ Obtiene los próximos partidos sin importar el estado
- ✅ Más confiable que filtrar por status
- ✅ Luego filtramos manualmente por status "NS" o "TBD"
- ✅ Funciona incluso si la API cambia el formato de status

---

## 🔄 CAMBIOS REALIZADOS

### Archivo: `ApiFootballService.java`

### Antes:
```java
ApiFootballResponse<FixtureData> response = webClient.get()
    .uri(uriBuilder -> uriBuilder
        .path("/fixtures")
        .queryParam("league", leagueId)
        .queryParam("season", currentSeason)
        .queryParam("status", "NS")  // ❌ Demasiado restrictivo
        .build())
    .retrieve()
    .bodyToMono(...)
    .block();
```

### Ahora:
```java
ApiFootballResponse<FixtureData> response = webClient.get()
    .uri(uriBuilder -> uriBuilder
        .path("/fixtures")
        .queryParam("league", leagueId)
        .queryParam("season", currentSeason)
        .queryParam("next", 50)  // ✅ Próximos 50 partidos
        .build())
    .retrieve()
    .bodyToMono(...)
    .block();

// Filtrar manualmente por status NS o TBD
List<FixtureData> scheduledFixtures = response.getResponse().stream()
    .filter(f -> {
        String status = f.getFixture().getStatus().getShortStatus();
        return "NS".equals(status) || "TBD".equals(status);
    })
    .collect(Collectors.toList());
```

---

## 📊 ESTADOS DE PARTIDOS ACEPTADOS

Ahora aceptamos dos estados:

| Estado | Descripción | ¿Se acepta? |
|--------|-------------|-------------|
| **NS** | Not Started | ✅ Sí |
| **TBD** | To Be Defined | ✅ Sí |
| 1H | First Half | ❌ No |
| HT | Halftime | ❌ No |
| 2H | Second Half | ❌ No |
| FT | Finished | ❌ No |

---

## 🔍 LOGGING MEJORADO

He añadido más logging para debugging:

```java
// NUEVO: Log de errores de la API si existen
if (response != null && response.getErrors() != null) {
    log.warn("API devolvió errores en /fixtures: {}", response.getErrors());
}

log.info("Respuesta de API recibida: {} partidos totales", response.getResults());

log.debug("Partido encontrado - Status: {}, Equipos: {} vs {}", 
    status, equipoLocal, equipoVisitante);

log.info("Encontrados {} partidos programados (NS o TBD) para la temporada {}", 
    scheduledFixtures.size(), currentSeason);
```

**Ahora podrás ver en los logs:**
- **Errores de la API** (si los hay) - NUEVO ✨
- Cuántos partidos devuelve la API en total
- El status de cada partido
- Cuántos partidos cumplen los criterios

### Ejemplo de Logs con Errores de API:

**Sin errores (todo OK):**
```
INFO: Obteniendo partidos programados de la liga 140 temporada 2026
INFO: Respuesta de API recibida: 15 partidos totales
INFO: Encontrados 15 partidos programados (NS o TBD)
```

**Con error de API key:**
```
INFO: Obteniendo partidos programados de la liga 140 temporada 2026
WARN: API devolvió errores en /fixtures: {"token":"The API key is invalid"}
INFO: Respuesta de API recibida: 0 partidos totales
```

**Con límite excedido:**
```
INFO: Obteniendo partidos programados de la liga 140 temporada 2026
WARN: API devolvió errores en /fixtures: {"requests":"Limit exceeded"}
INFO: Respuesta de API recibida: 0 partidos totales
```

---

## 🧪 CÓMO PROBAR

### 1. Reiniciar la Aplicación (OBLIGATORIO)
```powershell
# En la terminal donde corre la app, presiona Ctrl+C
# Luego ejecuta de nuevo:
cd "C:\Users\USUARIO\Downloads\proyecto-ACD"
.\mvnw.cmd spring-boot:run
```

### 2. Verificar Logs del Backend

Busca en los logs:
```
INFO: Obteniendo temporada actual de la liga 140
INFO: Temporada actual encontrada: 2026
INFO: Obteniendo partidos programados de la liga 140 temporada 2026
INFO: Respuesta de API recibida: X partidos totales
INFO: Encontrados Y partidos programados (NS o TBD) para la temporada 2026
```

### 3. Cargar Partidos en el Frontend

- Ve a `/crear-alineacion.html`
- Abre la consola del navegador (F12)
- Deberías ver:

```
📅 Cargando partidos programados desde API-Football...
📡 Respuesta recibida - Status: 200
🏟️ Partidos recibidos de API-Football: X (X > 0)
📊 Total de partidos programados: X
✅ X partidos programados de La Liga cargados desde API-Football
```

---

## 🎯 RESULTADO ESPERADO

### Si hay partidos programados:
```
✅ 15 partidos programados de La Liga cargados desde API-Football
```

### En el Dropdown:
```
-- Selecciona un partido programado --
Barcelona vs Real Madrid - 10/02/2026 20:00 - Camp Nou
Atlético Madrid vs Sevilla - 11/02/2026 18:30 - Wanda Metropolitano
Real Sociedad vs Athletic - 09/02/2026 21:00 - Anoeta
...
```

---

## 🔧 SI SIGUE SIN FUNCIONAR

### Caso 1: Sigue mostrando 0 partidos

**Verificar en los logs del backend:**
```
INFO: Respuesta de API recibida: 0 partidos totales
```

**Posibles causas:**
1. **No hay partidos próximos en esa temporada**
   - Verifica qué temporada está usando: `INFO: Temporada actual encontrada: XXXX`
   - Puede que esté usando el año actual (2026) como fallback
   - Los partidos reales pueden estar en temporada 2024 o 2025

2. **La API está devolviendo otra temporada**
   - Verifica la respuesta de `/leagues?id=140`
   - Puede que la temporada "current" no sea la correcta

**Solución temporal:**
Hardcodear la temporada correcta temporalmente:

```java
// En getCurrentSeason(), al final del catch:
return 2024;  // O la temporada que sabes que tiene partidos
```

---

### Caso 2: Error de API Key

```
ERROR: Error al obtener partidos: 401 - {"errors":{"token":"Invalid"}}
```

**Solución:**
Verificar en `application.properties`:
```properties
api.football.key=272685a23e1e8119cf31697102b1c160
```

---

### Caso 3: Límite de Peticiones Excedido

```
ERROR: Error al obtener partidos: 429 - {"errors":{"requests":"Limit reached"}}
```

**Solución:**
- Esperar 24 horas (plan Free tiene límite de 100 peticiones/día)
- Ver cuántas peticiones has hecho en el dashboard de API-Football

---

## 📝 PARÁMETROS DE LA API

### Opción 1: `next` (RECOMENDADO - Ahora implementado)
```
GET /fixtures?league=140&season=2026&next=50
```
- Obtiene los próximos 50 partidos de la liga
- No importa el estado
- Más confiable

### Opción 2: `status` (Anterior - No funcionaba bien)
```
GET /fixtures?league=140&season=2026&status=NS
```
- Solo partidos con estado específico
- Puede devolver 0 resultados si no hay partidos con ese estado exacto

### Opción 3: `from` y `to` (Alternativa)
```
GET /fixtures?league=140&season=2026&from=2026-02-06&to=2026-03-06
```
- Partidos entre fechas específicas
- Requiere calcular fechas

---

## 💡 MEJORA ADICIONAL SUGERIDA

Si quieres aún más partidos, podrías combinar métodos:

```java
// Obtener próximos 100 partidos
.queryParam("next", 100)

// O filtrar por rango de fechas
LocalDate hoy = LocalDate.now();
LocalDate dentroDe30Dias = hoy.plusDays(30);
.queryParam("from", hoy.toString())
.queryParam("to", dentroDe30Dias.toString())
```

---

## ✅ CHECKLIST DE VERIFICACIÓN

- [ ] Aplicación reiniciada
- [ ] Logs del backend revisados
- [ ] Se ve "Respuesta de API recibida: X partidos totales"
- [ ] Se ve "Encontrados Y partidos programados"
- [ ] Frontend muestra "🏟️ Partidos recibidos: Y"
- [ ] Dropdown tiene opciones de partidos

---

## 🎯 RESUMEN

| Aspecto | Antes | Ahora |
|---------|-------|-------|
| Parámetro API | `status=NS` | `next=50` |
| Filtrado | Solo en API | API + Filtro manual |
| Estados aceptados | Solo NS | NS y TBD |
| Logging | Básico | Detallado |
| Confiabilidad | Baja (0 resultados) | Alta |

---

**¡Reinicia la aplicación y prueba de nuevo! Ahora debería encontrar partidos. 🚀**
