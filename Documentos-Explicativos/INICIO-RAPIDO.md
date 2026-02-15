# 🚀 Inicio Rápido - Tu Proyecto Configurado

## ✅ API Key Configurada

Tu API key **"patatatata"** ya está configurada en el proyecto.

**Ubicación:** `src/main/resources/application.properties`

```properties
api.football.key=patatatata
api.football.base-url=https://v3.football.api-sports.io
```

---

## 🏃 Ejecutar el Proyecto

### Opción 1: Desde PowerShell/CMD
```powershell
cd C:\Users\USUARIO\Downloads\proyecto-ACD
.\mvnw.cmd spring-boot:run
```

### Opción 2: Desde IntelliJ IDEA
1. Abre el proyecto
2. Busca la clase `ProyectoAcdApplication.java`
3. Click derecho → Run 'ProyectoAcdApplication'

---

## 🔧 Antes de Ejecutar: Configurar MySQL

⚠️ **IMPORTANTE:** Asegúrate de tener MySQL configurado:

### 1. Crear la Base de Datos
```sql
CREATE DATABASE futbol_app;
USE futbol_app;
-- Ejecutar el script de creación de tablas (schema.sql si lo tienes)
```

### 2. Configurar Credenciales
Si tu usuario/contraseña de MySQL no es `root` sin contraseña, edita:

**Archivo:** `src/main/resources/application.properties`
```properties
spring.datasource.username=TU_USUARIO_MYSQL
spring.datasource.password=TU_CONTRASEÑA_MYSQL
```

---

## 🎯 Probar la Aplicación

### 1. Verificar que está funcionando
Abre tu navegador en:
```
http://localhost:8081
```

### 2. Acceder a Swagger UI
```
http://localhost:8081/swagger-ui.html
```
Aquí podrás probar todos los endpoints interactivamente.

---

## 🧪 Probar la Integración con API-Football

### Paso 1: Registrar un Usuario
```http
POST http://localhost:8081/api/auth/register
Content-Type: application/json

{
  "email": "test@futbol.com",
  "password": "password123"
}
```

### Paso 2: Login y Obtener Token
```http
POST http://localhost:8081/api/auth/login
Content-Type: application/json

{
  "email": "test@futbol.com",
  "password": "password123"
}
```

**Respuesta:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "email": "test@futbol.com"
}
```

**🔑 Guarda este token** - Lo necesitarás para crear equipos.

### Paso 3: Buscar un Equipo
```http
GET http://localhost:8081/api/equipos/api-football/search?name=Barcelona
```

**Respuesta:**
```json
[
  {
    "team": {
      "id": 529,
      "name": "Barcelona",
      "logo": "https://media.api-sports.io/football/teams/529.png"
    }
  }
]
```

### Paso 4: Crear tu Equipo
```http
POST http://localhost:8081/api/equipos/create-from-api?apiTeamId=529&season=2024
Authorization: Bearer TU_TOKEN_AQUI
```

---

## 📋 Equipos Populares para Probar

| Equipo | ID | Liga |
|--------|----|----- |
| Barcelona | 529 | La Liga 🇪🇸 |
| Real Madrid | 541 | La Liga 🇪🇸 |
| Manchester City | 50 | Premier League 🏴󠁧󠁢󠁥󠁮󠁧󠁿 |
| Liverpool | 40 | Premier League 🏴󠁧󠁢󠁥󠁮󠁧󠁿 |
| Bayern Munich | 157 | Bundesliga 🇩🇪 |
| PSG | 85 | Ligue 1 🇫🇷 |
| Juventus | 496 | Serie A 🇮🇹 |
| AC Milan | 489 | Serie A 🇮🇹 |

---

## 🛠️ Comandos Útiles

### Compilar
```powershell
.\mvnw.cmd clean compile
```

### Empaquetar (crear JAR)
```powershell
.\mvnw.cmd clean package
```

### Ejecutar
```powershell
.\mvnw.cmd spring-boot:run
```

### Ejecutar Tests
```powershell
.\mvnw.cmd test
```

---

## 🐛 Solución de Problemas

### Error: "Access denied for user 'root'"
**Solución:** Configura tu usuario y contraseña de MySQL en `application.properties`

### Error: "Unknown database 'futbol_app'"
**Solución:** Ejecuta el script SQL para crear la base de datos

### Error: "Port 8081 is already in use"
**Solución:** Cambia el puerto en `application.properties`:
```properties
server.port=8082
```

### La API-Football no responde
**Verificaciones:**
1. ✅ Tu API key está configurada: `patatatata`
2. ✅ Tienes conexión a internet
3. ✅ No has excedido el límite (100 requests/día en plan gratuito)
4. Verifica el status en: https://status.api-football.com/

---

## 📚 Archivos de Ayuda

- `README.md` - Documentación general
- `INTEGRACION-API-FOOTBALL.md` - Guía completa de la API
- `test-api-football.http` - Ejemplos de peticiones HTTP
- `CHECKLIST-INTEGRACION.md` - Lista de verificación

---

## 🎉 ¡Listo para Usar!

Tu proyecto está **100% configurado** con:
- ✅ API key de API-Football: **patatatata**
- ✅ Base de datos: MySQL (futbol_app)
- ✅ Puerto: 8081
- ✅ Endpoints REST: 6 funcionando
- ✅ Swagger UI: Disponible
- ✅ Autenticación JWT: Activa

**Solo necesitas:**
1. Configurar MySQL (si aún no lo has hecho)
2. Ejecutar: `.\mvnw.cmd spring-boot:run`
3. Abrir: http://localhost:8081/swagger-ui.html
4. ¡Crear tus equipos de ensueño! ⚽

---

**Proyecto:** proyecto-ACD  
**API Key:** patatatata  
**Estado:** ✅ LISTO PARA EJECUTAR
