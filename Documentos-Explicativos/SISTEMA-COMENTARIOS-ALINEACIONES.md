# 📝 Sistema de Comentarios para Alineaciones

## 🎯 Descripción General

Este documento explica cómo funciona el sistema de comentarios implementado para las alineaciones en la aplicación de fútbol. Los usuarios autenticados pueden comentar en las alineaciones de otros usuarios y responder a comentarios existentes, creando conversaciones anidadas.

---

## 🗄️ Arquitectura de Base de Datos

### Tabla: `comentarios`

La tabla `comentarios` ha sido modificada para soportar comentarios tanto en equipos como en alineaciones:

```sql
CREATE TABLE comentarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    equipo_id INT NULL,                    -- ID del equipo (opcional)
    alineacion_id INT NULL,                -- ID de la alineación (opcional)
    user_id INT NULL,                      -- ID del usuario que comenta
    mensaje TEXT NOT NULL,                 -- Contenido del comentario
    responde_a INT NULL,                   -- ID del comentario padre (para respuestas)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    FOREIGN KEY (equipo_id) REFERENCES equipos(id) ON DELETE CASCADE,
    FOREIGN KEY (alineacion_id) REFERENCES alineaciones(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (responde_a) REFERENCES comentarios(id) ON DELETE CASCADE,
    
    -- Verificar que el comentario pertenezca a equipo O alineación (no ambos)
    CONSTRAINT chk_comentario_tipo CHECK (
        (equipo_id IS NOT NULL AND alineacion_id IS NULL) 
        OR 
        (equipo_id IS NULL AND alineacion_id IS NOT NULL)
    )
);
```

### Características Clave:

1. **Flexibilidad**: Un comentario puede pertenecer a un equipo O a una alineación
2. **Autoría**: Cada comentario está vinculado al usuario que lo creó
3. **Respuestas anidadas**: Los comentarios pueden tener respuestas mediante `responde_a`
4. **Cascada**: Si se elimina una alineación, se eliminan sus comentarios automáticamente
5. **Integridad**: La constraint `chk_comentario_tipo` asegura que cada comentario pertenezca solo a un contexto

---

## 🏗️ Estructura del Backend (Java/Spring Boot)

### 1. Modelo: `Comentario.java`

```java
@Entity
@Table(name = "comentarios")
public class Comentario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipo_id")
    private Equipo equipo;                    // Opcional: comentario en equipo

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alineacion_id")
    private Alineacion alineacion;            // Opcional: comentario en alineación

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;                        // Usuario autor del comentario

    @Column(name = "mensaje", nullable = false)
    private String mensaje;                   // Texto del comentario

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responde_a")
    private Comentario respondeA;             // Comentario padre (si es respuesta)

    @CreationTimestamp
    private LocalDateTime createdAt;          // Fecha de creación
}
```

**Relaciones:**
- `alineacion`: Relación Many-to-One con la alineación
- `user`: Relación Many-to-One con el usuario autor
- `respondeA`: Auto-referencia para crear árbol de comentarios

---

### 2. DTOs (Data Transfer Objects)

#### `ComentarioDTO.java`

Objeto para transferir información de comentarios al frontend:

```java
public class ComentarioDTO {
    private Integer id;
    private Integer equipoId;
    private String equipoNombre;
    private Integer alineacionId;
    private Integer userId;
    private String userEmail;              // Email del autor
    private String mensaje;
    private Integer respondeAId;           // ID del comentario padre
    private LocalDateTime createdAt;
}
```

#### `CrearComentarioRequest.java`

Objeto para recibir peticiones de creación de comentarios:

```java
public class CrearComentarioRequest {
    private Integer alineacionId;          // ID de la alineación
    private String mensaje;                // Texto del comentario
    private Integer respondeAId;           // null si es comentario raíz
}
```

---

### 3. Repository: `ComentarioRepository.java`

