# 📋 CÓMO FUNCIONAN LAS ALINEACIONES EN LA APLICACIÓN

## 📅 Fecha: 10 Febrero 2026

---

## 🎯 ¿QUÉ ES UNA ALINEACIÓN?

Una **alineación** es la lista de jugadores que un usuario elige para un equipo en un partido específico. Es como cuando un entrenador de fútbol decide quién juega y en qué posición.

En nuestra aplicación, los usuarios pueden crear sus propias alineaciones para los equipos de La Liga y competir con otros usuarios para ver quién hace la mejor alineación.

---

## 🔄 FLUJO COMPLETO: DE INICIO A FIN

### 1️⃣ **USUARIO INICIA SESIÓN**
```
Usuario → Login → Sistema verifica credenciales → Token JWT guardado
```

**Qué pasa aquí:**
- El usuario introduce su email y contraseña
- El sistema verifica que sean correctos
- Si todo está bien, el sistema da un "token" (como un pase VIP) que permite crear alineaciones

---

### 2️⃣ **USUARIO VA A "CREAR ALINEACIÓN"**
```
Usuario → Hacer clic en "Crear Alineación" → Página se carga
```

**Qué pasa aquí:**
- Se verifica que el usuario tenga su "token" (pase VIP)
- Si no lo tiene, se le redirige al login
- Si lo tiene, puede continuar

---

### 3️⃣ **SISTEMA CARGA LOS PARTIDOS DISPONIBLES**

```javascript
// El sistema consulta la base de datos
Consultar BD → SELECT * FROM partidos WHERE fecha > HOY
              → Devuelve lista de partidos futuros
```

**Qué pasa aquí:**
- El sistema busca en la base de datos todos los partidos que AÚN NO SE HAN JUGADO
- Muestra solo partidos del futuro (para que tenga sentido crear una alineación)
- Los partidos fueron creados previamente por un administrador

**Ejemplo de partido:**
```
ID: 1
Equipo Local: Real Betis (ID: 543)
Equipo Visitante: FC Barcelona (ID: 529)
Fecha: 15 Febrero 2026, 21:00
```

---

### 4️⃣ **USUARIO SELECCIONA UN PARTIDO**

```
Usuario → Selecciona "Real Betis vs FC Barcelona"
        → Sistema muestra las dos opciones de equipo
```

**Qué pasa aquí:**
- El usuario elige el partido que le interesa
- El sistema le pregunta: "¿Para qué equipo quieres crear la alineación?"
  - Opción A: Real Betis
  - Opción B: FC Barcelona

---

### 5️⃣ **USUARIO ELIGE UN EQUIPO**

```
Usuario → Selecciona "Real Betis"
        → Sistema guarda: partidoId=1, equipoId=543
```

**Qué pasa aquí:**
- El usuario elige para qué equipo va a hacer la alineación
- El sistema guarda esta información para usarla después

---

### 6️⃣ **USUARIO DEFINE LA FORMACIÓN**

```
Usuario → Elige formación táctica
        → Ejemplo: 1-4-3-3
          (1 portero, 4 defensas, 3 centrocampistas, 3 delanteros)
```

**Qué pasa aquí:**
- El usuario decide cuántos jugadores quiere en cada posición
- Debe sumar exactamente 11 jugadores
- Ejemplo de formaciones populares:
  - 1-4-4-2 (equilibrada)
  - 1-4-3-3 (ofensiva)
  - 1-5-3-2 (defensiva)

---

### 7️⃣ **SISTEMA OBTIENE LOS JUGADORES DEL EQUIPO (API-FOOTBALL)**

Aquí es donde viene la **MAGIA** ✨

#### **Paso A: Buscar el equipo en API-Football**

```javascript
// 1. Sistema busca el equipo por nombre
Consultar API → GET https://v3.football.api-sports.io/teams?name=Real+Betis
              → Respuesta: { id: 543, name: "Real Betis", ... }
```

**Qué pasa:**
- El sistema envía el nombre del equipo a API-Football
- API-Football devuelve información del equipo, incluyendo su ID

---

#### **Paso B: Obtener la plantilla completa del equipo**

