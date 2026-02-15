# Guía de Inicio Rápido - Proyecto Fútbol App

## Prerequisitos

1. **Java 21** instalado
2. **Maven** (incluido con el wrapper `mvnw`)
3. **MySQL Server** instalado y ejecutándose
4. **IntelliJ IDEA** (recomendado) o cualquier IDE Java

## Pasos para Iniciar

### 1. Configurar la Base de Datos

#### Opción A: Crear la base de datos desde línea de comandos
```bash
mysql -u root -p
```

Luego ejecuta el script de creación que está en la raíz del proyecto:
```sql
source C:/Users/USUARIO/Downloads/proyecto-ACD/schema.sql
```

#### Opción B: Usar un cliente MySQL (MySQL Workbench, DBeaver, etc.)
- Abre el archivo de la estructura de la BD (el script SQL proporcionado)
- Ejecuta todo el script para crear la base de datos `futbol_app` y sus tablas

### 2. Configurar Credenciales de MySQL

Edita el archivo `src/main/resources/application.properties`:

```properties
# Si tu usuario de MySQL no es 'root', cámbialo aquí
spring.datasource.username=root

# Añade tu contraseña de MySQL aquí
spring.datasource.password=TU_CONTRASEÑA_AQUI
```

### 3. (Opcional) Cargar Datos de Prueba

Si quieres tener datos de ejemplo en tu base de datos:

```bash
mysql -u root -p futbol_app < C:/Users/USUARIO/Downloads/proyecto-ACD/datos-prueba.sql
```

O desde MySQL Workbench/cliente, ejecuta el archivo `datos-prueba.sql`.

### 4. Compilar el Proyecto

Desde PowerShell o CMD en la carpeta del proyecto:

```powershell
.\mvnw.cmd clean install
```

O desde IntelliJ:
- Click derecho en el proyecto → Maven → Reload Project
- Luego: Maven → proyecto-ACD → Lifecycle → install

### 5. Ejecutar la Aplicación

#### Desde línea de comandos:
```powershell
.\mvnw.cmd spring-boot:run
```

#### Desde IntelliJ:
- Abre la clase `ProyectoAcdApplication.java`
- Click en el botón verde de "Run" ▶️

### 6. Verificar que Funciona

La aplicación debería iniciar en: `http://localhost:8081`

Puedes verificar que está funcionando accediendo a:
- **Swagger UI**: http://localhost:8081/swagger-ui.html
- **Health Check**: http://localhost:8081/actuator/health

## Endpoints Disponibles

### Autenticación
- `POST /api/auth/register` - Registrar nuevo usuario
- `POST /api/auth/login` - Iniciar sesión

### Usuarios (requiere autenticación)
- `GET /usuarios/lista` - Listar usuarios (solo ADMIN)
- `POST /usuarios` - Crear usuario (solo ADMIN)
- `GET /usuarios/{id}/editar` - Ver usuario para editar
- `POST /usuarios/{id}/editar` - Actualizar usuario
- `POST /usuarios/{id}/eliminar` - Eliminar usuario (solo ADMIN)

## Estructura del Proyecto

```
proyecto-ACD/
├── src/main/java/com/futbol/proyectoacd/
│   ├── config/          # Configuración (Security, Exception Handlers)
│   ├── controller/      # Controladores REST y Web
│   ├── dto/            # Data Transfer Objects
│   ├── exception/      # Excepciones personalizadas
│   ├── model/          # Entidades JPA (User, Equipo, Partido, etc.)
│   ├── repository/     # Repositorios JPA
│   └── service/        # Lógica de negocio
├── src/main/resources/
│   ├── application.properties  # Configuración de la app
│   ├── static/         # HTML, CSS, JS
│   └── templates/      # Plantillas Thymeleaf
└── pom.xml             # Dependencias Maven
```

## Modelos Principales

1. **User**: Usuarios del sistema (admin o user)
2. **Equipo**: Equipos de fútbol creados por usuarios
3. **Partido**: Partidos entre equipos
4. **Alineacion**: Alineaciones específicas por partido
5. **Comentario**: Comentarios sobre equipos
6. **Api**: Registro de integraciones con APIs externas

## Próximos Pasos

1. ✅ Configurar MySQL y ejecutar scripts
2. ✅ Compilar y ejecutar la aplicación
3. 🔲 Crear servicios para Equipo, Partido, etc.
4. 🔲 Crear controladores REST para las entidades
5. 🔲 Integrar con la API de SportsMonks
6. 🔲 Crear interfaces de usuario para gestión de equipos
7. 🔲 Implementar sistema de votación
8. 🔲 Implementar sistema de partidos

## Solución de Problemas

### Error: "Access denied for user 'root'@'localhost'"
- Verifica que la contraseña en `application.properties` sea correcta
- Asegúrate de que MySQL está ejecutándose

### Error: "Unknown database 'futbol_app'"
- Ejecuta el script SQL de creación de la base de datos primero

### Error: "Port 8081 is already in use"
- Cambia el puerto en `application.properties`: `server.port=8082`
- O detén la aplicación que está usando el puerto 8081

### Error de compilación con Lombok
- Asegúrate de tener habilitado el plugin de Lombok en IntelliJ
- File → Settings → Plugins → buscar "Lombok" → Install

## Contacto y Ayuda

Para más información, consulta los siguientes archivos:
- `RESUMEN-CAMBIOS-MODELO.md` - Detalles de la estructura de datos
- `HELP.md` - Documentación de Spring Boot
- `test-endpoints.http` - Ejemplos de llamadas a endpoints
