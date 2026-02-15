# 🔧 DEBUGGING - Jugadores No Aparecen en Dropdowns

## ⚠️ PASOS OBLIGATORIOS PARA RESOLVER

### 1. LIMPIAR CACHÉ DEL NAVEGADOR (CRÍTICO)

El navegador está cargando la versión antigua del archivo. **DEBES hacer esto:**

#### Opción A: Hard Reload (Más Rápido)
1. Abre la página: `http://localhost:8081/crear-alineacion.html`
2. Presiona: **`Ctrl + Shift + R`** (Windows)
3. O: **`Ctrl + F5`**

#### Opción B: Limpiar Caché (Más Seguro)
1. Presiona **`F12`** para abrir Developer Tools
2. Click derecho en el botón **Reload** (junto a la barra de direcciones)
3. Selecciona: **"Empty Cache and Hard Reload"**

#### Opción C: Deshabilitar Caché Temporalmente
1. Presiona **`F12`**
2. Ve a la pestaña **Network**
3. Marca la casilla **"Disable cache"**
4. Recarga la página **`F5`**

---

### 2. VERIFICAR QUE EL SERVIDOR ESTÉ CORRIENDO

```bash
# Si el servidor NO está corriendo, inícialo:
.\mvnw.cmd spring-boot:run
```

**Espera a ver este mensaje:**
```
Started ProyectoAcdApplication in X.XXX seconds
```

---

### 3. ABRIR CONSOLA DEL NAVEGADOR Y SEGUIR PASOS

1. **Abre Developer Tools:**
   - Presiona **`F12`**
   - O click derecho → **Inspeccionar**

2. **Ve a la pestaña Console:**
   - Debería estar vacía o con pocos mensajes

3. **Accede a la página:**
   ```
   http://localhost:8081/crear-alineacion.html
   ```

4. **Sigue los pasos normalmente:**
   - Paso 1: Selecciona un partido
   - Paso 2: Elige un equipo (ej: Barcelona)
   - Paso 3: Define formación (ej: 1-4-3-3)
   - Paso 4: Haz click en "Siguiente →"

5. **OBSERVA LA CONSOLA:**

---

## 📊 QUÉ DEBERÍAS VER EN LA CONSOLA

### Si Funciona Correctamente:

```javascript
Buscando equipo: Barcelona
Equipos encontrados: [{team: {id: 529, name: "Barcelona"}}]
ID de API-Football: 529
Jugadores cargados: 25

Estructura del primer jugador: {
  "player": {
    "id": 1,
    "name": "Marc-André ter Stegen",
    ...
  },
  "statistics": [
    {
      "position": "Goalkeeper",
      "games": { "number": 1 }
    }
  ]
}

Posiciones encontradas: ["Goalkeeper", "Defender", "Midfielder", "Attacker"]

=== INICIANDO mostrarSelectoresJugadores ===
Total de jugadores disponibles: 25
Primeros 3 jugadores (estructura completa):
Jugador 1: {nombre: "Marc-André ter Stegen", tieneStatistics: true, posicion: "Goalkeeper", numero: 1}
Jugador 2: {nombre: "Ronald Araújo", tieneStatistics: true, posicion: "Defender", numero: 4}
Jugador 3: {nombre: "Pedri", tieneStatistics: true, posicion: "Midfielder", numero: 8}

--- Procesando posición: Portero (necesita 1 jugadores) ---
✓ Marc-André ter Stegen → Goalkeeper (coincide con Goalkeeper)
✓ Iñaki Peña → Goalkeeper (coincide con Goalkeeper)
Total jugadores de Portero: 2

--- Procesando posición: Defensa (necesita 4 jugadores) ---
✓ Ronald Araújo → Defender (coincide con Defender)
✓ Jules Koundé → Defender (coincide con Defender)
...
Total jugadores de Defensa: 7

--- Procesando posición: Centrocampista (necesita 3 jugadores) ---
✓ Pedri → Midfielder (coincide con Midfielder)
✓ Gavi → Midfielder (coincide con Midfielder)
...
Total jugadores de Centrocampista: 9

--- Procesando posición: Delantero (necesita 3 jugadores) ---
✓ Robert Lewandowski → Attacker (coincide con Attacker)
✓ Raphinha → Attacker (coincide con Attacker)
...
Total jugadores de Delantero: 7

=== FIN mostrarSelectoresJugadores ===
```

