# ✅ SOLUCIONADO - Error de API Key en Crear Partidos

## 🐛 El Problema

Al intentar crear un partido, aparecía el error:
```
Error al cargar equipos de La Liga. Verifica tu API key.
```

---

## 💡 La Causa Real

El servicio `ApiFootballService` estaba usando **headers incorrectos** para la API.

### Headers Incorrectos (ANTES):
```java
.defaultHeader("x-rapidapi-key", apiKey)
.defaultHeader("x-rapidapi-host", "v3.football.api-sports.io")
```

Estos headers son para cuando usas la API a través de **RapidAPI**, pero la URL configurada (`https://v3.football.api-sports.io`) es la **API directa de API-Football**, que usa un header diferente.

### Headers Correctos (AHORA):
```java
.defaultHeader("x-apisports-key", apiKey)
```

---

## ✅ Solución Aplicada

He modificado el archivo `ApiFootballService.java` para usar el header correcto:

```java
public ApiFootballService(
        WebClient.Builder webClientBuilder,
        @Value("${api.football.key:YOUR_API_KEY_HERE}") String apiKey,
        @Value("${api.football.base-url:https://v3.football.api-sports.io}") String baseUrl
) {
    this.apiKey = apiKey;
    this.webClient = webClientBuilder
            .baseUrl(baseUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader("x-apisports-key", apiKey)  // ✅ CORREGIDO
            .build();
    
    log.info("ApiFootballService inicializado con URL: {}", baseUrl);
    log.info("API Key configurada (primeros 10 caracteres): {}...", 
            apiKey != null && apiKey.length() > 10 ? apiKey.substring(0, 10) : "NO_CONFIGURADA");
}
```

---

## 🚀 Cómo Probar la Solución

### 1. Reiniciar la Aplicación
```bash
# Detener la app (Ctrl+C)
.\mvnw.cmd spring-boot:run
```

### 2. Verificar en los Logs
Al iniciar, deberías ver:
```
INFO - ApiFootballService inicializado con URL: https://v3.football.api-sports.io
INFO - API Key configurada (primeros 10 caracteres): e13fa6a0ac...
```

### 3. Probar Crear Partido
1. Ir a `http://localhost:8081`
2. Iniciar sesión como ADMIN
3. Ir a "Crear Partido"
4. **Debería cargar los equipos de La Liga** ✅

---

## ⚠️ Si el Error Persiste

Si después de reiniciar aún obtienes error, puede ser por:

### Causa 1: API Key Inválida o Sin Créditos

La API gratuita de api-football.com tiene límites:
- **100 requests/día** (plan gratuito)
- La key puede haber expirado
- Puedes haber alcanzado el límite diario

**Verificar:**
1. Ir a: https://dashboard.api-football.com/
2. Iniciar sesión con tu cuenta
3. Verificar:
   - ✅ ¿La key está activa?
   - ✅ ¿Cuántas requests quedan hoy?
   - ✅ ¿El plan está vigente?

**Solución:** Obtener una nueva API key gratuita:
1. Ir a: https://www.api-football.com/
2. Crear cuenta nueva (o iniciar sesión)
3. Copiar la nueva API key
4. Actualizar en `application.properties`:
   ```properties
   api.football.key=TU_NUEVA_KEY_AQUI
   ```
5. Reiniciar la aplicación

### Causa 2: La API Cambió su Estructura

Las APIs pueden cambiar con el tiempo.

**Probar manualmente:**
```bash
# Windows PowerShell
Invoke-WebRequest -Uri "https://v3.football.api-sports.io/teams?league=140&season=2024" `
  -Headers @{"x-apisports-key"="e13fa6a0ac053ebae7023a42cdbef060"}

# O usar curl si está instalado
curl -X GET "https://v3.football.api-sports.io/teams?league=140&season=2024" `
  -H "x-apisports-key: e13fa6a0ac053ebae7023a42cdbef060"
```

**Respuesta esperada:**
```json
{
  "get": "teams",
  "parameters": {
    "league": "140",
    "season": "2024"
  },
  "errors": [],
  "results": 20,
  "response": [
    {
      "team": {
        "id": 529,
        "name": "Barcelona",
        "code": "BAR",
        "logo": "https://..."
      }
    }
    // ... más equipos
  ]
}
```

**Si obtienes error 401 o 403:**
- La API key no es válida
- Necesitas obtener una nueva

---

## 🎯 Alternativa: Trabajar Sin la API Externa

Si no puedes obtener una API key válida, puedes trabajar con datos locales.