```java
public interface ComentarioRepository extends JpaRepository<Comentario, Integer> {
    // Buscar comentarios por equipo
    List<Comentario> findByEquipo(Equipo equipo);
    
    // Buscar comentarios por alineación
    List<Comentario> findByAlineacion(Alineacion alineacion);
    
    // Buscar comentarios por alineación ordenados por fecha
    List<Comentario> findByAlineacionOrderByCreatedAtDesc(Alineacion alineacion);
    
    // Buscar respuestas de un comentario
    List<Comentario> findByRespondeA(Comentario comentario);
}
```

**Métodos principales:**
- `findByAlineacionOrderByCreatedAtDesc`: Obtiene todos los comentarios de una alineación, ordenados del más reciente al más antiguo
- `findByRespondeA`: Obtiene todas las respuestas de un comentario específico

---

### 4. Service: `ComentarioService.java`

#### Método: `obtenerComentariosPorAlineacion()`

```java
@Transactional(readOnly = true)
public List<ComentarioDTO> obtenerComentariosPorAlineacion(Integer alineacionId) {
    // 1. Buscar la alineación
    Alineacion alineacion = alineacionRepository.findById(alineacionId)
            .orElseThrow(() -> new RuntimeException("Alineación no encontrada"));

    // 2. Obtener todos los comentarios de la alineación
    List<Comentario> comentarios = 
        comentarioRepository.findByAlineacionOrderByCreatedAtDesc(alineacion);

    // 3. Construir árbol de comentarios (solo raíz, las respuestas se cargan bajo demanda)
    return construirArbolComentarios(comentarios);
}
```

**Flujo:**
1. Valida que la alineación existe
2. Obtiene todos los comentarios de la alineación ordenados
3. Filtra solo los comentarios raíz (los que no tienen `respondeA`)
4. Convierte a DTOs

---

#### Método: `crearComentario()`

```java
@Transactional
public ComentarioDTO crearComentario(CrearComentarioRequest request, Integer userId) {
    // 1. Validar que existe la alineación
    Alineacion alineacion = alineacionRepository.findById(request.getAlineacionId())
            .orElseThrow(() -> new RuntimeException("Alineación no encontrada"));

    // 2. Obtener el usuario
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

    // 3. Crear el comentario
    Comentario comentario = new Comentario();
    comentario.setAlineacion(alineacion);
    comentario.setUser(user);
    comentario.setMensaje(request.getMensaje());

    // 4. Si es una respuesta, vincular al comentario padre
    if (request.getRespondeAId() != null) {
        Comentario comentarioPadre = comentarioRepository.findById(request.getRespondeAId())
                .orElseThrow(() -> new RuntimeException("Comentario padre no encontrado"));
        comentario.setRespondeA(comentarioPadre);
    }

    // 5. Guardar y retornar
    Comentario guardado = comentarioRepository.save(comentario);
    return convertirADTO(guardado);
}
```

**Flujo:**
1. Valida la alineación
2. Valida el usuario
3. Crea el nuevo comentario
4. Si es una respuesta, vincula con el comentario padre
5. Guarda en la base de datos
6. Convierte a DTO y retorna

---

#### Método: `obtenerRespuestas()`

```java
@Transactional(readOnly = true)
public List<ComentarioDTO> obtenerRespuestas(Integer comentarioId) {
    // 1. Buscar el comentario padre
    Comentario comentario = comentarioRepository.findById(comentarioId)
            .orElseThrow(() -> new RuntimeException("Comentario no encontrado"));

    // 2. Obtener todas las respuestas
    List<Comentario> respuestas = comentarioRepository.findByRespondeA(comentario);

    // 3. Convertir a DTOs
    return respuestas.stream()
            .map(this::convertirADTO)
            .collect(Collectors.toList());
}
```

**Flujo:**
1. Valida que el comentario padre existe
2. Busca todas las respuestas vinculadas
3. Convierte a DTOs