### Si NO Funciona (Posibles Errores):

#### Error 1: No hay jugadores cargados
```javascript
Jugadores cargados: 0
Total de jugadores disponibles: 0
```
**Causa:** La API no devolvió jugadores o hay error en la API key.

#### Error 2: Jugadores sin statistics
```javascript
Jugadores cargados: 25
Primeros 3 jugadores (estructura completa):
Jugador 1: {nombre: "...", tieneStatistics: false, posicion: undefined}
```
**Causa:** La estructura de datos es diferente a la esperada.

#### Error 3: Posiciones no coinciden
```javascript
Total jugadores de Portero: 0
Total jugadores de Defensa: 0
Total jugadores de Centrocampista: 0
Total jugadores de Delantero: 0
```
**Causa:** Los valores de `position` no coinciden con el mapeo.

---

## 🎯 ACCIONES SEGÚN LO QUE VEAS

### CASO 1: No ves NINGÚN log en consola

**Problema:** El navegador está usando la versión en caché.

**Solución:**
1. Presiona **`Ctrl + Shift + R`** (hard reload)
2. O borra el caché del navegador
3. Recarga la página

---

### CASO 2: Ves "Jugadores cargados: 0"

**Problema:** La API no devuelve jugadores.

**Solución:**
1. Verifica que tu API key sea válida: `api.football.key=patatatata` en `application.properties`
2. Prueba con otro equipo
3. Mira si hay errores de red en la pestaña Network

---

### CASO 3: Ves jugadores cargados pero "Total jugadores de [posición]: 0" en TODAS

**Problema:** El campo `statistics[0].position` está vacío o tiene valores diferentes.

**Solución:**
1. Copia el log "Estructura del primer jugador" completo
2. Mándame ese JSON completo
3. Verificaremos la estructura real de datos

---

### CASO 4: Ves "tieneStatistics: false"

**Problema:** Los datos de la API no tienen el campo statistics.

**Solución:**
1. Puede ser que el endpoint esté devolviendo datos diferentes
2. Copia el JSON completo de "Estructura del primer jugador"
3. Necesitaremos adaptar el código a la estructura real

---

## 📋 CHECKLIST COMPLETO

Marca cada paso que completes:

- [ ] Servidor ejecutándose (`.\mvnw.cmd spring-boot:run`)
- [ ] Caché del navegador limpiada (`Ctrl + Shift + R`)
- [ ] Consola abierta (F12)
- [ ] Accedido a `http://localhost:8081/crear-alineacion.html`
- [ ] Paso 1 completado (partido seleccionado)
- [ ] Paso 2 completado (equipo seleccionado)
- [ ] Paso 3 completado (formación definida)
- [ ] Click en "Siguiente →" al Paso 4
- [ ] Observado logs en consola

---

## 🚨 SI SIGUE SIN FUNCIONAR

**Envíame EXACTAMENTE esto de la consola:**

1. Todo el contenido del log "Estructura del primer jugador"
2. El mensaje "Jugadores cargados: X"
3. Los mensajes "Total jugadores de [posición]: X"
4. Cualquier error en rojo que aparezca

**Ejemplo de lo que necesito:**
```
Jugadores cargados: 25
Estructura del primer jugador: {
  "player": { ... },
  "statistics": [ ... ]
}
Total jugadores de Portero: 0
Total jugadores de Defensa: 0
```

---

## 💡 SOLUCIÓN RÁPIDA

**Si tienes prisa, haz esto:**

```bash
# 1. Detén el servidor (Ctrl+C)

# 2. Ejecuta estos comandos:
.\mvnw.cmd clean compile

# 3. Inicia el servidor
.\mvnw.cmd spring-boot:run

# 4. En el navegador:
# - Presiona Ctrl + Shift + Delete
# - Selecciona "Cached images and files"
# - Click "Clear data"

# 5. Abre la página:
http://localhost:8081/crear-alineacion.html

# 6. F12 para abrir consola

# 7. Completa los pasos hasta el Paso 4

# 8. Mira la consola
```

---

## ✅ SI FUNCIONA CORRECTAMENTE

Deberías ver:
- ✅ Dropdowns con jugadores
- ✅ Nombres y números correctos
- ✅ Dropdowns interactivos (no deshabilitados)
- ✅ Mensaje "-- Selecciona jugador (X disponibles) --"

---

**¡Prueba estos pasos y dime exactamente qué ves en la consola!**
