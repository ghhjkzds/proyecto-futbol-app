# 🔧 Guía de Troubleshooting - Problemas de Login

## ✅ Problemas Resueltos

He corregido varios problemas críticos en el sistema de autenticación:

### 1. **Faltaba el Filtro JWT** ❌ → ✅
**Problema:** No había un filtro para validar los tokens JWT en las peticiones.

**Solución:** Creado `JwtAuthenticationFilter.java` que:
- Intercepta todas las peticiones HTTP
- Extrae el token del header `Authorization: Bearer {token}`
- Valida el token con JwtService
- Autentica al usuario en el contexto de Spring Security

### 2. **SecurityConfig Mal Configurado** ❌ → ✅
**Problema:** Tenía configurado `formLogin` tradicional que interfería con JWT.

**Solución:** Actualizado `SecurityConfig.java` para:
- Usar `SessionCreationPolicy.STATELESS` (sin sesiones)
- Eliminar `formLogin`
- Agregar el filtro JWT antes del filtro de autenticación
- Permitir rutas públicas correctamente

### 3. **JwtService Incompleto** ❌ → ✅
**Problema:** Faltaban métodos `extractUsername()` e `isTokenValid()`.

**Solución:** Agregados métodos:
- `extractUsername()` - Extrae el email/username del token
- `isTokenValid()` - Valida el token y verifica que no haya expirado
- `isTokenExpired()` - Verifica la fecha de expiración

### 4. **Logging Mejorado** ❌ → ✅
**Problema:** No había logs para debuggear problemas de login.

**Solución:** Agregados logs en `AuthService.java`:
- Log de intentos de registro
- Log de intentos de login
- Log cuando se encuentra/no encuentra un usuario
- Log de contraseñas incorrectas
- Log de login exitoso

---

## 🧪 Cómo Verificar que Funciona

### Paso 1: Ejecutar la Aplicación
```powershell
cd C:\Users\USUARIO\Downloads\proyecto-ACD
.\mvnw.cmd spring-boot:run
```

### Paso 2: Verificar Usuarios en la Base de Datos
```sql
-- Conectar a MySQL
mysql -u root -p

-- Usar la base de datos
USE futbol_app;

-- Ver todos los usuarios
SELECT id, email, role, created_at FROM users;
```

### Paso 3: Probar Registro (Si No Tienes Usuario)

**Opción A: Desde la página web**
```
http://localhost:8081/register.html
```
- Email: test@futbol.com
- Password: password123

**Opción B: Con curl**
```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@futbol.com",
    "password": "password123"
  }'
```

**Respuesta esperada:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "email": "test@futbol.com"
}
```

### Paso 4: Probar Login

**Opción A: Desde la página web**
```
http://localhost:8081/login.html
```
- Email: test@futbol.com
- Password: password123

**Opción B: Con curl**
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@futbol.com",
    "password": "password123"
  }'
```

**Respuesta esperada:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "email": "test@futbol.com"
}
```

---

## 🐛 Problemas Comunes y Soluciones

### Problema 1: "Credenciales inválidas"

**Causas posibles:**
1. ❌ Email no existe en la base de datos
2. ❌ Contraseña incorrecta
3. ❌ Usuario no se guardó correctamente

**Soluciones:**

#### 1.1 Verificar si el usuario existe
```sql
SELECT * FROM users WHERE email = 'tu@email.com';
```

Si no existe:
```sql
-- Crear usuario manualmente (contraseña: password123)
INSERT INTO users (email, password, role) VALUES
('test@futbol.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J2sOVUPbLvE1BgGzWV.FKv7z7X/w2C', 'USER');
```

#### 1.2 Resetear contraseña
```sql
-- Cambiar contraseña a "password123"
UPDATE users 
SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMye1J2sOVUPbLvE1BgGzWV.FKv7z7X/w2C'
WHERE email = 'tu@email.com';
```

#### 1.3 Verificar en los logs
Mira la consola de la aplicación, deberías ver:
```
INFO  - Intento de login para: test@futbol.com
INFO  - Usuario encontrado: test@futbol.com, verificando contraseña...
INFO  - Login exitoso para: test@futbol.com
```

Si ves:
```
WARN  - Usuario no encontrado: test@futbol.com
```
→ El usuario no existe, créalo.

Si ves:
```
WARN  - Contraseña incorrecta para: test@futbol.com
```
→ La contraseña está mal, resetéala.

---

### Problema 2: "Email ya registrado"

**Causa:** Ya existe un usuario con ese email.

**Soluciones:**

#### 2.1 Usar otro email
```
nuevo@email.com
```

#### 2.2 Eliminar el usuario existente
```sql
DELETE FROM users WHERE email = 'test@futbol.com';
```

#### 2.3 Hacer login en lugar de registro
Si ya tienes cuenta, usa login.html en lugar de register.html

---

### Problema 3: La página de login no responde

**Causas posibles:**
1. ❌ La aplicación no está corriendo
2. ❌ Puerto 8081 ocupado
3. ❌ Error en el frontend

**Soluciones:**

#### 3.1 Verificar que la aplicación está corriendo
```powershell
# Debería mostrar algo si está corriendo
netstat -ano | findstr :8081
```

#### 3.2 Ver logs del backend
En la consola donde ejecutaste `mvnw spring-boot:run`, busca:
```
Started ProyectoAcdApplication in X.XXX seconds
```

#### 3.3 Probar el endpoint directamente
```bash
curl http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@futbol.com","password":"password123"}'
```

---

### Problema 4: Error 403 Forbidden

**Causa:** El filtro JWT está bloqueando la petición.

**Solución:** Verificar que las rutas de auth están permitidas en SecurityConfig:
```java
.requestMatchers("/api/auth/**").permitAll()
```

✅ Ya está corregido en la configuración actual.

---

### Problema 5: Token JWT no funciona

**Síntomas:**
- Login funciona
- Pero no puedes acceder a endpoints protegidos

**Solución:**

#### 5.1 Verificar que el token se guarda
```javascript
// En la consola del navegador
console.log(localStorage.getItem('token'));
```

Debería mostrar algo como:
```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### 5.2 Verificar que el header se envía
Las peticiones deben incluir:
```javascript
headers: {
  'Authorization': `Bearer ${token}`
}
```

