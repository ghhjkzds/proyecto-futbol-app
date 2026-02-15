# 🔐 Cómo Funciona el Login - Explicación Completa

## ✅ Respuestas a tus Preguntas

### 1. ¿El login comprueba los datos con la base de datos?
**SÍ**, completamente. Aquí está el flujo completo:

```
Usuario → login.html → POST /api/auth/login → AuthController
                                                    ↓
                                              AuthService
                                                    ↓
                                        userRepository.findByEmail()
                                                    ↓
                                              MySQL (futbol_app)
                                                    ↓
                                    passwordEncoder.matches(password)
                                                    ↓
                                        ✅ Válido → genera JWT
                                        ❌ Inválido → error 401
```

### 2. ¿El login usa método POST?
**SÍ**, el método es POST. Lo he verificado y corregido un error de código duplicado.

---

## 🔄 Flujo Completo del Login (Paso a Paso)

### Frontend (login.html)

#### 1. Usuario completa el formulario
```html
<form id="loginForm" onsubmit="handleLogin(event)">
    <input type="email" id="email" />
    <input type="password" id="password" />
    <button type="submit">Iniciar Sesión</button>
</form>
```

#### 2. JavaScript intercepta el submit
```javascript
async function handleLogin(event) {
    event.preventDefault();  // Previene submit tradicional
    
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    
    // Hace petición POST a la API
    const response = await fetch('/api/auth/login', {
        method: 'POST',                          // ✅ MÉTODO POST
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ email, password }) // Envía credenciales
    });
}
```

---

### Backend (Java Spring Boot)

#### 3. AuthController recibe la petición
```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @PostMapping("/login")  // ✅ Endpoint POST
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
```

#### 4. AuthService busca en la base de datos
```java
@Service
public class AuthService {
    
    public AuthResponse login(LoginRequest request) {
        log.info("Intento de login para: {}", request.getEmail());
        
        // 🔍 PASO 1: Buscar usuario en la base de datos
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Usuario no encontrado: {}", request.getEmail());
                    return new RuntimeException("Credenciales inválidas");
                });
        
        log.debug("Usuario encontrado: {}, verificando contraseña...", user.getEmail());
        
        // 🔐 PASO 2: Verificar contraseña encriptada
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Contraseña incorrecta para: {}", request.getEmail());
            throw new RuntimeException("Credenciales inválidas");
        }
        
        // ✅ PASO 3: Generar JWT token
        log.info("Login exitoso para: {} con rol: {}", user.getEmail(), user.getRole());
        String token = jwtService.generateToken(user.getEmail());
        
        // 📤 PASO 4: Retornar respuesta
        return new AuthResponse(token, user.getEmail(), user.getRole().name());
    }
}
```

#### 5. UserRepository consulta MySQL
```java
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    
    // Este método genera automáticamente:
    // SELECT * FROM users WHERE email = ?
    Optional<User> findByEmail(String email);
}
```

#### 6. MySQL ejecuta la consulta
```sql
-- Consulta real que se ejecuta:
SELECT 
    id, 
    email, 
    password,  -- Hash BCrypt
    role, 
    created_at 
FROM users 
WHERE email = 'test@ejemplo.com';
```

#### 7. PasswordEncoder verifica la contraseña
```java
// La contraseña en BD está encriptada con BCrypt
// Ejemplo: $2a$10$N9qo8uLOickgx2ZMRZoMye1J2sOVUPbLvE1BgGzWV.FKv7z7X/w2C

// passwordEncoder.matches() compara:
String passwordIngresada = "password123";  // Del formulario
String hashEnBD = "$2a$10$N9qo8uLOick..."; // De la BD

boolean esValida = passwordEncoder.matches(passwordIngresada, hashEnBD);
// ✅ true  → Login exitoso
// ❌ false → Credenciales inválidas
```

---

## 🗄️ Verificación en la Base de Datos

### Consulta para ver usuarios
```sql
USE futbol_app;

SELECT 
    id,
    email,
    SUBSTRING(password, 1, 30) as password_hash,  -- Primeros 30 caracteres
    role,
    created_at
FROM users;
```

### Ejemplo de resultado:
```
+----+------------------+--------------------------------+-------+---------------------+
| id | email            | password_hash                  | role  | created_at          |
+----+------------------+--------------------------------+-------+---------------------+
|  1 | test@ejemplo.com | $2a$10$N9qo8uLOickgx2ZMRZoM... | USER  | 2026-02-03 10:30:00 |
|  2 | admin@test.com   | $2a$10$92IXUNpkjO0rOQ5byMi... | ADMIN | 2026-02-03 11:15:00 |
+----+------------------+--------------------------------+-------+---------------------+
```

---

## 🔐 Seguridad del Login

### 1. Encriptación de Contraseñas (BCrypt)
```java
// Cuando se registra un usuario:
String plainPassword = "password123";
String hashedPassword = passwordEncoder.encode(plainPassword);
// Resultado: $2a$10$N9qo8uLOickgx2ZMRZoMye1J2sOVUPbLvE1BgGzWV.FKv7z7X/w2C

// Las contraseñas NUNCA se guardan en texto plano
```

