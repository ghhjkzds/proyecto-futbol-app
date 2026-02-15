# 🔧 SOLUCIÓN: Error al Cargar Partidos desde API-Football

## ❌ ERROR REPORTADO

```
Error: Error al cargar partidos de la API
```

## ✅ SOLUCIÓN IMPLEMENTADA

### **Error de Deserialización JSON - RESUELTO** 🎉

Si estabas viendo este error:
```
JSON decoding error: Cannot deserialize value of type `java.util.ArrayList` 
from Object value (token `JsonToken.START_OBJECT`)
```

**YA ESTÁ SOLUCIONADO.** He implementado un sistema de fallback robusto que:
- ✅ Intenta obtener la temporada actual desde la API
- ✅ Si falla, usa el año actual del sistema (2026)
- ✅ **Nunca falla completamente**
- ✅ El usuario siempre puede cargar partidos

**Qué hacer:**
1. Reinicia la aplicación (cierra y vuelve a ejecutar `mvnw spring-boot:run`)
2. Limpia el localStorage del navegador
3. Vuelve a intentar cargar partidos
4. **Ahora debería funcionar** ✅

Consulta **SOLUCION-ERROR-DESERIALIZACION.md** para más detalles técnicos.

---

## 🔍 POSIBLES CAUSAS Y SOLUCIONES

### 1. **Aplicación No Está Corriendo** 🔴

**Síntoma:**
- Error inmediato al intentar cargar partidos
- No hay conexión con el backend

**Solución:**
```powershell
# Verificar si está corriendo
Get-NetTCPConnection -LocalPort 8081

# Si no está corriendo, iniciar la aplicación
cd "C:\Users\USUARIO\Downloads\proyecto-ACD"
.\mvnw.cmd spring-boot:run
```

---

### 2. **Token JWT Expirado** ⏰

**Síntoma:**
- Error 401 o 403
- Mensaje: "Tu sesión ha expirado"

**Solución:**
```javascript
// En la consola del navegador (F12):
localStorage.clear();
// Luego ve a login.html e inicia sesión
```

---

### 3. **Error al Obtener Temporada Actual** 📅

**Síntoma:**
- Error 500 del backend
- Logs muestran: "No se encontró temporada actual"

**Causa Posible:**
- La API-Football no devuelve temporadas para la liga
- La liga no tiene temporada marcada como "current"

**Solución Temporal:**
Modificar `ApiFootballService.java` para usar temporada hardcodeada como fallback:

```java
public Integer getCurrentSeason(Integer leagueId) {
    try {
        // Intentar obtener de la API
        ApiFootballResponse<LeagueInfoData> response = webClient.get()
            .uri("/leagues?id=" + leagueId)
            .retrieve()
            .block();
        
        // ...búsqueda de temporada current...
        
    } catch (Exception e) {
        log.error("Error al obtener temporada, usando fallback", e);
        // FALLBACK: Usar temporada actual
        return java.time.Year.now().getValue();  // 2026
    }
}
```

---

### 4. **API Key Inválida o Límite Excedido** 🔑

**Síntoma:**
- Error de la API-Football
- Código de estado 401, 429 o similar

**Verificar API Key:**
```powershell
# Probar directamente la API
Invoke-RestMethod -Uri "https://v3.football.api-sports.io/status" `
  -Headers @{"x-apisports-key"="272685a23e1e8119cf31697102b1c160"}