```javascript
// 2. Sistema obtiene todos los jugadores del equipo (plantilla/squad)
Consultar API → GET https://v3.football.api-sports.io/players/squads?team=543
              → Respuesta: Lista de 25-30 jugadores con sus datos
```

**Qué devuelve la API:**
```json
{
  "response": [
    {
      "team": {
        "id": 543,
        "name": "Real Betis",
        "logo": "https://..."
      },
      "players": [
        {
          "id": 306,
          "name": "Claudio Bravo",
          "age": 40,
          "number": 1,
          "position": "Goalkeeper",
          "photo": "https://..."
        },
        {
          "id": 1234,
          "name": "Marc Bartra",
          "age": 32,
          "number": 15,
          "position": "Defender",
          "photo": "https://..."
        }
        // ... más jugadores ...
      ]
    }
  ]
}
```

**Qué información obtiene el sistema:**
- ✅ ID del jugador (único)
- ✅ Nombre completo
- ✅ Número de camiseta
- ✅ Posición (Goalkeeper, Defender, Midfielder, Attacker)
- ✅ Edad del jugador
- ✅ Foto del jugador

---

#### **Paso C: Sistema procesa y organiza los jugadores**

```javascript
// 3. Sistema clasifica jugadores por posición
// La respuesta de /players/squads tiene una estructura más simple
const squad = response.response[0]; // Primer elemento es el equipo con sus jugadores
const jugadores = squad.players;

jugadores.forEach(jugador => {
    if (jugador.position === "Goalkeeper") {
        porteros.push(jugador);
    } else if (jugador.position === "Defender") {
        defensas.push(jugador);
    } else if (jugador.position === "Midfielder") {
        centrocampistas.push(jugador);
    } else if (jugador.position === "Attacker") {
        delanteros.push(jugador);
    }
});
```

**Resultado:**
```
Porteros: [Claudio Bravo, Rui Silva]
Defensas: [Marc Bartra, Álex Moreno, Héctor Bellerín, ...]
Centrocampistas: [Sergio Canales, Guido Rodríguez, ...]
Delanteros: [Borja Iglesias, Willian José, ...]
```

---

### 8️⃣ **SISTEMA MUESTRA LOS JUGADORES AL USUARIO**

```
Sistema → Crea dropdowns (listas desplegables) por posición
        → Usuario puede seleccionar de cada lista
```

**Interfaz mostrada:**

```
╔════════════════════════════════════════════╗
║  🧤 PORTEROS                               ║
║  Portero 1: [ Claudio Bravo (#1) ] ▼      ║
╠════════════════════════════════════════════╣
║  🛡️ DEFENSAS                               ║
║  Defensa 1: [ Marc Bartra (#15) ] ▼       ║
║  Defensa 2: [ Álex Moreno (#12) ] ▼       ║
║  Defensa 3: [ Héctor Bellerín (#2) ] ▼    ║
║  Defensa 4: [ Germán Pezzella (#6) ] ▼    ║
╠════════════════════════════════════════════╣
║  ⚙️ CENTROCAMPISTAS                        ║
║  Centro 1: [ Sergio Canales (#10) ] ▼     ║
║  Centro 2: [ Guido Rodríguez (#21) ] ▼    ║
║  Centro 3: [ Nabil Fekir (#8) ] ▼         ║
╠════════════════════════════════════════════╣
║  ⚡ DELANTEROS                              ║
║  Delan 1: [ Borja Iglesias (#9) ] ▼       ║
║  Delan 2: [ Willian José (#12) ] ▼        ║
║  Delan 3: [ Luiz Henrique (#17) ] ▼       ║
╠════════════════════════════════════════════╣
║  🔄 SUPLENTES (Opcional)                   ║
║  Suplente 1: [ Rui Silva (#13) ] ▼        ║
║  Suplente 2: [ Juan Miranda (#3) ] ▼      ║
║  Suplente 3: [-- Opcional --] ▼           ║
║  Suplente 4: [-- Opcional --] ▼           ║
║  Suplente 5: [-- Opcional --] ▼           ║
╚════════════════════════════════════════════╝
```

---

### 9️⃣ **USUARIO SELECCIONA LOS JUGADORES**