---

### 5. Controller: `ComentarioController.java`

#### Endpoints disponibles:

##### 1. GET `/api/comentarios/alineacion/{alineacionId}`
**Descripción:** Obtiene todos los comentarios raíz de una alineación  
**Acceso:** Público (no requiere autenticación)  
**Respuesta:** Lista de `ComentarioDTO`

```java
@GetMapping("/alineacion/{alineacionId}")
public ResponseEntity<List<ComentarioDTO>> obtenerComentariosPorAlineacion(
        @PathVariable Integer alineacionId) {
    List<ComentarioDTO> comentarios = 
        comentarioService.obtenerComentariosPorAlineacion(alineacionId);
    return ResponseEntity.ok(comentarios);
}
```

---

##### 2. GET `/api/comentarios/{comentarioId}/respuestas`
**Descripción:** Obtiene todas las respuestas de un comentario específico  
**Acceso:** Público (no requiere autenticación)  
**Respuesta:** Lista de `ComentarioDTO`

```java
@GetMapping("/{comentarioId}/respuestas")
public ResponseEntity<List<ComentarioDTO>> obtenerRespuestas(
        @PathVariable Integer comentarioId) {
    List<ComentarioDTO> respuestas = 
        comentarioService.obtenerRespuestas(comentarioId);
    return ResponseEntity.ok(respuestas);
}
```

---

##### 3. POST `/api/comentarios`
**Descripción:** Crea un nuevo comentario o respuesta  
**Acceso:** Requiere autenticación (token JWT)  
**Body:** `CrearComentarioRequest`

```java
@PostMapping
public ResponseEntity<?> crearComentario(
        @RequestBody CrearComentarioRequest request,
        @RequestHeader("Authorization") String authHeader) {
    try {
        // 1. Extraer token JWT
        String token = authHeader.substring(7);
        
        // 2. Obtener email del usuario desde el token
        String email = jwtService.extractEmail(token);

        // 3. Buscar usuario en la base de datos
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 4. Crear comentario
        ComentarioDTO comentario = 
            comentarioService.crearComentario(request, user.getId());
            
        return ResponseEntity.ok(comentario);
    } catch (Exception e) {
        return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
    }
}
```

**Flujo de autenticación:**
1. Extrae el token JWT del header `Authorization`
2. Decodifica el token para obtener el email
3. Busca el usuario en la base de datos
4. Crea el comentario con el ID del usuario

---

### 6. Configuración de Seguridad

En `SecurityConfig.java` se permite acceso público a la lectura de comentarios:

```java
.requestMatchers(
    "/api/comentarios/alineacion/**",    // Leer comentarios
    "/api/comentarios/*/respuestas"      // Leer respuestas
).permitAll()
.requestMatchers("/api/comentarios")     // Crear comentarios (requiere auth)
    .authenticated()
```

---

## 🎨 Frontend (HTML/JavaScript)

### 1. Estructura HTML de Comentarios

Cada alineación tiene una sección de comentarios que se muestra/oculta:

```html
<div class="comentarios-section">
    <div class="comentarios-header">
        <h3>💬 Comentarios</h3>
        <button onclick="toggleComentarios(alineacionId)">Ver comentarios</button>
    </div>
    
    <div class="comentarios-container" id="comentarios-{alineacionId}">
        <!-- Formulario para nuevo comentario -->
        <div class="comentario-form">
            <textarea placeholder="Escribe tu comentario..."></textarea>
            <button onclick="enviarComentario(alineacionId)">Enviar</button>
        </div>
        
        <!-- Lista de comentarios -->
        <div class="comentarios-lista" id="lista-comentarios-{alineacionId}">
            <!-- Comentarios se cargan aquí -->
        </div>
    </div>
</div>
```

---

### 2. Funciones JavaScript Principales

#### `toggleComentarios(alineacionId)`