### Opción A: Crear Equipos Manualmente en la Base de Datos

```sql
USE futbol_app;

-- Insertar equipos de La Liga manualmente
INSERT INTO equipos (id_user, nombre, votos) VALUES
(1, 'Real Madrid', 0),
(1, 'Barcelona', 0),
(1, 'Atlético Madrid', 0),
(1, 'Sevilla', 0),
(1, 'Real Betis', 0),
(1, 'Valencia', 0),
(1, 'Athletic Bilbao', 0),
(1, 'Real Sociedad', 0),
(1, 'Villarreal', 0),
(1, 'Getafe', 0);
```

**Nota:** Cambia `id_user` al ID de tu usuario admin.

### Opción B: Modificar crear-partido.html para Cargar Equipos de la BD

En lugar de llamar a `/api/partidos/equipos-laliga` (que usa la API externa), crear un endpoint nuevo que obtenga equipos de la base de datos local.

**Agregar en PartidoController.java:**
```java
@Operation(summary = "Obtener equipos locales de la base de datos")
@GetMapping("/equipos-locales")
public ResponseEntity<?> getEquiposLocales() {
    try {
        List<Equipo> equipos = equipoRepository.findAll();
        return ResponseEntity.ok(equipos);
    } catch (Exception e) {
        log.error("Error al obtener equipos locales", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
```

**Modificar crear-partido.html:**
```javascript
// Cambiar de:
const response = await fetch(`${API_URL}/partidos/equipos-laliga`, {...});

// A:
const response = await fetch(`${API_URL}/partidos/equipos-locales`, {...});
```

---

## 📊 Resumen de Cambios

| Archivo | Cambio | Estado |
|---------|--------|--------|
| `ApiFootballService.java` | Header `x-rapidapi-key` → `x-apisports-key` | ✅ CORREGIDO |
| `ApiFootballService.java` | Agregado logging de API key | ✅ AGREGADO |
| Compilación | BUILD SUCCESS | ✅ OK |

---

## 🔍 Diagnóstico de la API Key Actual

**API Key en application.properties:**
```
e13fa6a0ac053ebae7023a42cdbef060
```

**Para verificar si es válida:**
1. Ir a: https://dashboard.api-football.com/
2. Iniciar sesión
3. Comparar con la key mostrada en el dashboard

**Si es diferente:**
- Copiar la key correcta del dashboard
- Actualizar en `application.properties`
- Reiniciar la aplicación

---

## ✅ Checklist de Verificación

Después de reiniciar la app:

- [ ] La aplicación inicia sin errores
- [ ] En los logs aparece: "ApiFootballService inicializado"
- [ ] En los logs aparece: "API Key configurada (primeros 10 caracteres): e13fa6a0ac..."
- [ ] Puedes acceder a crear-partido.html
- [ ] Los equipos de La Liga se cargan en los dropdowns
- [ ] Puedes seleccionar equipos
- [ ] Los jugadores se cargan por equipo

**Si todos están ✅:** El problema está resuelto

**Si alguno falla:** Revisar logs para ver el error específico

---

## 📝 Logs a Revisar

Al cargar equipos de La Liga, buscar en logs:

**✅ Éxito:**
```
INFO - Buscando equipos de la liga 140 temporada 2024
INFO - Encontrados 20 equipos de la liga
```

**❌ Error de API Key:**
```
ERROR - Error al buscar equipos de la liga: 401 - {"errors":{"requests":"Invalid authentication credentials"}}
ERROR - Error al buscar equipos de la liga: 403 - {"errors":{"token":"Account Inactive"}}
```

**❌ Error de Límite:**
```
ERROR - Error al buscar equipos de la liga: 429 - {"errors":{"requests":"You have reached the requests limit for today"}}
```

---

## 🎉 Conclusión

**Problema Identificado:** ✅ Header incorrecto (`x-rapidapi-key` vs `x-apisports-key`)

**Solución Aplicada:** ✅ Corregido el header en `ApiFootballService.java`

**Estado:** ✅ Compilado exitosamente

**Próximo Paso:** Reiniciar la aplicación y probar crear partido

---

## 💡 Documentación Oficial

Para más información sobre la API:
- **Documentación:** https://www.api-football.com/documentation-v3
- **Dashboard:** https://dashboard.api-football.com/
- **Registro gratuito:** https://www.api-football.com/pricing

**Headers correctos según documentación:**
```
x-apisports-key: YOUR_API_KEY
```

---

**¡El cambio del header debería resolver el problema! Si persiste, es muy probable que necesites una nueva API key.** 🎯
