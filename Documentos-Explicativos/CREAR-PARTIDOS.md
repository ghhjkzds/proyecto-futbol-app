# 🏆 Guía: Crear Partidos - La Liga

## ✅ Funcionalidad Implementada

Se ha creado una página web completa para que los **administradores** puedan crear partidos entre equipos de La Liga.

---

## 📁 Archivos Creados

### Backend (Java)
1. **PartidoController.java** - Controlador REST para gestión de partidos
   - `GET /api/partidos/equipos-laliga` - Obtiene equipos de La Liga
   - `POST /api/partidos/crear` - Crea un nuevo partido
   - `GET /api/partidos` - Lista todos los partidos
   - `GET /api/partidos/{id}` - Obtiene un partido por ID
   - `DELETE /api/partidos/{id}` - Elimina un partido

2. **ApiFootballService.java** - Método agregado:
   - `searchTeamsByLeague()` - Busca equipos por liga

### Frontend (HTML)
1. **crear-partido.html** - Página web para crear partidos

---

## 🚀 Cómo Usar

### 1. Ejecutar la Aplicación

```powershell
cd C:\Users\USUARIO\Downloads\proyecto-ACD
.\mvnw.cmd spring-boot:run
```

### 2. Acceder a la Página

Abre tu navegador en:
```
http://localhost:8081/crear-partido.html
```

### 3. Iniciar Sesión

**IMPORTANTE:** Debes haber iniciado sesión primero en:
```
http://localhost:8081/login.html
```

La página verificará automáticamente que tengas un token JWT válido.

---

## 🎮 Funcionalidades de la Página

### ✅ Verificación de Autenticación
- La página verifica automáticamente si estás logueado
- Si no estás autenticado, te redirige a login
- Badge "ADMIN" visible en el título

### 🔍 Selección de Equipos
**Dos dropdowns:**
- 🏠 **Equipo Local** - Selecciona el equipo que juega en casa
- ✈️ **Equipo Visitante** - Selecciona el equipo visitante

**Características:**
- Carga automática de todos los equipos de **La Liga** desde API-Football
- Aproximadamente 20 equipos disponibles (Real Madrid, Barcelona, Atlético, etc.)
- Los equipos se cargan al abrir la página

### 👥 Visualización de Jugadores
**Cuando seleccionas un equipo:**
- Se muestra el **logo** del equipo
- Se muestra el **nombre** del equipo
- Se cargan automáticamente todos los **jugadores** del equipo
- Los jugadores se organizan por posición:
  - 🧤 **Porteros** (Goalkeepers)
  - 🛡️ **Defensas** (Defenders)
  - ⚙️ **Centrocampistas** (Midfielders)
  - ⚡ **Delanteros** (Attackers)

**Información de cada jugador:**
- Número de camiseta
- Nombre completo
- Posición

### 📅 Configuración del Partido
- **Fecha y hora** del partido
- Selector de fecha y hora integrado

### 🏆 Creación del Partido
Botón "Crear Partido" que:
1. Valida que ambos equipos estén seleccionados
2. Valida que no sean el mismo equipo
3. Valida que se haya seleccionado una fecha
4. Crea el partido en la base de datos
5. Muestra mensaje de éxito
6. Pregunta si deseas crear otro partido

---

## 📊 Vista Previa de la Página

```
┌──────────────────────────────────────────────────────┐
│  ⚽ Crear Partido de La Liga [ADMIN]                │
│  Selecciona dos equipos y configura el partido      │
├──────────────────────────────────────────────────────┤
│                                                      │
│  🏠 Equipo Local         ✈️ Equipo Visitante       │
│  [Barcelona ▼]           [Real Madrid ▼]            │
│                                                      │
│  📅 Fecha y Hora del Partido                        │
│  [2026-02-10 20:00]                                 │
│                                                      │
├─────────────────┬────────┬──────────────────────────┤
│                 │        │                          │
│  [Logo Barça]   │   VS   │   [Logo Real Madrid]    │
│   Barcelona     │        │    Real Madrid           │
│                 │        │                          │
│ 🧤 Porteros     │        │  🧤 Porteros            │
│ 1  Ter Stegen   │        │  1  Courtois            │
│                 │        │                          │
│ 🛡️ Defensas     │        │  🛡️ Defensas            │
│ 2  Araujo       │        │  2  Carvajal            │
│ 3  Pique        │        │  3  Militao             │
│ ...             │        │  ...                     │
│                 │        │                          │
│ ⚙️ Centrocampistas      │  ⚙️ Centrocampistas     │
│ 5  Busquets     │        │  8  Kroos               │
│ 8  Pedri        │        │  10 Modric              │
│ ...             │        │  ...                     │
│                 │        │                          │
│ ⚡ Delanteros    │        │  ⚡ Delanteros           │
│ 10 Ansu Fati    │        │  9  Benzema             │
│ ...             │        │  ...                     │
└─────────────────┴────────┴──────────────────────────┘

         [🏆 Crear Partido]
         [← Volver al Inicio]
```

---

## 🔌 Endpoints de la API

### 1. Obtener Equipos de La Liga
```http
GET /api/partidos/equipos-laliga
Authorization: Bearer {token}
```

**Respuesta:**
```json
[
  {
    "team": {
      "id": 529,
      "name": "Barcelona",
      "code": "BAR",
      "logo": "https://media.api-sports.io/football/teams/529.png"
    },
    "venue": {
      "name": "Camp Nou",
      "city": "Barcelona"
    }
  },
  {
    "team": {
      "id": 541,
      "name": "Real Madrid",
      "code": "RMA",
      "logo": "https://media.api-sports.io/football/teams/541.png"
    }
  }
  // ... más equipos
]
```