**Propósito:** Mostrar u ocultar la sección de comentarios  
**Comportamiento:**
- Si está oculta → la muestra y carga los comentarios desde el servidor
- Si está visible → la oculta

```javascript
async function toggleComentarios(alineacionId) {
    const container = document.getElementById(`comentarios-${alineacionId}`);
    const btn = event.target;

    if (container.classList.contains('visible')) {
        container.classList.remove('visible');
        btn.textContent = 'Ver comentarios';
    } else {
        container.classList.add('visible');
        btn.textContent = 'Ocultar comentarios';
        await cargarComentarios(alineacionId);  // Carga desde API
    }
}
```

---

#### `cargarComentarios(alineacionId)`

**Propósito:** Obtener comentarios del servidor y mostrarlos  

```javascript
async function cargarComentarios(alineacionId) {
    try {
        const response = await fetch(
            `${API_BASE_URL}/comentarios/alineacion/${alineacionId}`
        );
        
        if (!response.ok) throw new Error('Error al cargar comentarios');
        
        const comentarios = await response.json();
        renderizarComentarios(alineacionId, comentarios);
    } catch (error) {
        console.error('Error:', error);
        // Mostrar mensaje de error
    }
}
```

**Flujo:**
1. Llama al endpoint GET `/api/comentarios/alineacion/{id}`
2. Recibe array de comentarios
3. Renderiza cada comentario en el DOM

---

#### `enviarComentario(alineacionId)`

**Propósito:** Crear un nuevo comentario raíz  