**Características de BCrypt:**
- ✅ One-way hash (no se puede desencriptar)
- ✅ Salt automático (cada hash es único)
- ✅ Resistente a rainbow tables
- ✅ Lento intencionalmente (previene brute force)

### 2. Validación Backend
```java
// SIEMPRE se valida en el servidor
// Aunque alguien modifique el JavaScript, el backend rechazará credenciales inválidas
```

### 3. JWT Token
```java
// Después del login exitoso:
String token = jwtService.generateToken(user.getEmail());

// El token incluye:
// - Email del usuario
// - Fecha de emisión
// - Fecha de expiración (24 horas por defecto)
// - Firma digital (HMAC-SHA256)
```

### 4. HTTPS en Producción
```
// En desarrollo: http://localhost:8081
// En producción: https://tudominio.com (OBLIGATORIO)
```

---

## 📊 Comparación: Flujo GET vs POST

### ❌ GET (INCORRECTO - No usado)
```javascript
// SI fuera GET (mal):
fetch('/api/auth/login?email=test@ejemplo.com&password=password123')

// Problemas:
// ❌ Contraseña visible en URL
// ❌ Queda en logs del servidor
// ❌ Queda en historial del navegador
// ❌ Puede ser cacheada
```

### ✅ POST (CORRECTO - Implementado)
```javascript
// Método POST (bien):
fetch('/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password })
})

// Ventajas:
// ✅ Contraseña en el body (no en URL)
// ✅ No aparece en logs
// ✅ No queda en historial
// ✅ No se cachea
// ✅ Más seguro
```

---

## 🧪 Prueba de Verificación con BD

### Paso 1: Crear usuario en MySQL
```sql
USE futbol_app;

-- Crear usuario con contraseña conocida
-- Password en texto plano: "password123"
-- Hash BCrypt: $2a$10$N9qo8uLOickgx2ZMRZoMye1J2sOVUPbLvE1BgGzWV.FKv7z7X/w2C

INSERT INTO users (email, password, role) VALUES
('prueba@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J2sOVUPbLvE1BgGzWV.FKv7z7X/w2C', 'USER');

-- Verificar
SELECT * FROM users WHERE email = 'prueba@test.com';
```

### Paso 2: Intentar login desde la web
```
1. Ir a: http://localhost:8081/login.html
2. Email: prueba@test.com
3. Password: password123
4. Click "Iniciar Sesión"
```

### Paso 3: Ver logs del servidor
```
En la consola donde ejecutaste spring-boot:run verás:

INFO  - Intento de login para: prueba@test.com
DEBUG - Usuario encontrado: prueba@test.com, verificando contraseña...
INFO  - Login exitoso para: prueba@test.com con rol: USER
```

### Paso 4: Verificar en el navegador
```javascript
// Abrir consola (F12)
localStorage.getItem('token')     // Token JWT
localStorage.getItem('userEmail') // prueba@test.com
localStorage.getItem('userRole')  // USER
```

---

## ❌ Casos de Error

### Error 1: Usuario no existe
```
Input: test@noexiste.com / password123

Backend log:
WARN - Usuario no encontrado: test@noexiste.com

Response:
{
  "message": "Credenciales inválidas"
}

Frontend:
❌ Error en el login
Credenciales inválidas
```

### Error 2: Contraseña incorrecta
```
Input: prueba@test.com / wrongpassword

Backend log:
DEBUG - Usuario encontrado: prueba@test.com, verificando contraseña...
WARN - Contraseña incorrecta para: prueba@test.com

Response:
{
  "message": "Credenciales inválidas"
}

Frontend:
❌ Error en el login
Credenciales inválidas
```

### Error 3: Base de datos no disponible
```
Backend log:
ERROR - Could not open JDBC Connection

Response:
500 Internal Server Error

Frontend:
❌ Error en el login
Error al conectar con el servidor
```

---

## 🔍 Resumen

### ✅ El Login SÍ comprueba con la BD:
1. Busca el usuario por email en MySQL
2. Compara la contraseña usando BCrypt
3. Verifica el rol del usuario
4. Genera un token JWT si todo es correcto
5. Retorna token + email + rol al frontend

### ✅ El Login SÍ usa método POST:
```javascript
fetch('/api/auth/login', {
    method: 'POST',  // ✅ Correcto
    // ...
})
```

### ✅ Seguridad implementada:
- BCrypt para contraseñas
- JWT para sesiones
- Validación en backend
- Logs de auditoría
- CORS configurado

---

## 📝 Archivos Involucrados

### Frontend:
- `login.html` - Formulario y JavaScript

### Backend:
- `AuthController.java` - Recibe petición POST
- `AuthService.java` - Lógica de validación
- `UserRepository.java` - Consulta MySQL
- `JwtService.java` - Genera tokens
- `SecurityConfig.java` - Configuración de seguridad

### Base de Datos:
- Tabla `users` en MySQL
- Campos: id, email, password (hash), role, created_at

---

**✅ TODO CORRECTO: El login usa POST y verifica contra la base de datos MySQL con encriptación BCrypt.**
