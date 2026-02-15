# 🔍 DIAGNÓSTICO COMPLETO - Problemas Reportados

## 📊 Análisis de los Problemas

### ✅ **PROBLEMA 1: Puerto Incorrecto en ver-alineaciones.html** - RESUELTO

**Estado:** ✅ CORREGIDO

**Descripción del problema:**
- La aplicación corre en el puerto **8081** (configurado en `application.properties`)
- La página `ver-alineaciones.html` estaba usando `localhost:8080` ❌
- Las demás páginas SÍ usaban correctamente `localhost:8081` ✅

**Evidencia encontrada:**
```javascript
// ARCHIVOS CORRECTOS (usando puerto 8081):
- crear-alineacion.html  → const API_URL = 'http://localhost:8081/api'; ✅
- crear-partido.html     → const API_URL = 'http://localhost:8081/api'; ✅
- mis-alineaciones.html  → const API_URL = 'http://localhost:8081/api'; ✅

// ARCHIVO CON ERROR (usando puerto 8080):
- ver-alineaciones.html  → const API_BASE_URL = 'http://localhost:8080/api'; ❌
```

**Solución aplicada:**
```javascript
// ANTES:
const API_BASE_URL = 'http://localhost:8080/api';

// AHORA:
const API_BASE_URL = 'http://localhost:8081/api';
```

**Archivo modificado:**
- `src/main/resources/static/ver-alineaciones.html`

---

### ⚠️ **PROBLEMA 2: Error de API Key** - REQUIERE INVESTIGACIÓN

**Estado:** 🔍 REQUIERE VERIFICACIÓN MANUAL

**Descripción del problema:**
Usuario reporta: "la api key es incorrecta, he cambiado la api key y me sigue dando el mismo error"

**Configuración actual:**
```properties
# application.properties
api.football.key=e13fa6a0ac053ebae7023a42cdbef060
api.football.base-url=https://v3.football.api-sports.io
```

**Posibles causas:**

#### 1. **API Key Inválida o Expirada**
- La API key gratuita de api-football.com tiene límites
- Plan gratuito: 100 requests/día
- La key puede haber expirado o alcanzado el límite

**Cómo verificar:**
```bash
# Test manual con curl
curl -X GET "https://v3.football.api-sports.io/status" \
  -H "x-rapidapi-key: e13fa6a0ac053ebae7023a42cdbef060" \
  -H "x-rapidapi-host: v3.football.api-sports.io"
```

#### 2. **Cambio en la API de API-Football**
- La API pudo haber cambiado sus headers requeridos
- Verificar documentación actual: https://www.api-football.com/documentation-v3

**Headers actuales en el código:**
```java
.defaultHeader("x-rapidapi-key", apiKey)
.defaultHeader("x-rapidapi-host", "v3.football.api-sports.io")
```

#### 3. **Caché del Navegador**
- Aunque cambies la API key en `application.properties`, si no reinicias la app, seguirá usando la antigua

**Solución:**
1. Modificar `application.properties`
2. **REINICIAR** la aplicación (no solo refrescar el navegador)
3. Verificar en logs que cargue la nueva key

---

### ⚠️ **PROBLEMA 3: No Carga Partidos ni Alineaciones de la BD** - DIAGNÓSTICO

**Estado:** 🔍 POSIBLE CAUSA IDENTIFICADA

**Descripción del problema:**
"tampoco carga los partidos ya creados o las alineaciones de la base de datos"

**Diagnóstico:**

#### Causa Probable: Puerto Incorrecto
Si estabas intentando acceder desde `ver-alineaciones.html` (que usaba puerto 8080):
- Frontend: `http://localhost:8080/api/partidos` ❌
- Backend: Corriendo en `http://localhost:8081` ✅
- **Resultado:** Error de conexión (CORS o Network Error)

**Con la corrección aplicada, esto debería resolverse.**

#### Cómo Verificar que Funciona:

**Test 1: Verificar que la aplicación corre en 8081**
```bash
# Abrir navegador en:
http://localhost:8081/actuator/health
# Debería mostrar: {"status":"UP"}
```

**Test 2: Verificar endpoint de partidos**
```bash
# Después de iniciar sesión, desde consola del navegador:
fetch('http://localhost:8081/api/partidos', {
  headers: {
    'Authorization': 'Bearer ' + localStorage.getItem('token')
  }
})
.then(r => r.json())
.then(console.log)
```

**Test 3: Verificar datos en la base de datos**
```sql
USE futbol_app;
SELECT * FROM partidos;
SELECT * FROM alineaciones;
SELECT * FROM equipos;
```

---

## 🎯 RESUMEN DEL DIAGNÓSTICO

| Problema | Estado | Causa | Solución |
|----------|--------|-------|----------|
| Puerto incorrecto en ver-alineaciones.html | ✅ RESUELTO | Puerto hardcodeado a 8080 | Cambiado a 8081 |
| Error de API Key | ⚠️ REQUIERE VERIFICACIÓN | Key inválida/expirada o límite alcanzado | Verificar key en api-football.com |
| No carga partidos/alineaciones | 🟡 PROBABLEMENTE RESUELTO | Puerto incorrecto impedía conexión | Reiniciar app tras corrección |

