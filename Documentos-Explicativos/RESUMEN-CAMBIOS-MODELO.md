# Resumen de Adaptaciones - Clases del Modelo a Base de Datos

## Cambios Realizados

### 1. **Clase User** (`User.java`)
**Cambios principales:**
- ✅ Cambio de `Long id` → `Integer id` (coincide con INT en MySQL)
- ✅ Eliminado campo `username` (no existe en la BD)
- ✅ Renombrado `rol` → `role` (coincide con el nombre de columna)
- ✅ Agregado `@Column(name = "role")` para mapeo explícito
- ✅ Agregado campo `createdAt` con `@CreationTimestamp`
- ✅ Ajustadas longitudes: `email` VARCHAR(150), `password` VARCHAR(255)

### 2. **Enum Rol** (`Rol.java`)
**Cambios principales:**
- ✅ Agregados valores en minúsculas `admin` y `user` para coincidir con ENUM de MySQL
- ✅ Agregado método `getValue()` para obtener el valor string

### 3. **Clase Equipo** (`Equipo.java`)
**Cambios principales:**
- ✅ Ya estaba correctamente mapeada
- ✅ Usa `Integer id` 
- ✅ Relación con `User` mediante `@ManyToOne` y `@JoinColumn(name = "id_user")`
- ✅ Campo `alineacion` de tipo JSON con `EquipoDetalles`
- ✅ Campo `createdAt` con `@CreationTimestamp`

### 4. **Nuevas Clases Creadas**

#### **Partido** (`Partido.java`)
```java
- Integer id
- Equipo equipoLocal (FK: equipo_local_id)
- Equipo equipoVisitante (FK: equipo_visitante_id)
- LocalDateTime fecha
- User creadoPor (FK: creado_por)
- LocalDateTime createdAt
```

#### **Alineacion** (`Alineacion.java`)
```java
- Integer id
- Partido partido (FK: partido_id)
- Equipo equipo (FK: equipo_id)
- EquipoDetalles alineacion (JSON)
- LocalDateTime createdAt
- Constraint UNIQUE (partido_id, equipo_id)
```

#### **Comentario** (`Comentario.java`)
```java
- Integer id
- Equipo equipo (FK: equipo_id)
- String mensaje (TEXT)
- Comentario respondeA (FK: responde_a) - auto-referencia
- LocalDateTime createdAt
```

#### **Api** (`Api.java`)
```java
- Integer id
- String nombre (VARCHAR 100)
- String idApi (VARCHAR 150, UNIQUE)
- LocalDateTime createdAt
```

### 5. **Repositorios Creados**
- ✅ `EquipoRepository.java`
- ✅ `PartidoRepository.java`
- ✅ `AlineacionRepository.java`
- ✅ `ComentarioRepository.java`
- ✅ `ApiRepository.java`

### 6. **DTOs Creados**
- ✅ `EquipoDTO.java` (ya existía)
- ✅ `PartidoDTO.java`
- ✅ `AlineacionDTO.java`
- ✅ `ComentarioDTO.java`

### 7. **Servicios Actualizados**

#### **UserRepository**
- ✅ Cambio de `JpaRepository<User, Long>` → `JpaRepository<User, Integer>`
- ✅ Eliminados métodos `findByUsername()` y `existsByUsername()`

#### **UserService**
- ✅ Actualizado método `crearUsuario()` - sin `username`, con `role`
- ✅ Cambiados todos los `Long id` → `Integer id`
- ✅ Eliminadas referencias a `username`
- ✅ Renombrado `rol` → `role`

#### **CustomUserDetailsService**
- ✅ Cambio de `loadUserByUsername(String username)` ahora carga por `email`
- ✅ Usa `usuario.getRole()` en lugar de `usuario.getRol()`

#### **UsuarioController**
- ✅ Actualizados parámetros de métodos para usar `Integer id`
- ✅ Eliminado parámetro `username` del método `crearUsuario()`

### 8. **Configuración**

#### **application.properties**
```properties
# ANTES: H2 en memoria
spring.datasource.url=jdbc:h2:mem:testdb

# AHORA: MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/futbol_app?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.driverClassName=com.mysql.cj.jdbc.Driver
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
```

## Estructura de la Base de Datos

```
users (tabla principal de usuarios)
  ↓
equipos (equipos creados por usuarios)
  ↓
partidos (partidos entre equipos)
  ↓
alineaciones (alineaciones específicas por partido)

equipos
  ↓
comentarios (comentarios sobre equipos, pueden responder a otros)

apis (registro de datos de APIs externas)
```

## Relaciones Principales

1. **User → Equipo**: Un usuario puede tener muchos equipos (1:N)
2. **Equipo → Partido**: Un equipo puede participar en muchos partidos (N:N indirecta)
3. **Partido → Alineacion**: Un partido puede tener varias alineaciones (1:N)
4. **Equipo → Comentario**: Un equipo puede tener muchos comentarios (1:N)
5. **Comentario → Comentario**: Auto-referencia para respuestas (1:N)

## Próximos Pasos Recomendados

1. **Crear la base de datos MySQL** ejecutando el script SQL proporcionado
2. **Configurar credenciales** en `application.properties` (usuario y contraseña de MySQL)
3. **Crear servicios** para las nuevas entidades (EquipoService, PartidoService, etc.)
4. **Crear controladores REST** para exponer endpoints
5. **Implementar lógica de negocio** específica de la aplicación
6. **Agregar validaciones** en los DTOs con anotaciones de validación

## Verificación

✅ Compilación exitosa con Maven
✅ Todas las clases de modelo coinciden con las tablas de la BD
✅ Todos los tipos de datos son compatibles (Integer, String, LocalDateTime, JSON)
✅ Todas las relaciones Foreign Key están mapeadas correctamente
✅ Repositorios creados para todas las entidades
