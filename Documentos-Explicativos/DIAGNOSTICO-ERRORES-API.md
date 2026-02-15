# 🔍 DIAGNÓSTICO DE ERRORES - Proyecto ACD

## 📅 Fecha del Diagnóstico: 6 de Febrero de 2026

---

## ❌ PROBLEMA IDENTIFICADO

### Síntomas Reportados:
- "Error al cargar equipos de La Liga. Verifica tu API key" en **crear partido**
- "Error al cargar equipos de La Liga. Verifica tu API key" en **crear equipos**
- "Error al cargar alineaciones: Error al cargar alineaciones" en **mis alineaciones**
- "Error al cargar partidos: Error al cargar partidos" en **crear alineación**

### Causa Real: **TOKEN JWT EXPIRADO**

**NO es un problema de la API key de fútbol.** La API key funciona perfectamente.

El error real encontrado en los logs es:

```
ERROR JwtAuthenticationFilter: Error al procesar JWT: 
JWT expired at 2026-02-04T18:06:00Z. 
Current time: 2026-02-06T15:09:44Z, 
a difference of 162224752 milliseconds.
```

---

## 🔧 EXPLICACIÓN TÉCNICA

### ¿Qué está pasando?

1. **El usuario inició sesión** hace varios días (2026-02-04)
2. **El token JWT expiró** después de 24 horas (configurado en `jwt.expiration=86400000` ms)
3. **El navegador sigue enviando el token expirado** almacenado en `localStorage`
4. **El backend rechaza el token** con error 403 (Forbidden)
5. **El frontend muestra "Error de API key"** porque el mensaje de error es genérico

### Flujo del Error:

```
Usuario → Petición con Token Expirado → Backend valida JWT → 
JWT Expirado → 403 Forbidden → Frontend → "Error de API key"
```

---

## ✅ SOLUCIÓN

### Opción 1: Cerrar Sesión y Volver a Iniciar (RECOMENDADO)

1. Abre la consola del navegador (F12 → Console)
2. Ejecuta:
   ```javascript
   localStorage.clear();
   ```
3. Refresca la página (F5)
4. Ve a `/login.html` e inicia sesión nuevamente

### Opción 2: Limpiar Datos del Navegador

1. En Chrome: Configuración → Privacidad → Borrar datos de navegación
2. Selecciona "Cookies y otros datos de sitios"
3. Borra los datos
4. Vuelve a iniciar sesión

---

## 📊 VERIFICACIONES REALIZADAS

### 1. API Key de Fútbol ✅ FUNCIONA
```powershell
# Prueba directa a la API:
Invoke-RestMethod -Uri "https://v3.football.api-sports.io/status" `
  -Headers @{"x-apisports-key"="272685a23e1e8119cf31697102b1c160"}
```

**Resultado:**
- Account: cesar (cealonspont@gmail.com)
- Plan: Free
- Estado: Activo
- Límite diario: 100 peticiones

### 2. Configuración de application.properties ✅ CORRECTA
```properties
api.football.key=272685a23e1e8119cf31697102b1c160
api.football.base-url=https://v3.football.api-sports.io
```

### 3. Inicialización del Servicio ✅ CORRECTA
```
INFO ApiFootballService: API Key configurada (primeros 10 caracteres): 272685a23e...
INFO ApiFootballService: ApiFootballService inicializado con URL: https://v3.football.api-sports.io
```

### 4. Token JWT ❌ EXPIRADO
```
ERROR JwtAuthenticationFilter: JWT expired at 2026-02-04T18:06:00Z
```

---

## 🛡️ MEJORAS RECOMENDADAS

### 1. Mejorar Mensajes de Error en el Frontend

Modificar `crear-partido.html` para mostrar mensajes más específicos:

```javascript
} catch (error) {
    console.error('Error:', error);
    
    // Detectar si es error de autenticación
    if (error.message.includes('401') || error.message.includes('403')) {
        showAlert('⚠️ Tu sesión ha expirado. Por favor, inicia sesión nuevamente.', 'error');
        setTimeout(() => window.location.href = 'login.html', 2000);
    } else {
        showAlert('Error al cargar equipos: ' + error.message, 'error');
    }
}
```

### 2. Implementar Renovación Automática de Token

En el frontend, verificar si el token está próximo a expirar y renovarlo automáticamente.

### 3. Redirigir Automáticamente al Login

Cuando el backend devuelva 401 o 403 por token expirado, redirigir automáticamente al login.

---

## 📝 RESUMEN

| Componente | Estado | Notas |
|------------|--------|-------|
| API Key de Fútbol | ✅ OK | Funciona correctamente |
| URL Base API | ✅ OK | https://v3.football.api-sports.io |
| Servicio Backend | ✅ OK | Se inicializa correctamente |
| Base de Datos | ✅ OK | Conexión establecida |
| Token JWT | ❌ EXPIRADO | Necesita renovación |

### Acción Requerida:
**Cerrar sesión (limpiar localStorage) y volver a iniciar sesión para obtener un nuevo token JWT.**

---

## 🚀 PASOS PARA CONTINUAR

1. **Inmediato:** Limpiar localStorage y volver a iniciar sesión
2. **Corto plazo:** Mejorar los mensajes de error en el frontend
3. **Medio plazo:** Implementar renovación automática de tokens
4. **Largo plazo:** Considerar usar refresh tokens para mayor seguridad

---

*Documento generado automáticamente durante el diagnóstico del proyecto.*