```javascript
async function enviarComentario(alineacionId) {
    const textarea = document.getElementById(`comentario-texto-${alineacionId}`);
    const mensaje = textarea.value.trim();

    if (!mensaje) {
        mostrarAlerta('Por favor escribe un comentario', 'error');
        return;
    }

    const token = localStorage.getItem('token');
    if (!token) {
        mostrarAlerta('Debes iniciar sesión para comentar', 'error');
        window.location.href = 'login.html';
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/comentarios`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({
                alineacionId: alineacionId,
                mensaje: mensaje
            })
        });

        if (!response.ok) throw new Error('Error al enviar comentario');

        textarea.value = '';
        mostrarAlerta('✅ Comentario publicado', 'success');
        await cargarComentarios(alineacionId);  // Recargar lista
    } catch (error) {
        mostrarAlerta('Error al publicar el comentario', 'error');
    }
}
```

**Flujo:**
1. Valida que hay texto
2. Valida que el usuario está autenticado
3. Envía POST con el token JWT
4. Limpia el formulario
5. Recarga los comentarios para mostrar el nuevo

---

#### `mostrarFormularioRespuesta(comentarioId, alineacionId)`

**Propósito:** Mostrar formulario para responder a un comentario  

```javascript
async function mostrarFormularioRespuesta(comentarioId, alineacionId) {
    const respuestasDiv = document.getElementById(`respuestas-${comentarioId}`);
    
    // Evitar múltiples formularios
    if (respuestasDiv.querySelector('.formulario-respuesta')) {
        return;
    }

    // Cargar respuestas existentes primero
    await cargarRespuestas(comentarioId);

    // Crear formulario
    const formulario = document.createElement('div');
    formulario.className = 'formulario-respuesta';
    formulario.innerHTML = `
        <textarea id="respuesta-texto-${comentarioId}" 
                  placeholder="Escribe tu respuesta..."></textarea>
        <div class="acciones">
            <button onclick="enviarRespuesta(${comentarioId}, ${alineacionId})">
                📤 Enviar
            </button>
            <button onclick="cancelarRespuesta(${comentarioId})">
                ❌ Cancelar
            </button>
        </div>
    `;

    respuestasDiv.insertBefore(formulario, respuestasDiv.firstChild);
}
```

**Flujo:**
1. Verifica que no existe ya un formulario
2. Carga las respuestas existentes
3. Crea el formulario dinámicamente
4. Lo inserta en el DOM

---

#### `enviarRespuesta(comentarioId, alineacionId)`

**Propósito:** Enviar una respuesta a un comentario existente  

```javascript
async function enviarRespuesta(comentarioId, alineacionId) {
    const textarea = document.getElementById(`respuesta-texto-${comentarioId}`);
    const mensaje = textarea.value.trim();

    if (!mensaje) {
        mostrarAlerta('Por favor escribe una respuesta', 'error');
        return;
    }

    const token = localStorage.getItem('token');

    try {
        const response = await fetch(`${API_BASE_URL}/comentarios`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({
                alineacionId: alineacionId,
                mensaje: mensaje,
                respondeAId: comentarioId  // ← Vincula con el comentario padre
            })
        });

        if (!response.ok) throw new Error('Error al enviar respuesta');

        mostrarAlerta('✅ Respuesta publicada', 'success');
        cancelarRespuesta(comentarioId);
        await cargarRespuestas(comentarioId);  // Recargar respuestas
    } catch (error) {
        mostrarAlerta('Error al publicar la respuesta', 'error');
    }
}
```

**Diferencia clave:** El campo `respondeAId` indica que es una respuesta, no un comentario raíz

---

#### `cargarRespuestas(comentarioId)`

**Propósito:** Cargar y mostrar las respuestas de un comentario  

```javascript
async function cargarRespuestas(comentarioId) {
    try {
        const response = await fetch(
            `${API_BASE_URL}/comentarios/${comentarioId}/respuestas`
        );
        
        if (!response.ok) throw new Error('Error al cargar respuestas');
        
        const respuestas = await response.json();
        renderizarRespuestas(comentarioId, respuestas);
    } catch (error) {
        console.error('Error:', error);
    }
}
```

---

### 3. Renderizado de Comentarios

#### `renderizarComentarios(alineacionId, comentarios)`

```javascript
function renderizarComentarios(alineacionId, comentarios) {
    const lista = document.getElementById(`lista-comentarios-${alineacionId}`);

    if (comentarios.length === 0) {
        lista.innerHTML = '<p class="no-comentarios">💭 Aún no hay comentarios</p>';
        return;
    }

    lista.innerHTML = comentarios
        .map(comentario => renderComentario(comentario, alineacionId))
        .join('');
}
```

---

#### `renderComentario(comentario, alineacionId)`

```javascript
function renderComentario(comentario, alineacionId) {
    const fecha = new Date(comentario.createdAt).toLocaleString('es-ES', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });

    return `
        <div class="comentario" id="comentario-${comentario.id}">
            <div class="comentario-header-info">
                <span class="comentario-autor">👤 ${comentario.userEmail}</span>
                <span class="comentario-fecha">🕒 ${fecha}</span>
            </div>
            <div class="comentario-mensaje">${escapeHtml(comentario.mensaje)}</div>
            <div class="comentario-acciones">
                <button onclick="mostrarFormularioRespuesta(${comentario.id}, ${alineacionId})">
                    💬 Responder
                </button>
            </div>
            <div class="respuestas" id="respuestas-${comentario.id}"></div>
        </div>
    `;
}
```

**Elementos clave:**
- `comentario-autor`: Email del usuario
- `comentario-fecha`: Fecha formateada
- `comentario-mensaje`: Texto escapado (seguridad XSS)
- `respuestas`: Contenedor para las respuestas

---

### 4. Seguridad: Prevención de XSS

```javascript
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
```

**Propósito:** Escapar caracteres HTML para prevenir inyección de código malicioso

---

## 🔒 Flujo de Autenticación

### Token JWT

1. **Login:** Usuario obtiene token JWT
2. **Almacenamiento:** Token se guarda en `localStorage`
3. **Envío:** Se incluye en header `Authorization: Bearer {token}`
4. **Validación Backend:**
   - Se extrae el email del token
   - Se busca el usuario en la base de datos
   - Se asocia el comentario al usuario

### Expiración de Token

Si el token expira (401/403):

```javascript
if (response.status === 401 || response.status === 403) {
    mostrarAlerta('Tu sesión ha expirado. Redirigiendo...', 'error');
    localStorage.removeItem('token');
    setTimeout(() => window.location.href = 'login.html', 2000);
    return;
}
```

---

## 📊 Flujo Completo: Crear Comentario

```
┌─────────────┐
│   Usuario   │
└──────┬──────┘
       │ 1. Escribe comentario y hace clic en "Enviar"
       ▼