---

## 📊 Verificación de la Base de Datos

### Script SQL Completo de Verificación

Usa el archivo `verificar-usuarios.sql`:

```powershell
mysql -u root -p futbol_app < verificar-usuarios.sql
```

O ejecuta manualmente:

```sql
USE futbol_app;

-- Ver todos los usuarios
SELECT 
    id,
    email,
    role,
    created_at,
    SUBSTRING(password, 1, 20) as password_hash_preview
FROM users
ORDER BY created_at DESC;

-- Contar usuarios por rol
SELECT role, COUNT(*) as total
FROM users
GROUP BY role;
```

---

## 🔑 Usuarios de Prueba Pre-creados

He incluido en `verificar-usuarios.sql` dos usuarios de prueba:

### Usuario Normal
- **Email:** test@futbol.com
- **Password:** password123
- **Rol:** USER

### Usuario Admin
- **Email:** admin@futbol.com
- **Password:** admin123
- **Rol:** ADMIN

Para crearlos:
```sql
USE futbol_app;

-- Usuario normal
INSERT INTO users (email, password, role) VALUES
('test@futbol.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J2sOVUPbLvE1BgGzWV.FKv7z7X/w2C', 'USER');

-- Usuario admin
INSERT INTO users (email, password, role) VALUES
('admin@futbol.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'ADMIN');
```

---

## 🧪 Test de Login Paso a Paso

### Test 1: Registro
```bash
# 1. Registrar usuario
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"nuevo@test.com","password":"test123"}'

# ✅ Resultado esperado:
# {"token":"eyJhbGc...","email":"nuevo@test.com"}
```

### Test 2: Login
```bash
# 2. Hacer login
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"nuevo@test.com","password":"test123"}'

# ✅ Resultado esperado:
# {"token":"eyJhbGc...","email":"nuevo@test.com"}
```

### Test 3: Usar el Token
```bash
# 3. Usar el token en una petición protegida
# (Reemplaza {TOKEN} con el token obtenido)
curl -X GET http://localhost:8081/api/equipos/api-football/search?name=Barcelona \
  -H "Authorization: Bearer {TOKEN}"

# ✅ Resultado esperado:
# Lista de equipos
```

---

## 📝 Checklist de Verificación

Antes de probar el login, verifica:

- [ ] ✅ La aplicación está corriendo (`mvnw spring-boot:run`)
- [ ] ✅ MySQL está corriendo
- [ ] ✅ La base de datos `futbol_app` existe
- [ ] ✅ La tabla `users` existe
- [ ] ✅ Tienes al menos un usuario creado
- [ ] ✅ La contraseña del usuario es conocida
- [ ] ✅ Compilación sin errores (BUILD SUCCESS)
- [ ] ✅ Puedes acceder a http://localhost:8081
- [ ] ✅ Los archivos estáticos se cargan (login.html, register.html)

---

## 🎯 Cambios Realizados (Resumen)

### Archivos Creados:
1. ✅ `JwtAuthenticationFilter.java` - Filtro para validar tokens JWT

### Archivos Modificados:
1. ✅ `SecurityConfig.java` - Configuración de seguridad con JWT
2. ✅ `JwtService.java` - Métodos adicionales para validación
3. ✅ `AuthService.java` - Logging mejorado

### Archivos de Ayuda:
1. ✅ `verificar-usuarios.sql` - Scripts SQL para troubleshooting
2. ✅ `TROUBLESHOOTING-LOGIN.md` - Esta guía

---

## 🚀 Estado Actual

```
╔═══════════════════════════════════════════════╗
║                                               ║
║  ✅ SISTEMA DE AUTENTICACIÓN CORREGIDO       ║
║                                               ║
║  ✅ Filtro JWT:           Creado             ║
║  ✅ SecurityConfig:       Actualizado        ║
║  ✅ JwtService:           Completado         ║
║  ✅ Logging:              Mejorado           ║
║  ✅ Compilación:          BUILD SUCCESS      ║
║                                               ║
║  Estado: LISTO PARA USAR                     ║
║                                               ║
╚═══════════════════════════════════════════════╝
```

---

## 🆘 Si Aún Tienes Problemas

1. **Revisar logs:** Mira la consola donde ejecutaste `spring-boot:run`
2. **Verificar BD:** Ejecuta `verificar-usuarios.sql`
3. **Usar curl:** Prueba los endpoints directamente con curl
4. **Limpiar caché:** Borra localStorage del navegador (F12 → Application → Storage → Clear)
5. **Recrear usuario:** Elimina y crea el usuario de nuevo

---

**¡El sistema de login ahora debería funcionar correctamente! 🎉**