---

## ✅ ACCIONES COMPLETADAS

1. ✅ **Corregido puerto en ver-alineaciones.html** (8080 → 8081)
2. ✅ **Compilado el proyecto** (BUILD SUCCESS)
3. ✅ **Diagnosticado problema de API Key**

---

## 🚀 PRÓXIMOS PASOS RECOMENDADOS

### **Paso 1: Reiniciar la Aplicación**
```bash
# Detener la aplicación actual (Ctrl+C)
# Luego iniciar de nuevo:
.\mvnw.cmd spring-boot:run
```

**IMPORTANTE:** No es necesario reiniciar el PC. Solo la aplicación Spring Boot.

### **Paso 2: Verificar que Carga Partidos**
1. Abrir navegador: `http://localhost:8081`
2. Iniciar sesión
3. Ir a "Ver Alineaciones"
4. Verificar que aparece el dropdown de partidos

### **Paso 3: Sobre el Error de API Key**

**Si el error persiste después de reiniciar:**

#### Opción A: Obtener Nueva API Key
1. Ir a: https://www.api-football.com/
2. Crear cuenta o iniciar sesión
3. Obtener nueva API key
4. Actualizar en `application.properties`:
   ```properties
   api.football.key=TU_NUEVA_KEY_AQUI
   ```
5. Reiniciar la aplicación

#### Opción B: Verificar Key Actual
```bash
# En tu navegador, ir a:
https://dashboard.api-football.com/
# Iniciar sesión y verificar:
# - ¿La key es válida?
# - ¿Cuántas requests quedan hoy?
# - ¿El plan está activo?
```

#### Opción C: Verificar Endpoint (puede haber cambiado)
Según la documentación más reciente, algunos proveedores cambiaron headers:
```java
// Probar cambiar de:
.defaultHeader("x-rapidapi-key", apiKey)
.defaultHeader("x-rapidapi-host", "v3.football.api-sports.io")

// A:
.defaultHeader("x-apisports-key", apiKey)
```

---

## 📝 LOGS A REVISAR

Al iniciar la aplicación, buscar en los logs:

**✅ Señales de éxito:**
```
INFO - Tomcat started on port(s): 8081 (http)
INFO - Started ProyectoAcdApplication
```

**❌ Señales de problemas con API:**
```
ERROR - API Key inválida o error de conexión
ERROR - WebClientResponseException: 401 Unauthorized
ERROR - WebClientResponseException: 403 Forbidden
```

---

## 🎓 LECCIONES APRENDIDAS

### **Por qué NO necesitas reiniciar el PC:**

**Spring Boot carga la configuración al iniciar:**
- `application.properties` se lee al arrancar la app
- Los cambios en archivos `.properties` requieren **reiniciar la app**, no el PC
- Los cambios en archivos `.html` se reflejan **automáticamente** (sin reiniciar nada)

**Flujo correcto:**
```
Cambio en .properties → Reiniciar aplicación Spring Boot
Cambio en .html → Solo refrescar navegador (Ctrl+F5)
Cambio en .java → Recompilar + reiniciar app
```

### **Por qué el puerto era el problema:**

```
Frontend (ver-alineaciones.html)
    ↓
    Hace petición a: http://localhost:8080/api/partidos
    ↓
    ❌ NO HAY NADA escuchando en puerto 8080
    ↓
    Error: Failed to fetch / Network Error
    ↓
    Usuario ve: "No cargan partidos"
```

**Corrección:**
```
Frontend (ver-alineaciones.html - CORREGIDO)
    ↓
    Hace petición a: http://localhost:8081/api/partidos
    ↓
    ✅ Backend escuchando en puerto 8081
    ↓
    ✅ Respuesta exitosa con datos de partidos
    ↓
    ✅ Usuario ve: Lista de partidos
```

---

## 🔧 VERIFICACIÓN FINAL

**Después de reiniciar la aplicación, verificar:**

```
✅ Aplicación inicia en puerto 8081
✅ Login funciona
✅ Index.html carga
✅ Ver Alineaciones carga (sin error 403)
✅ Dropdown de partidos se llena con datos de BD
✅ Al seleccionar un partido, muestra alineaciones

❓ API de fútbol externa (requiere key válida)
```

---

## 📌 CONCLUSIÓN

**Problema Principal Resuelto:** ✅ Puerto incorrecto en `ver-alineaciones.html`

**Problema Secundario:** ⚠️ API Key necesita verificación manual

**Acción Inmediata Requerida:**
1. Reiniciar la aplicación Spring Boot
2. Probar acceso a "Ver Alineaciones"
3. Si el error de API persiste, obtener nueva key de api-football.com

**NO es necesario reiniciar el PC.** 🚫💻