```
Usuario → Elige 11 jugadores titulares (obligatorio)
        → Elige 0-5 suplentes (opcional)
        → Hace clic en "Guardar Alineación"
```

**Sistema valida:**
- ✅ ¿Son exactamente 11 titulares? → Sí
- ✅ ¿Hay jugadores repetidos? → No
- ✅ ¿Algún suplente es titular? → No
- ✅ ¿Hay suplentes repetidos? → No

---

### 🔟 **SISTEMA GUARDA LA ALINEACIÓN EN LA BASE DE DATOS**

```javascript
// Sistema construye el objeto de alineación
const alineacionData = {
    partidoId: 1,  // Real Betis vs FC Barcelona
    equipoId: 543, // Real Betis
    alineacion: {
        formacion: "1-4-3-3",
        titulares: [
            { idJugador: 306, nombre: "Claudio Bravo", numero: 1, posicion: "Goalkeeper" },
            { idJugador: 1234, nombre: "Marc Bartra", numero: 15, posicion: "Defender" },
            { idJugador: 5678, nombre: "Álex Moreno", numero: 12, posicion: "Defender" },
            // ... 8 jugadores más (total 11)
        ],
        suplentes: [
            { idJugador: 9999, nombre: "Rui Silva", numero: 13, posicion: "Goalkeeper" },
            { idJugador: 8888, nombre: "Juan Miranda", numero: 3, posicion: "Defender" }
        ]
    }
};

// Sistema envía al backend
POST /api/alineaciones
Body: alineacionData
```

**Backend recibe y guarda en la base de datos:**

```sql
INSERT INTO alineaciones (
    partido_id, 
    equipo_id, 
    created_by, 
    alineacion, 
    created_at
) VALUES (
    1,                    -- ID del partido
    543,                  -- ID del equipo (Real Betis)
    7,                    -- ID del usuario que la creó
    '{"formacion":"1-4-3-3","titulares":[...],"suplentes":[...]}',  -- JSON
    '2026-02-10 18:30:00' -- Fecha de creación
);
```

---

### 1️⃣1️⃣ **ALINEACIÓN GUARDADA - ÉXITO**

```
Sistema → Muestra mensaje: "✅ Alineación guardada correctamente"
        → Usuario puede ver su alineación en "Mis Alineaciones"
        → Otros usuarios pueden verla y votarla en "Ver Alineaciones"
```

---

## 📊 DIAGRAMA VISUAL DEL FLUJO COMPLETO

