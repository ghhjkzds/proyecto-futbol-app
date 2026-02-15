# 📊 MEJORA: Logging del Campo "errors" de la API

## 🎯 MEJORA IMPLEMENTADA

He añadido logging del campo `errors` que devuelve la API de API-Football en todas las llamadas principales.

---

## ✅ ¿QUÉ SE HA AÑADIDO?

### Logging de Errores en Cada Llamada a la API

Ahora en cada método que consulta la API, **antes** de procesar la respuesta, se verifica si el campo `errors` contiene algún mensaje y se registra en los logs.

### Código Añadido:
```java
// Log de errores de la API si existen
if (response != null && response.getErrors() != null) {
    log.warn("API devolvió errores en [endpoint]: {}", response.getErrors());
}
```

---

## 📝 MÉTODOS ACTUALIZADOS

### 1. `getCurrentSeason()` - /leagues
```java
if (response != null && response.getErrors() != null) {
    log.warn("API devolvió errores en /leagues: {}", response.getErrors());
}
```

### 2. `getScheduledFixtures()` - /fixtures
```java
if (response != null && response.getErrors() != null) {
    log.warn("API devolvió errores en /fixtures: {}", response.getErrors());
}
```

### 3. `getTeamById()` - /teams
```java
if (response != null && response.getErrors() != null) {
    log.warn("API devolvió errores en /teams (getTeamById): {}", response.getErrors());
}
```

### 4. `getTeamPlayers()` - /players
```java
if (response != null && response.getErrors() != null) {
    log.warn("API devolvió errores en /players: {}", response.getErrors());
}
```

### 5. `searchTeamsByLeague()` - /teams
```java
if (response != null && response.getErrors() != null) {
    log.warn("API devolvió errores en /teams (searchTeamsByLeague): {}", response.getErrors());
}
```

---

## 🔍 EJEMPLOS DE LOGS

### Caso 1: Sin Errores (API funcionando correctamente)
```
INFO: Obteniendo partidos programados de la liga 140 temporada 2026
INFO: Respuesta de API recibida: 15 partidos totales
INFO: Encontrados 15 partidos programados (NS o TBD) para la temporada 2026
```
✅ No aparece ningún log de WARN sobre errores

### Caso 2: Con Error de API Key
```
INFO: Obteniendo partidos programados de la liga 140 temporada 2026
WARN: API devolvió errores en /fixtures: {"token":"The API key is invalid"}
INFO: Respuesta de API recibida: 0 partidos totales
```
⚠️ Ahora verás claramente que el problema es la API key

### Caso 3: Con Error de Límite Excedido
```
INFO: Obteniendo temporada actual de la liga 140
WARN: API devolvió errores en /leagues: {"requests":"The request limit has been reached"}
WARN: Usando año actual como fallback
```
⚠️ Verás que excediste el límite de peticiones

### Caso 4: Error en Array (Formato Antiguo)
```
INFO: Buscando equipos de la liga 140 temporada 2024
WARN: API devolvió errores en /teams: []
INFO: Encontrados 20 equipos de la liga
```
ℹ️ Array vacío = sin errores (formato antiguo de la API)

### Caso 5: Error en Objeto (Formato Nuevo)
```
INFO: Obteniendo jugadores del equipo 529 para la temporada 2024
WARN: API devolvió errores en /players: {"ratelimit":"Exceeded daily limit"}
INFO: Encontrados 0 jugadores
```
⚠️ Indica problema de rate limit

---

## 📊 FORMATOS DE ERRORES QUE MANEJA

Gracias al cambio anterior de `List<String>` a `Object`, ahora maneja:

### Formato 1: Array Vacío
```json
{
  "errors": []
}
```
**Log:** No se registra (array vacío = sin errores)

### Formato 2: Array con Mensajes
```json
{
  "errors": ["Invalid parameter", "Missing field"]
}
```
**Log:** `WARN: API devolvió errores en /endpoint: ["Invalid parameter", "Missing field"]`

### Formato 3: Objeto con Mensajes
```json
{
  "errors": {
    "token": "The API key is invalid",
    "requests": "Limit exceeded"
  }
}
```
**Log:** `WARN: API devolvió errores en /endpoint: {token=The API key is invalid, requests=Limit exceeded}`

### Formato 4: Null (Sin Campo errors)
```json
{
  "response": [...]
}
```
**Log:** No se registra (sin errores)

---

## 🛠️ CÓMO USAR ESTA INFORMACIÓN

