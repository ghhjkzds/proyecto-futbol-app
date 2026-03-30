# ⚽ Proyecto Fútbol App - API-Football Integration

> Sistema de gestión de alineaciones de fútbol con integración a API-Football para datos reales de equipos, jugadores y partidos.

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.2-brightgreen)]()
[![Java](https://img.shields.io/badge/Java-21-orange)]()
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Neon-blue)]()
[![API-Football](https://img.shields.io/badge/API--Football-v3-blue)]()

---

## 🎯 Características Principales

### ✅ Gestión de Usuarios
- Registro y autenticación con JWT
- Roles de usuario (ADMIN, USER)

### ⚽ Integración con API-Football
- **Búsqueda de equipos reales** de todo el mundo
- **Obtención de jugadores** con estadísticas actualizadas
- **Alineaciones de partidos reales** en tiempo real
- **Creación automática** de equipos con datos oficiales
- **Personalización** de equipos seleccionando jugadores

### 🏆 Sistema de Alineaciones
- Crear alineaciones para partidos futuros
- Almacenar formaciones tácticas con titulares y suplentes
- Visualizar todas las alineaciones creadas por partido
- Gestión de tus propias alineaciones

---

## 🚀 Inicio Rápido

### Prerequisitos

- Java 21+
- Maven 3.8+
- Cuenta en [Neon](https://neon.tech/) (PostgreSQL serverless)
- Cuenta gratuita en [API-Football](https://www.api-football.com/)

### Instalación

1. **Clonar el repositorio**
   ```bash
   git clone <repository-url>
   cd proyecto-ACD
   ```

2. **Crear base de datos en Neon**
   - Crea un proyecto en https://console.neon.tech
   - Ejecuta el script: `scripts-sql/schema-neon.sql`

3. **Configurar la aplicación**
   Edita `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:postgresql://TU_HOST.neon.tech/TU_DB?sslmode=require
   spring.datasource.username=TU_USUARIO
   spring.datasource.password=TU_CONTRASEÑA
   api.football.key=TU_API_KEY
   ```

4. **Compilar y ejecutar**
   ```bash
   .\mvnw.cmd clean install
   .\mvnw.cmd spring-boot:run
   ```

5. **Acceder a la aplicación**
   - App: http://localhost:8081
   - Swagger: http://localhost:8081/swagger-ui.html

> ⚠️ Tras el primer arranque exitoso cambia `spring.jpa.hibernate.ddl-auto` de `create` a `validate` o `none` para no perder datos.

---

## 🔌 API Endpoints

### Autenticación
```http
POST /api/auth/register
POST /api/auth/login
```

### Equipos (integración API-Football)
```http
GET  /api/equipos/api-football/search?name={nombre}
GET  /api/equipos/api-football/team/{id}/players?season={year}
POST /api/equipos/create-from-api
POST /api/equipos/create-custom
```

### Alineaciones
```http
GET    /api/alineaciones/mis-alineaciones
GET    /api/alineaciones/partido/{partidoId}
POST   /api/alineaciones
POST   /api/alineaciones/from-api-football
DELETE /api/alineaciones/{id}
```

### Partidos
```http
GET  /api/partidos
POST /api/partidos/crear   (ADMIN)
```

Documentación completa: http://localhost:8081/swagger-ui.html

---

## 🗄️ Modelo de Datos

```
users        → Usuarios del sistema
equipos      → Equipos (sin votos)
partidos     → Partidos entre equipos
alineaciones → Alineaciones por usuario/partido/equipo (sin votos)
apis         → Registro de integraciones externas
```

### Estructura de Alineación (JSONB)

```json
{
  "formacion": "4-3-3",
  "entrenador": "Pep Guardiola",
  "apiTeamId": 50,
  "logoEquipo": "https://...",
  "titulares": [
    { "id": 306, "nombre": "Ederson", "numero": 31, "posicion": "Goalkeeper", "grid": "1:1" }
  ],
  "suplentes": [...]
}
```

---

## 🛠️ Tecnologías

| Capa | Tecnología |
|------|-----------|
| Framework | Spring Boot 4.0.2 |
| Seguridad | Spring Security + JWT |
| Persistencia | Spring Data JPA + Hibernate |
| Base de datos | PostgreSQL (Neon) |
| Cliente HTTP | WebFlux WebClient |
| Documentación | Swagger / SpringDoc OpenAPI |
| Build | Maven |
| Utilidades | Lombok, MapStruct |

---

## 📊 Estructura del Proyecto

```
proyecto-ACD/
├── src/main/java/com/futbol/proyectoacd/
│   ├── config/          # Configuración (Security, WebClient)
│   ├── controller/      # Controladores REST
│   ├── dto/             # Data Transfer Objects
│   ├── model/           # Entidades JPA
│   │   └── apifootball/ # Modelos de API-Football
│   ├── repository/      # Repositorios JPA
│   └── service/         # Lógica de negocio
├── src/main/resources/
│   ├── application.properties
│   └── static/          # Frontend HTML
├── scripts-sql/
│   └── schema-neon.sql  # DDL completo para Neon/PostgreSQL
└── pom.xml
```

---

## 🔐 Seguridad

- Autenticación JWT
- Endpoints protegidos por roles (ADMIN/USER)
- Contraseñas encriptadas con BCrypt
- CORS configurado
- Conexión a Neon con SSL obligatorio (`sslmode=require`)

---

## 🧪 Testing

```bash
.\mvnw.cmd test
```

Prueba de endpoints manual: `test-api-football.http` / `test-endpoints.http`