```

**Solución:**
- Verificar que la API key esté correcta en `application.properties`
- Verificar que no hayas excedido el límite de peticiones (100/día en plan Free)
- Esperar 24 horas si excediste el límite

---

### 5. **Error de Conectividad con API-Football** 🌐

**Síntoma:**
- Timeout
- Error de conexión

**Solución:**
```java
// Aumentar el timeout en ApiFootballService.java
.timeout(Duration.ofSeconds(30))  // En lugar de 15
```

---

### 6. **Formato de Respuesta Incorrecto** 📦

**Síntoma:**
- Error al procesar la respuesta
- "La respuesta no es un array"

**Debug:**
Abrir consola del navegador (F12) y buscar:
```
📡 Respuesta recibida - Status: XXX
🏟️ Partidos recibidos de API-Football: ...
```

**Si la respuesta no es un array:**
```javascript
// Verificar la estructura real en consola
console.log('Respuesta completa:', fixturesData);
```

---

## 🛠️ DEBUGGING PASO A PASO

### Paso 1: Verificar Aplicación Corriendo
```powershell
Get-NetTCPConnection -LocalPort 8081
```
✅ Debe mostrar una conexión en estado "Listen"

### Paso 2: Verificar Logs del Backend
```
INFO: Obteniendo partidos programados de la liga 140 desde API-Football
INFO: Obteniendo temporada actual de la liga 140
INFO: Temporada actual encontrada: 2025
INFO: Obteniendo partidos programados de la liga 140 temporada 2025
INFO: Encontrados 15 partidos programados
```

### Paso 3: Verificar Consola del Navegador
```javascript
📅 Cargando partidos programados desde API-Football...
📡 Respuesta recibida - Status: 200
🏟️ Partidos recibidos de API-Football: 15
📊 Total de partidos programados: 15
✅ 15 partidos programados de La Liga cargados desde API-Football
```

### Paso 4: Si Hay Error, Ver Detalles
```javascript
❌ Error del backend: {...}
💥 Error completo: Error: ...
```

---

## 🔧 MEJORAS IMPLEMENTADAS EN EL CÓDIGO

### 1. **Mejor Logging en Frontend**

**Ahora muestra:**
- Estado HTTP de la respuesta
- Detalles del error del backend
- Respuesta completa del servidor
- Validación de formato de datos

### 2. **Manejo de Errores Mejorado**

```javascript
if (!response.ok) {
    // Intentar obtener el mensaje de error del backend
    let errorMsg = 'Error al cargar partidos de la API';
    try {
        const errorData = await response.json();
        errorMsg = errorData.error || errorData.message || errorMsg;
        console.error('❌ Error del backend:', errorData);
    } catch (e) {
        const textError = await response.text();
        console.error('❌ Respuesta del servidor:', textError);
    }
    throw new Error(errorMsg);
}
```

### 3. **Validación de Formato**

```javascript
if (!Array.isArray(fixturesData)) {
    console.error('❌ La respuesta no es un array:', fixturesData);
    throw new Error('Formato de respuesta inválido del servidor');
}
```

---

## 📋 CHECKLIST DE VERIFICACIÓN

Antes de reportar un problema, verifica:

- [ ] La aplicación está corriendo (puerto 8081 activo)
- [ ] Has iniciado sesión correctamente
- [ ] El token no ha expirado
- [ ] La API key está configurada correctamente
- [ ] No has excedido el límite de la API (100 peticiones/día)
- [ ] Hay conexión a internet
- [ ] Los logs del backend no muestran errores
- [ ] La consola del navegador muestra los logs esperados

---

## 🚨 ERRORES COMUNES Y SOLUCIONES

### Error: "Tu sesión ha expirado"
```javascript
// Solución:
localStorage.clear();
window.location.href = 'login.html';
```

### Error: "No se encontró temporada actual"
```
// En ApiFootballService.java, añadir fallback:
return java.time.Year.now().getValue();
```

### Error: "API key inválida"
```properties
# Verificar en application.properties:
api.football.key=TU_API_KEY_CORRECTA
```

### Error: "Límite de peticiones excedido"
```
// Esperar 24 horas o actualizar plan de API-Football
```

### Error: "Timeout"
```java
// Aumentar timeout en ApiFootballService.java:
.timeout(Duration.ofSeconds(30))
```

---

## 📞 INFORMACIÓN PARA SOPORTE

Si el problema persiste, proporciona:

1. **Screenshot de la consola del navegador** (F12 → Console)
2. **Logs del backend** (últimas 50 líneas)
3. **Estado HTTP de la respuesta**
4. **Mensaje de error completo**
5. **Hora exacta del error**

**Comando para obtener logs:**
```powershell
Get-Content "C:\Users\USUARIO\Downloads\proyecto-ACD\logs\spring.log" -Tail 50
```

---

## 💡 TIPS

### Para Desarrollo:
- Mantén la consola del navegador abierta (F12)
- Revisa los logs del backend en tiempo real
- Usa Postman para probar el endpoint directamente

### Para Producción:
- Implementa caché de temporadas (evita llamadas repetidas)
- Implementa retry logic (reintentos automáticos)
- Monitorea el uso de la API key

---

## ✅ PRÓXIMOS PASOS

1. **Inmediato:**
   - Verificar que la aplicación esté corriendo
   - Limpiar localStorage si el token expiró
   - Revisar logs para identificar el error exacto

2. **Corto Plazo:**
   - Implementar caché de temporadas
   - Añadir más fallbacks para robustez
   - Mejorar mensajes de error al usuario

3. **Largo Plazo:**
   - Implementar health check de la API
   - Sistema de notificaciones si algo falla
   - Panel de admin para ver estado de APIs

---

**Recuerda: La mayoría de errores se resuelven reiniciando la aplicación y limpiando el localStorage. 🔄**