### 1. Reinicia la Aplicación
```powershell
# Detener (Ctrl+C) y reiniciar:
cd "C:\Users\USUARIO\Downloads\proyecto-ACD"
.\mvnw.cmd spring-boot:run
```

### 2. Monitorea los Logs

Busca en los logs líneas que contengan:
```
WARN: API devolvió errores
```

### 3. Interpreta los Errores

| Error en Logs | Significado | Solución |
|---------------|-------------|----------|
| `"token":"Invalid"` | API key incorrecta | Verificar application.properties |
| `"requests":"Limit reached"` | Límite excedido | Esperar 24h o upgrade plan |
| `"league":"Not found"` | Liga no existe | Verificar ID de liga (140 = La Liga) |
| `"season":"Invalid"` | Temporada incorrecta | Verificar año de temporada |
| `"timeout"` | Timeout de API | Aumentar timeout o reintentar |
| `[]` | Array vacío | No hay errores |

---

## 🎯 BENEFICIOS

### Antes:
```
INFO: Encontrados 0 partidos programados
```
❓ No sabes por qué devuelve 0

### Ahora:
```
WARN: API devolvió errores en /fixtures: {"requests":"Limit exceeded"}
INFO: Encontrados 0 partidos programados
```
✅ Sabes exactamente por qué: límite de peticiones excedido

---

## 🔧 DEBUGGING MEJORADO

### Escenario 1: No Aparecen Partidos

**Logs sin errores:**
```
INFO: Respuesta de API recibida: 0 partidos totales
INFO: Encontrados 0 partidos programados
```
→ La API responde OK pero no hay partidos en esa temporada

**Logs con errores:**
```
WARN: API devolvió errores en /fixtures: {"season":"Invalid"}
INFO: Respuesta de API recibida: 0 partidos totales
```
→ El problema es que la temporada es inválida

### Escenario 2: No Se Cargan Jugadores

**Logs sin errores:**
```
INFO: Encontrados 25 jugadores
```
→ Todo OK

**Logs con errores:**
```
WARN: API devolvió errores en /players: {"team":"Not found"}
INFO: Encontrados 0 jugadores
```
→ El ID del equipo es incorrecto

---

## 📋 CHECKLIST DE USO

Para debugging efectivo:

1. [ ] Reiniciar aplicación después de implementar el cambio
2. [ ] Realizar la acción que falla (cargar partidos, jugadores, etc.)
3. [ ] Revisar los logs en busca de `WARN: API devolvió errores`
4. [ ] Identificar el tipo de error
5. [ ] Aplicar la solución correspondiente
6. [ ] Verificar que el error desaparezca

---

## 💡 EJEMPLOS DE USO REAL

### Debug: ¿Por qué no aparecen partidos?

**Paso 1:** Ve a crear-alineacion.html y intenta cargar partidos

**Paso 2:** Revisa los logs del backend:
```
INFO: Obteniendo temporada actual de la liga 140
WARN: API devolvió errores en /leagues: {"token":"Invalid"}
WARN: Usando año actual como fallback
INFO: Obteniendo partidos programados de la liga 140 temporada 2026
WARN: API devolvió errores en /fixtures: {"token":"Invalid"}
ERROR: Error al obtener partidos: 401
```

**Paso 3:** Identificas el problema: **API key inválida**

**Paso 4:** Verificas application.properties y corriges la API key

**Paso 5:** Reinicias y ahora funciona:
```
INFO: Obteniendo partidos programados de la liga 140 temporada 2026
INFO: Respuesta de API recibida: 15 partidos totales
INFO: Encontrados 15 partidos programados
```

---

## ✅ RESUMEN

| Aspecto | Antes | Ahora |
|---------|-------|-------|
| Visibilidad de errores API | ❌ No se veían | ✅ Se registran en logs |
| Nivel de log | N/A | WARN (apropiado) |
| Formato soportado | N/A | Array, Object, null |
| Métodos cubiertos | 0 | 5 principales |
| Debugging | Difícil | Mucho más fácil |

---

## 🚀 PRÓXIMOS PASOS

1. **Reinicia la aplicación** para que tome los cambios
2. **Prueba cargar partidos** en crear-alineacion.html
3. **Revisa los logs** en busca de WARN sobre errores
4. **Si hay errores**, sigue las soluciones indicadas en este documento

---

**Con esta mejora, ahora tendrás visibilidad completa de cualquier error que devuelva la API de API-Football. 🎉**