┌─────────────────────────────────┐
│ enviarComentario(alineacionId)  │
└──────┬──────────────────────────┘
       │ 2. Valida texto y autenticación
       ▼
┌─────────────────────────────────┐
│   POST /api/comentarios         │
│   Headers: Authorization        │
│   Body: { alineacionId,mensaje} │
└──────┬──────────────────────────┘
       │ 3. Llega al backend
       ▼
┌─────────────────────────────────┐
│  ComentarioController           │
│  - Extrae token JWT             │
│  - Obtiene email del token      │
│  - Busca usuario en BD          │
└──────┬──────────────────────────┘
       │ 4. Llama al servicio
       ▼
┌─────────────────────────────────┐
│  ComentarioService              │
│  - Valida alineación existe     │
│  - Crea entidad Comentario      │
│  - Vincula con usuario          │
│  - Guarda en BD                 │
└──────┬──────────────────────────┘
       │ 5. Retorna ComentarioDTO
       ▼
┌─────────────────────────────────┐
│  Frontend recibe respuesta      │
│  - Limpia formulario            │
│  - Recarga comentarios          │
│  - Muestra alerta de éxito      │
└─────────────────────────────────┘
```

---

## 📊 Flujo Completo: Responder a Comentario

```
┌─────────────┐
│   Usuario   │
└──────┬──────┘
       │ 1. Clic en "Responder" de un comentario
       ▼
┌────────────────────────────────────────┐
│ mostrarFormularioRespuesta()           │
│ - Carga respuestas existentes          │
│ - Crea formulario dinámico             │
└──────┬─────────────────────────────────┘
       │ 2. Usuario escribe y envía
       ▼
┌────────────────────────────────────────┐
│ enviarRespuesta(comentarioId,          │
│                 alineacionId)          │
└──────┬─────────────────────────────────┘
       │ 3. POST con respondeAId
       ▼
┌────────────────────────────────────────┐
│  Backend (igual que comentario raíz)   │
│  PERO: setRespondeA(comentarioPadre)   │
└──────┬─────────────────────────────────┘
       │ 4. Guarda con FK a comentario padre
       ▼
┌────────────────────────────────────────┐
│  Frontend                              │
│  - Cierra formulario                   │
│  - Recarga respuestas                  │
│  - Muestra la nueva respuesta          │
└────────────────────────────────────────┘
```

---

## 🎨 Estilos CSS Principales

### Sección de comentarios
```css
.comentarios-section {
    margin-top: 30px;
    padding-top: 20px;
    border-top: 2px solid #e9ecef;
}
```

### Comentario individual
```css
.comentario {
    background: white;
    padding: 15px;
    border-radius: 10px;
    border-left: 4px solid #667eea;
    box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}
```

### Respuestas anidadas
```css
.respuestas {
    margin-top: 15px;
    margin-left: 30px;           /* Indentación visual */
    padding-left: 15px;
    border-left: 2px solid #e9ecef;
}

