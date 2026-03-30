# ⚽ Proyecto Fútbol App - API-Football Integration

> Sistema de gestión de equipos de fútbol con integración a API-Football para datos reales de equipos, jugadores y partidos.

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)]()
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.2-brightgreen)]()
[![Java](https://img.shields.io/badge/Java-21-orange)]()
[![API-Football](https://img.shields.io/badge/API--Football-v3-blue)]()

---

## 🎯 Características Principales

### ✅ Gestión de Usuarios
- Registro y autenticación con JWT
- Roles de usuario (ADMIN, USER)
- Gestión de perfiles

### ⚽ Integración con API-Football
- **Búsqueda de equipos reales** de todo el mundo
- **Obtención de jugadores** con estadísticas actualizadas
- **Alineaciones de partidos reales** en tiempo real
- **Creación automática** de equipos con datos oficiales
- **Personalización** de equipos seleccionando jugadores

### 🏆 Sistema de Equipos
- Crear equipos personalizados o desde API
- Almacenar alineaciones con formaciones tácticas
- Sistema de votación
- Comentarios y discusiones
- Gestión de partidos

---

## 🚀 Inicio Rápido

### Prerequisitos

- Java 21+
- Maven 3.8+
- MySQL 8.0+
- Cuenta gratuita en [API-Football](https://www.api-football.com/)

### Instalación

1. **Clonar el repositorio**
   ```bash
   git clone <repository-url>
   cd proyecto-ACD
   ```

2. **Configurar la base de datos**
   ```sql
   mysql -u root -p < schema.sql
   ```

3. **Configurar API-Football**
   - Crear cuenta en https://www.api-football.com/
   - Obtener API key gratuita
   - Editar `src/main/resources/application.properties`:
   ```properties
   api.football.key=TU_API_KEY_AQUI
   ```

4. **Configurar MySQL**
   ```properties
   spring.datasource.username=tu_usuario
   spring.datasource.password=tu_contraseña
   ```

5. **Compilar y ejecutar**
   ```bash
   .\mvnw.cmd clean install
   .\mvnw.cmd spring-boot:run
   ```

6. **Acceder a la aplicación**
   - API: http://localhost:8081
   - Swagger: http://localhost:8081/swagger-ui.html

---

## 📚 Documentación

### 📖 Documentación Completa

Toda la documentación técnica, guías, soluciones y explicaciones del proyecto está organizada en:

**📁 [Documentos-Explicativos/](Documentos-Explicativos/)** - 77 documentos organizados por categorías

👉 **Empieza aquí:** [Índice de Documentación](Documentos-Explicativos/00-INDICE.md)

### 📘 Guías de Inicio Rápido

| Documento | Descripción |
|-----------|-------------|
| [Guía de Inicio](Documentos-Explicativos/GUIA-INICIO.md) | Configuración inicial del proyecto |
| [Inicio Rápido](Documentos-Explicativos/INICIO-RAPIDO.md) | Cómo empezar rápidamente |
| [Cómo Funciona Login](Documentos-Explicativos/COMO-FUNCIONA-LOGIN.md) | Sistema de autenticación |
| [Cómo Funcionan Alineaciones](Documentos-Explicativos/COMO-FUNCIONAN-LAS-ALINEACIONES.md) | Lógica de alineaciones |

### 🔌 Integración API-Football

| Documento | Descripción |
|-----------|-------------|
| [Integración API-Football](Documentos-Explicativos/INTEGRACION-API-FOOTBALL.md) | Documentación completa |
| [Checklist Integración](Documentos-Explicativos/CHECKLIST-INTEGRACION.md) | Verificación paso a paso |
| [Cambio Endpoint Players/Squads](Documentos-Explicativos/CAMBIO-ENDPOINT-PLAYERS-SQUADS.md) | Actualización importante |

### 🎯 Funcionalidades Implementadas

| Documento | Descripción |
|-----------|-------------|
| [Selección de Liga](Documentos-Explicativos/IMPLEMENTACION-SELECCION-LIGA.md) | Crear partidos de 5 ligas europeas ⭐ |
| [Sistema de Comentarios](Documentos-Explicativos/SISTEMA-COMENTARIOS-ALINEACIONES.md) | Comentarios en alineaciones |
| [Mis Alineaciones](Documentos-Explicativos/MIS-ALINEACIONES-IMPLEMENTADO.md) | Vista de alineaciones propias |
| [Ver Alineaciones](Documentos-Explicativos/VER-ALINEACIONES-IMPLEMENTADO.md) | Ver alineaciones de partidos |

### 🐛 Solución de Problemas

| Documento | Descripción |
|-----------|-------------|
| [Fix Error Whitelabel Ranking](Documentos-Explicativos/FIX-WHITELABEL-RANKING.md) | Error 403 en ranking ⭐ |
| [Fix Escudos Crear Partido](Documentos-Explicativos/FIX-ESCUDOS-CREAR-PARTIDO.md) | Escudos no se mostraban ⭐ |
| [Fix 403 Ver Alineaciones](Documentos-Explicativos/FIX-403-VER-ALINEACIONES.md) | Error de seguridad |
| [Troubleshooting Login](Documentos-Explicativos/TROUBLESHOOTING-LOGIN.md) | Problemas de autenticación |

### 📊 Modelo de Datos

| Documento | Descripción |
|-----------|-------------|
| [Resumen Cambios Modelo](Documentos-Explicativos/RESUMEN-CAMBIOS-MODELO.md) | Estructura de BD |
| [Migración Votos Individuales](Documentos-Explicativos/MIGRACION-VOTOS-INDIVIDUALES.md) | Cambio sistema de votos |

---

## 🔌 API Endpoints

### Autenticación
```http
POST /api/auth/register  # Registrar usuario
POST /api/auth/login     # Iniciar sesión
```

### Búsqueda en API-Football
```http
GET /api/equipos/api-football/search?name={nombre}
GET /api/equipos/api-football/team/{id}/players?season={year}
GET /api/equipos/api-football/fixture/{id}/lineups
```

### Gestión de Equipos
```http
POST /api/equipos/create-from-api       # Crear automático
POST /api/equipos/create-custom         # Crear personalizado
PUT /api/equipos/{id}/update-lineup     # Actualizar alineación
```

Ver documentación completa en Swagger: http://localhost:8081/swagger-ui.html

---

## 🗄️ Modelo de Datos

### Entidades Principales

```
users           → Usuarios del sistema
  ↓
equipos         → Equipos creados por usuarios
  ↓
partidos        → Partidos entre equipos
  ↓
alineaciones    → Alineaciones específicas por partido
  ↓
comentarios     → Comentarios sobre equipos
  ↓
apis            → Registro de integraciones
```

### Estructura de Alineación (JSON)

```json
{
  "formacion": "4-3-3",
  "entrenador": "Pep Guardiola",
  "apiTeamId": 50,
  "logoEquipo": "https://...",
  "titulares": [
    {
      "id": 306,
      "nombre": "Ederson",
      "numero": 31,
      "posicion": "GK",
      "grid": "1:1"
    }
    // ... más jugadores
  ],
  "suplentes": [...]
}
```

---

## 🛠️ Tecnologías

### Backend
- **Spring Boot 4.0.2** - Framework principal
- **Spring Security** - Autenticación y autorización
- **Spring Data JPA** - Persistencia de datos
- **WebFlux** - Cliente HTTP reactivo
- **JWT** - Tokens de autenticación

### Base de Datos
- **MySQL 8.0** - Base de datos relacional
- **Hibernate** - ORM
- **JSON** - Almacenamiento flexible de alineaciones

### APIs Externas
- **API-Football v3** - Datos reales de fútbol

### Herramientas
- **Lombok** - Reducción de boilerplate
- **MapStruct** - Mapeo de DTOs
- **Swagger/OpenAPI** - Documentación interactiva
- **Maven** - Gestión de dependencias

---

## 💡 Casos de Uso

### 1. Fantasy Football
Crea tu equipo ideal combinando jugadores de diferentes equipos reales.

```bash
# Buscar tu equipo favorito
GET /api/equipos/api-football/search?name=Barcelona

# Obtener jugadores
GET /api/equipos/api-football/team/529/players?season=2024

# Crear equipo personalizado
POST /api/equipos/create-custom
```

### 2. Análisis Táctico
Compara alineaciones de diferentes partidos.

```bash
# Obtener alineación de un partido
GET /api/equipos/api-football/fixture/867946/lineups
```

### 3. Predicciones
Crea equipos para próximos partidos y vótalos.

```bash
# Crear equipo automáticamente
POST /api/equipos/create-from-api?apiTeamId=541&season=2024
```

---

## 📊 Estructura del Proyecto

```
proyecto-ACD/
├── src/main/java/com/futbol/proyectoacd/
│   ├── config/              # Configuración (Security, WebClient)
│   ├── controller/          # Controladores REST
│   ├── dto/                # Data Transfer Objects
│   ├── exception/          # Excepciones personalizadas
│   ├── model/              # Entidades JPA
│   │   ├── apifootball/   # Modelos de API-Football
│   │   └── ...            # Modelos internos
│   ├── repository/         # Repositorios JPA
│   └── service/            # Lógica de negocio
├── src/main/resources/
│   ├── application.properties
│   └── static/            # Recursos estáticos
├── scripts-sql/           # Scripts SQL (migraciones, datos, fixes)
│   ├── datos-prueba.sql
│   ├── gestionar-roles.sql
│   ├── agregar-*.sql
│   ├── fix-*.sql
│   ├── recrear-*.sql
│   ├── verificar-*.sql
│   └── diagnostico-*.sql
├── Documentos-Explicativos/ # Documentación técnica
└── pom.xml               # Dependencias Maven
```

---

## 🔐 Seguridad

- Autenticación JWT
- Endpoints protegidos por roles
- Contraseñas encriptadas con BCrypt
- CORS configurado
- Validación de inputs

---

## 🧪 Testing

### Ejecutar tests
```bash
.\mvnw.cmd test
```

### Probar endpoints manualmente
Usa el archivo `test-api-football.http` con tu IDE (IntelliJ, VS Code con extensión REST Client)

---

## 📈 Roadmap

### Fase 1 - Completada ✅
- [x] Sistema de autenticación
- [x] Gestión de usuarios
- [x] Integración con API-Football
- [x] CRUD de equipos
- [x] Documentación completa

### Fase 2 - En Desarrollo 🚧
- [ ] Sistema de votación
- [ ] Comparador de equipos
- [ ] Interfaz web
- [ ] Caché con Redis
- [ ] Tests automatizados

### Fase 3 - Planificado 📝
- [ ] Sistema de torneos
- [ ] Chat en tiempo real
- [ ] Estadísticas avanzadas
- [ ] App móvil
- [ ] Notificaciones push

---

## 🤝 Contribuir

1. Fork el proyecto
2. Crea una rama (`git checkout -b feature/nueva-funcionalidad`)
3. Commit tus cambios (`git commit -m 'Agregar nueva funcionalidad'`)
4. Push a la rama (`git push origin feature/nueva-funcionalidad`)
5. Abre un Pull Request

---

## 📄 Licencia

Este proyecto está bajo la licencia MIT.

---

## 👥 Autores

- Tu Nombre - Desarrollo inicial

---

## 🙏 Agradecimientos

- [API-Football](https://www.api-football.com/) por proporcionar datos de fútbol
- Spring Boot community
- Todos los contribuidores

---

## 📞 Soporte

- 📧 Email: soporte@futbolapp.com
- 📝 Issues: [GitHub Issues](https://github.com/tu-usuario/proyecto-ACD/issues)
- 📚 Docs: Ver carpeta `/docs`

---

**⚽ ¡Disfruta creando tus equipos de ensueño con datos reales! ⚽**