```
┌─────────────────────────────────────────────────────────────────┐
│  USUARIO                                                        │
└────────┬────────────────────────────────────────────────────────┘
         │
         │ 1. Inicia sesión
         ▼
┌─────────────────────────────────────────────────────────────────┐
│  SISTEMA (Frontend)                                             │
│  - Verifica token                                               │
│  - Carga página crear-alineacion.html                           │
└────────┬────────────────────────────────────────────────────────┘
         │
         │ 2. Consulta partidos disponibles
         ▼
┌─────────────────────────────────────────────────────────────────┐
│  BASE DE DATOS (MySQL)                                          │
│  SELECT * FROM partidos WHERE fecha > NOW()                     │
│  DEVUELVE: [Partido1, Partido2, Partido3, ...]                 │
└────────┬────────────────────────────────────────────────────────┘
         │
         │ 3. Muestra partidos al usuario
         ▼
┌─────────────────────────────────────────────────────────────────┐
│  USUARIO                                                        │
│  - Selecciona partido: "Real Betis vs FC Barcelona"             │
│  - Selecciona equipo: "Real Betis"                              │
│  - Define formación: "1-4-3-3"                                  │
└────────┬────────────────────────────────────────────────────────┘
         │
         │ 4. Sistema necesita jugadores del Real Betis
         ▼
┌─────────────────────────────────────────────────────────────────┐
│  API-FOOTBALL (Servicio externo)                                │
│  GET /teams?name=Real+Betis                                     │
│  DEVUELVE: { id: 543, name: "Real Betis", ... }                 │
│                                                                 │
│  GET /players/squads?team=543                                   │
│  DEVUELVE:                                                      │
│  {                                                              │
│    response: [{                                                 │
│      team: { id: 543, name: "Real Betis" },                     │
│      players: [                                                 │
│        { id:306, name:"Claudio Bravo", number:1, pos:"GK" },    │
│        { id:1234, name:"Marc Bartra", number:15, pos:"DEF" },   │
│        ... (25-30 jugadores total)                              │
│      ]                                                          │
│    }]                                                           │
│  }                                                              │
└────────┬────────────────────────────────────────────────────────┘
         │
         │ 5. Sistema procesa jugadores
         ▼
┌─────────────────────────────────────────────────────────────────┐
│  SISTEMA (Frontend)                                             │
│  - Clasifica por posición: Porteros, Defensas, Centros, Delant │
│  - Crea dropdowns para cada posición                            │
│  - Muestra jugadores al usuario                                 │
└────────┬────────────────────────────────────────────────────────┘
         │
         │ 6. Usuario selecciona jugadores
         ▼
┌─────────────────────────────────────────────────────────────────┐
│  USUARIO                                                        │
│  - Elige 11 titulares                                           │
│  - Elige 2 suplentes (opcional)                                 │
│  - Hace clic en "Guardar Alineación"                            │
└────────┬────────────────────────────────────────────────────────┘
         │
         │ 7. Sistema valida y envía al backend
         ▼
┌─────────────────────────────────────────────────────────────────┐
│  BACKEND (Spring Boot - Java)                                   │
│  POST /api/alineaciones                                         │
│  - Valida datos                                                 │
│  - Verifica token JWT                                           │
│  - Comprueba que no exista otra alineación del mismo usuario    │
│    para el mismo equipo en el mismo partido                     │
└────────┬────────────────────────────────────────────────────────┘
         │
         │ 8. Backend guarda en base de datos
         ▼
┌─────────────────────────────────────────────────────────────────┐
│  BASE DE DATOS (MySQL)                                          │
│  INSERT INTO alineaciones (                                     │
│    partido_id = 1,                                              │
│    equipo_id = 543,                                             │
│    created_by = 7,                                              │
│    alineacion = '{"formacion":"1-4-3-3",...}'                   │
│  )                                                              │
│  RESULTADO: Alineación guardada con ID = 42                     │
└────────┬────────────────────────────────────────────────────────┘
         │
         │ 9. Respuesta de éxito
         ▼
┌─────────────────────────────────────────────────────────────────┐
│  USUARIO                                                        │
│  ✅ "Alineación guardada correctamente"                         │
│  → Puede verla en "Mis Alineaciones"                            │
│  → Otros usuarios pueden votarla                                │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🔑 CONCEPTOS CLAVE

### 1. **API-Football**
- Es un servicio externo que proporciona datos de fútbol en tiempo real
- Tiene información de equipos, jugadores, partidos, ligas, etc.
- Nosotros lo usamos para obtener la plantilla de jugadores de cada equipo
- **URL:** https://v3.football.api-sports.io

### 2. **Endpoints usados**

#### A) Buscar equipo por nombre
```
GET /teams?name={nombre_equipo}

Ejemplo:
GET /teams?name=Real+Betis

Respuesta:
{
  "response": [
    {
      "team": {
        "id": 543,
        "name": "Real Betis",
        "code": "BET",
        "country": "Spain",
        "founded": 1907,
        "logo": "https://..."
      }
    }
  ]
}
```

#### B) Obtener jugadores de un equipo (plantilla/squad)
```
GET /players/squads?team={team_id}

Ejemplo:
GET /players/squads?team=543