### 2. Crear Partido
```http
POST /api/partidos/crear
Authorization: Bearer {token}
Content-Type: application/json

{
  "equipoLocalId": 529,
  "equipoVisitanteId": 541,
  "fecha": "2026-02-10T20:00:00"
}
```

**Respuesta:**
```json
{
  "id": 1,
  "equipoLocalId": 529,
  "equipoLocalNombre": "Barcelona",
  "equipoVisitanteId": 541,
  "equipoVisitanteNombre": "Real Madrid",
  "fecha": "2026-02-10T20:00:00",
  "creadoPorId": 1,
  "creadoPorEmail": "admin@futbol.com"
}
```

### 3. Listar Partidos
```http
GET /api/partidos
```

### 4. Eliminar Partido (Solo Admin)
```http
DELETE /api/partidos/{id}
Authorization: Bearer {token}
```

---

## 🗄️ Estructura en Base de Datos

Los partidos se guardan en la tabla `partidos`:

```sql
CREATE TABLE partidos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    equipo_local_id INT NOT NULL,
    equipo_visitante_id INT NOT NULL,
    fecha DATETIME NOT NULL,
    creado_por INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (equipo_local_id) REFERENCES equipos(id),
    FOREIGN KEY (equipo_visitante_id) REFERENCES equipos(id),
    FOREIGN KEY (creado_por) REFERENCES users(id)
);
```

---

## 🎯 Equipos Principales de La Liga

| Equipo | ID | Ciudad |
|--------|----|----- |
| Real Madrid | 541 | Madrid |
| Barcelona | 529 | Barcelona |
| Atlético Madrid | 530 | Madrid |
| Sevilla | 536 | Sevilla |
| Valencia | 532 | Valencia |
| Real Sociedad | 548 | San Sebastián |
| Villarreal | 533 | Villarreal |
| Athletic Bilbao | 531 | Bilbao |
| Real Betis | 543 | Sevilla |
| Getafe | 546 | Getafe |

---

## 🛠️ Validaciones Implementadas

### Frontend (JavaScript)
- ✅ Verifica que el usuario esté logueado
- ✅ Verifica que ambos equipos estén seleccionados
- ✅ Verifica que no sean el mismo equipo
- ✅ Verifica que se haya seleccionado una fecha

### Backend (Java)
- ✅ Solo usuarios ADMIN pueden crear partidos (`@PreAuthorize("hasRole('ADMIN')")`)
- ✅ Verifica que los equipos existan en la BD
- ✅ Verifica que no sean el mismo equipo
- ✅ Manejo de errores completo

---

## 🔐 Seguridad

- **Autenticación JWT** requerida
- **Verificación de rol ADMIN** en el backend
- **Token almacenado** en localStorage del navegador
- Redirección automática a login si no está autenticado

---

## 🧪 Probar la Funcionalidad

### Paso 1: Iniciar la Aplicación
```powershell
.\mvnw.cmd spring-boot:run
```

### Paso 2: Registrar/Login como Admin
1. Ve a: http://localhost:8081/register.html
2. Regístrate con un email
3. Ve a: http://localhost:8081/login.html
4. Inicia sesión

**Nota:** Para ser ADMIN, necesitas modificar el rol en la base de datos:
```sql
UPDATE users SET role = 'ADMIN' WHERE email = 'tu@email.com';
```

### Paso 3: Crear Partido
1. Ve a: http://localhost:8081/crear-partido.html
2. Espera a que carguen los equipos
3. Selecciona Barcelona como local
4. Selecciona Real Madrid como visitante
5. Observa cómo se cargan los jugadores
6. Selecciona fecha y hora
7. Click en "Crear Partido"

---

## 📝 Notas Importantes

### API-Football
- Los equipos se cargan desde la API de API-Football
- Se usa **La Liga (ID: 140)** de la temporada **2024**
- Los jugadores también vienen de la API
- Respeta el límite de 100 requests/día del plan gratuito

### Performance
- Los equipos se cargan **una vez** al abrir la página
- Los jugadores se cargan **solo cuando seleccionas un equipo**
- Indicadores de carga visibles

### Limitaciones
- Solo muestra equipos de **La Liga**
- Si quieres otras ligas, modifica el ID en `crear-partido.html`:
  - Premier League: 39
  - Serie A: 135
  - Bundesliga: 78

---

## 🎨 Características de Diseño

- ✅ Diseño responsive (funciona en móviles)
- ✅ Gradientes modernos
- ✅ Animaciones suaves
- ✅ Spinners de carga
- ✅ Alertas coloridas (éxito, error, warning)
- ✅ Scroll personalizado
- ✅ Layout en grid para comparar equipos
- ✅ Organización de jugadores por posición

---

## 🚀 Estado del Proyecto

```
✅ Compilación:        BUILD SUCCESS
✅ Controlador:        PartidoController creado
✅ Endpoint Liga:      /api/partidos/equipos-laliga
✅ Endpoint Crear:     /api/partidos/crear
✅ Página HTML:        crear-partido.html
✅ Integración API:    API-Football La Liga
✅ Seguridad:          JWT + ADMIN role
✅ Jugadores:          Carga automática
✅ Validaciones:       Frontend + Backend
```

---

**¡Tu página para crear partidos está lista! ⚽🏆**

Ahora los administradores pueden crear partidos entre equipos de La Liga y ver todos los jugadores de ambos equipos antes de confirmar.