.respuesta {
    background: #f8f9fa;         /* Fondo diferente */
    border-left: 3px solid #764ba2; /* Color diferente */
}
```

---

## 🚀 Cómo Usar el Sistema

### Para Usuarios:

1. **Ver comentarios:**
   - Ir a "Ver Alineaciones"
   - Seleccionar un partido
   - Hacer clic en una alineación para expandirla
   - Hacer clic en "Ver comentarios"

2. **Crear comentario:**
   - Escribir en el campo de texto
   - Hacer clic en "📤 Enviar comentario"

3. **Responder a comentario:**
   - Hacer clic en "💬 Responder" bajo un comentario
   - Escribir la respuesta
   - Hacer clic en "📤 Enviar"

---

## ✅ Checklist de Implementación

- [x] Modificar base de datos (agregar campos a `comentarios`)
- [x] Actualizar modelo `Comentario.java`
- [x] Crear DTOs (`ComentarioDTO`, `CrearComentarioRequest`)
- [x] Actualizar `ComentarioRepository`
- [x] Crear `ComentarioService` con lógica de negocio
- [x] Crear `ComentarioController` con endpoints REST
- [x] Configurar seguridad en `SecurityConfig`
- [x] Agregar estilos CSS en `ver-alineaciones.html`
- [x] Implementar funciones JavaScript para comentarios
- [x] Implementar funciones JavaScript para respuestas
- [x] Integrar comentarios en la UI de alineaciones
- [x] Implementar carga asíncrona de comentarios
- [x] Implementar prevención de XSS
- [x] Manejar expiración de tokens JWT

---

## 🔧 Mantenimiento y Extensiones Futuras

### Posibles mejoras:

1. **Edición de comentarios:** Permitir al autor editar sus comentarios
2. **Eliminación de comentarios:** Permitir al autor o admin eliminar
3. **Likes/Reacciones:** Agregar sistema de "me gusta" a comentarios
4. **Notificaciones:** Notificar cuando alguien responde a tu comentario
5. **Paginación:** Cargar comentarios en páginas para mejorar rendimiento
6. **Moderación:** Panel de administración para moderar comentarios
7. **Formato de texto:** Permitir markdown o formato básico en comentarios
8. **Menciones:** Sistema de @usuario para mencionar a otros

---

## 📝 Script SQL de Instalación

Ejecutar el siguiente script para habilitar comentarios en alineaciones:

```sql
USE futbol_app;

-- Modificar tabla comentarios
ALTER TABLE comentarios 
    MODIFY COLUMN equipo_id INT NULL,
    ADD COLUMN alineacion_id INT NULL AFTER equipo_id,
    ADD COLUMN user_id INT NULL AFTER alineacion_id;

-- Agregar foreign keys
ALTER TABLE comentarios
    ADD CONSTRAINT fk_comentarios_alineacion
        FOREIGN KEY (alineacion_id)
        REFERENCES alineaciones(id)
        ON DELETE CASCADE,
    ADD CONSTRAINT fk_comentarios_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE SET NULL;

-- Agregar constraint de validación
ALTER TABLE comentarios
    ADD CONSTRAINT chk_comentario_tipo
        CHECK (
            (equipo_id IS NOT NULL AND alineacion_id IS NULL) 
            OR 
            (equipo_id IS NULL AND alineacion_id IS NOT NULL)
        );
```

---

## 🐛 Troubleshooting

### Problema: No se cargan los comentarios
**Solución:** Verificar en consola del navegador si hay errores de CORS o de API

### Problema: "Token expirado"
**Solución:** Volver a hacer login para obtener un nuevo token

### Problema: No aparece el formulario de respuesta
**Solución:** Verificar que el usuario está autenticado

### Problema: Error al guardar comentario en BD
**Solución:** Ejecutar el script SQL de modificación de la tabla

---

## 📚 Referencias

- **JWT Authentication:** Tokens usados para identificar usuarios
- **Spring Data JPA:** Para operaciones de base de datos
- **Fetch API:** Para llamadas asíncronas al backend
- **LocalStorage:** Para almacenar token JWT en el navegador

---

**Fecha de creación:** 2026-02-09  
**Versión:** 1.0  
**Autor:** Sistema de Comentarios - Proyecto ACD