Respuesta:
{
  "response": [
    {
      "team": {
        "id": 543,
        "name": "Real Betis",
        "logo": "https://..."
      },
      "players": [
        {
          "id": 306,
          "name": "Claudio Bravo",
          "age": 40,
          "number": 1,
          "position": "Goalkeeper",
          "photo": "https://..."
        },
        {
          "id": 1234,
          "name": "Marc Bartra",
          "age": 32,
          "number": 15,
          "position": "Defender",
          "photo": "https://..."
        }
        // ... más jugadores
      ]
    }
  ]
}
```

---

## 🎯 RESUMEN EN 10 PUNTOS

1. **Usuario inicia sesión** → Obtiene token (pase VIP)

2. **Sistema carga partidos** → Consulta base de datos (partidos futuros)

3. **Usuario elige partido** → Ejemplo: "Real Betis vs FC Barcelona"

4. **Usuario elige equipo** → Ejemplo: "Real Betis"

5. **Usuario define formación** → Ejemplo: "1-4-3-3" (11 jugadores)

6. **Sistema consulta API-Football** → Busca equipo por nombre

7. **Sistema obtiene jugadores** → API devuelve plantilla completa (25-30 jugadores)

8. **Sistema organiza jugadores** → Por posición (Porteros, Defensas, etc.)

9. **Usuario selecciona jugadores** → 11 titulares + 0-5 suplentes

10. **Sistema guarda en BD** → Alineación guardada con éxito ✅

---

## 📝 EJEMPLO COMPLETO PASO A PASO

### Escenario: Juan quiere crear una alineación del Real Betis

**PASO 1: Login**
```
Juan → Email: juan@email.com, Password: *****
     → Sistema: ✅ Credenciales correctas
     → Token guardado: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**PASO 2: Ir a Crear Alineación**
```
Juan → Hace clic en "Crear Alineación"
     → Sistema verifica token → ✅ Válido
     → Página se carga
```

**PASO 3: Sistema carga partidos**
```
Sistema → SELECT * FROM partidos WHERE fecha > '2026-02-10'
        → Resultado:
          [
            { id: 1, local: "Real Betis", visitante: "FC Barcelona", fecha: "2026-02-15 21:00" },
            { id: 2, local: "Real Madrid", visitante: "Atlético Madrid", fecha: "2026-02-16 18:30" }
          ]
```

**PASO 4: Juan selecciona partido**
```
Juan → Selecciona: "Real Betis vs FC Barcelona (15 Feb, 21:00)"
     → Sistema guarda: partidoId = 1
```

**PASO 5: Juan elige equipo**
```
Juan → Selecciona: "Real Betis"
     → Sistema guarda: equipoId = 543
```

**PASO 6: Juan define formación**
```
Juan → Elige: 1 portero, 4 defensas, 3 centrocampistas, 3 delanteros
     → Sistema guarda: formacion = "1-4-3-3"
```

**PASO 7: Sistema obtiene jugadores**
```
Sistema → API Request 1:
          GET https://v3.football.api-sports.io/teams?name=Real+Betis
          Headers: { "x-apisports-key": "tu_api_key" }
          
        → API Response 1:
          { response: [{ team: { id: 543, name: "Real Betis" } }] }
          
        → API Request 2:
          GET https://v3.football.api-sports.io/players/squads?team=543
          Headers: { "x-apisports-key": "tu_api_key" }
          
        → API Response 2:
          {
            response: [
              {
                team: { id: 543, name: "Real Betis" },
                players: [
                  { id: 306, name: "Claudio Bravo", number: 1, position: "Goalkeeper" },
                  { id: 1234, name: "Marc Bartra", number: 15, position: "Defender" },
                  // ... 23 jugadores más
                ]
              }
            ]
          }
```

**PASO 8: Sistema organiza y muestra**
```
Sistema → Procesa respuesta:
          
          Porteros: [
            { id: 306, name: "Claudio Bravo", number: 1 },
            { id: 789, name: "Rui Silva", number: 13 }
          ]
          
          Defensas: [
            { id: 1234, name: "Marc Bartra", number: 15 },
            { id: 5678, name: "Álex Moreno", number: 12 },
            // ...
          ]
          
          // ... resto de posiciones
          
        → Crea dropdowns y los muestra a Juan
```

**PASO 9: Juan selecciona jugadores**
```
Juan → Portero 1: Claudio Bravo
     → Defensa 1: Marc Bartra
     → Defensa 2: Álex Moreno
     → Defensa 3: Héctor Bellerín
     → Defensa 4: Germán Pezzella
     → Centro 1: Sergio Canales
     → Centro 2: Guido Rodríguez
     → Centro 3: Nabil Fekir
     → Delantero 1: Borja Iglesias
     → Delantero 2: Willian José
     → Delantero 3: Luiz Henrique
     
     → Suplente 1: Rui Silva
     → Suplente 2: Juan Miranda
     
     → Hace clic en "Guardar Alineación"
```

**PASO 10: Sistema valida**
```
Sistema → ✅ 11 titulares seleccionados
        → ✅ No hay duplicados
        → ✅ Suplentes no son titulares
        → ✅ Todo correcto
```

**PASO 11: Sistema envía al backend**
```
Sistema → POST http://localhost:8081/api/alineaciones
          Headers: {
            "Authorization": "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
            "Content-Type": "application/json"
          }
          Body: {
            "partidoId": 1,
            "equipoId": 543,
            "alineacion": {
              "formacion": "1-4-3-3",
              "titulares": [
                { "idJugador": 306, "nombre": "Claudio Bravo", "numero": 1, "posicion": "Goalkeeper" },
                { "idJugador": 1234, "nombre": "Marc Bartra", "numero": 15, "posicion": "Defender" },
                // ... resto de titulares
              ],
              "suplentes": [
                { "idJugador": 789, "nombre": "Rui Silva", "numero": 13, "posicion": "Goalkeeper" },
                { "idJugador": 888, "nombre": "Juan Miranda", "numero": 3, "posicion": "Defender" }
              ]
            }
          }
```

**PASO 12: Backend guarda**
```
Backend → Verifica token JWT → ✅ Válido (usuario ID = 7)
        → Verifica que Juan no tenga otra alineación para:
          - Partido ID 1
          - Equipo ID 543
        → ✅ No existe
        
        → INSERT INTO alineaciones (
            partido_id,
            equipo_id,
            created_by,
            alineacion,
            created_at,
            votos
          ) VALUES (
            1,
            543,
            7,  -- ID de Juan
            '{"formacion":"1-4-3-3","titulares":[...],"suplentes":[...]}',
            '2026-02-10 18:45:30',
            0
          );
          
        → Alineación guardada con ID = 42
```

**PASO 13: Éxito**
```
Backend → Respuesta: { "id": 42, "message": "Alineación guardada correctamente" }
Sistema → Muestra: "✅ Alineación guardada correctamente"
Juan → Ve su alineación en "Mis Alineaciones"
Otros usuarios → Pueden verla y votarla en "Ver Alineaciones"
```

---

## 🎨 REPRESENTACIÓN VISUAL DE LOS DATOS

### Datos en la Base de Datos:

```sql
-- Tabla: alineaciones
+----+------------+-----------+------------+----------+---------------------+
| id | partido_id | equipo_id | created_by | votos    | created_at          |
+----+------------+-----------+------------+----------+---------------------+
| 42 | 1          | 543       | 7          | 0        | 2026-02-10 18:45:30 |
+----+------------+-----------+------------+----------+---------------------+

-- Campo JSON: alineacion
{
  "formacion": "1-4-3-3",
  "titulares": [
    { "idJugador": 306, "nombre": "Claudio Bravo", "numero": 1, "posicion": "Goalkeeper" },
    { "idJugador": 1234, "nombre": "Marc Bartra", "numero": 15, "posicion": "Defender" },
    { "idJugador": 5678, "nombre": "Álex Moreno", "numero": 12, "posicion": "Defender" },
    { "idJugador": 9012, "nombre": "Héctor Bellerín", "numero": 2, "posicion": "Defender" },
    { "idJugador": 3456, "nombre": "Germán Pezzella", "numero": 6, "posicion": "Defender" },
    { "idJugador": 7890, "nombre": "Sergio Canales", "numero": 10, "posicion": "Midfielder" },
    { "idJugador": 1122, "nombre": "Guido Rodríguez", "numero": 21, "posicion": "Midfielder" },
    { "idJugador": 3344, "nombre": "Nabil Fekir", "numero": 8, "posicion": "Midfielder" },
    { "idJugador": 5566, "nombre": "Borja Iglesias", "numero": 9, "posicion": "Attacker" },
    { "idJugador": 7788, "nombre": "Willian José", "numero": 12, "posicion": "Attacker" },
    { "idJugador": 9900, "nombre": "Luiz Henrique", "numero": 17, "posicion": "Attacker" }
  ],
  "suplentes": [
    { "idJugador": 789, "nombre": "Rui Silva", "numero": 13, "posicion": "Goalkeeper" },
    { "idJugador": 888, "nombre": "Juan Miranda", "numero": 3, "posicion": "Defender" }
  ]
}
```

---

## ❓ PREGUNTAS FRECUENTES

### ❓ ¿De dónde salen los jugadores?
**R:** De API-Football, un servicio externo que tiene información actualizada de todos los equipos de fútbol del mundo.

### ❓ ¿Por qué necesitamos API-Football?
**R:** Porque mantener una base de datos propia con todos los jugadores de todos los equipos sería muy complicado. API-Football ya tiene esa información actualizada.

### ❓ ¿Qué pasa si API-Football no funciona?
**R:** La aplicación mostraría un error al intentar cargar los jugadores. El usuario no podría crear la alineación hasta que la API vuelva a funcionar.

### ❓ ¿Los jugadores se guardan en nuestra base de datos?
**R:** Sí y no. Guardamos el ID, nombre, número y posición de los jugadores seleccionados en el JSON de la alineación, pero no guardamos la plantilla completa de todos los equipos.

### ❓ ¿Puedo crear varias alineaciones para el mismo equipo en el mismo partido?
**R:** No. La base de datos tiene una restricción: un usuario solo puede crear UNA alineación por equipo por partido. Esto evita que alguien haga spam.

### ❓ ¿Qué es el campo "votos"?
**R:** Es un contador que guarda cuántos usuarios han votado positivamente por esa alineación. Otros usuarios pueden ver las alineaciones y votar por las que les gustan más.

---

## 🔐 SEGURIDAD

### Token JWT (JSON Web Token)
- Cuando el usuario inicia sesión, recibe un token
- Este token debe enviarse en todas las peticiones que requieren autenticación
- Si el token no es válido o ha expirado, el sistema rechaza la petición
- El token contiene el ID del usuario, por eso el sistema sabe quién creó cada alineación

### Restricción en Base de Datos
```sql
UNIQUE (partido_id, equipo_id, created_by)
```
Esto significa que:
- Usuario 7 puede crear 1 alineación para Betis en partido 1
- Usuario 7 puede crear 1 alineación para Barcelona en partido 1
- Usuario 8 puede crear 1 alineación para Betis en partido 1
- Pero usuario 7 NO puede crear 2 alineaciones para Betis en partido 1

---

## 📚 RESUMEN TÉCNICO (Para Desarrolladores)

### Tecnologías usadas:
- **Frontend:** HTML, CSS, JavaScript (Vanilla)
- **Backend:** Spring Boot (Java)
- **Base de datos:** MySQL
- **API externa:** API-Football (v3.football.api-sports.io)
- **Autenticación:** JWT (JSON Web Tokens)

### Endpoints principales:

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/auth/login` | Iniciar sesión |
| GET | `/api/partidos/api-football/scheduled` | Obtener partidos futuros |
| POST | `/api/alineaciones` | Guardar alineación |
| GET | `/api/alineaciones/user` | Obtener alineaciones del usuario |
| GET | `/api/alineaciones/partido/{id}` | Ver alineaciones de un partido |
| POST | `/api/alineaciones/{id}/votar` | Votar por una alineación |

### Flujo de datos:
```
Usuario → Frontend → Backend → Base de Datos
                  ↓
                  ↓ (cuando necesita jugadores)
                  ↓
              API-Football
```

---

## ✅ CONCLUSIÓN

El sistema de alineaciones funciona de manera sencilla:

1. **Usuario se autentica** (login)
2. **Elige un partido y equipo** (de la base de datos)
3. **Sistema obtiene jugadores** (de API-Football)
4. **Usuario selecciona 11 titulares + suplentes**
5. **Sistema guarda** (en la base de datos)
6. **¡Listo!** La alineación está creada

La clave está en **API-Football**, que nos proporciona información actualizada de todos los jugadores de La Liga sin tener que mantener nuestra propia base de datos de jugadores.

---

**Documento creado el:** 10 de Febrero de 2026  
**Versión:** 1.0  
**Autor:** Sistema de Documentación Automática

