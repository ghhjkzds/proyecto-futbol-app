# 🔧 GUÍA DE SOLUCIÓN: No puedo ver las alineaciones

## 📅 Fecha: 6 de Febrero de 2026

---

## 🎯 PROBLEMA REPORTADO

**"No puedo interactuar para ver las alineaciones en la página de ver alineaciones"**

---

## ✅ MEJORAS IMPLEMENTADAS

### 1. **Logging Mejorado** 📊

Se han agregado mensajes de debug en la consola del navegador que te ayudarán a identificar exactamente dónde está el problema:

```javascript
// Abre la consola del navegador (F12) y verás mensajes como:
🔄 Cargando alineaciones para partido ID: 1
🔑 Token presente: true
🌐 Llamando a: http://localhost:8081/api/alineaciones/partido/1
📡 Respuesta recibida - Status: 200
📦 Datos recibidos: {...}
📊 Total alineaciones: 5
🃏 Creando card para alineación ID 1, Rank #1
🖱️ Click en alineación ID: 1
🔄 Toggle detalles llamado
✅ Detalles ahora visibles
```

### 2. **Eventos Click Mejorados** 🖱️

Se ha cambiado la forma de asignar los eventos click:

**Antes:**
```javascript
card.onclick = () => toggleDetalles(card);
```

**Ahora:**
```javascript
card.addEventListener('click', function(e) {
    console.log('🖱️ Click en alineación ID:', alineacion.id);
    toggleDetalles(card);
});
```

### 3. **Indicadores Visuales** 👀

Ahora las tarjetas muestran un mensaje al pasar el mouse:
- **Cuando pasas el mouse**: "👆 Click para ver jugadores"
- **Cuando está expandida**: "👆 Click para ocultar"

### 4. **Cursor Pointer** 👆

El cursor cambia a "pointer" (manita) cuando pasas sobre las alineaciones, indicando que son clickeables.

---

## 🔍 DIAGNÓSTICO PASO A PASO

Sigue estos pasos para identificar el problema:

### Paso 1: Verificar que la aplicación esté corriendo ✅

```powershell
# En PowerShell, ejecuta:
Get-NetTCPConnection -LocalPort 8081 -ErrorAction SilentlyContinue
```

**Si no sale nada**, la aplicación no está corriendo. Inicia la aplicación:

```powershell
cd "C:\Users\USUARIO\Downloads\proyecto-ACD"
.\mvnw.cmd spring-boot:run
```

---

### Paso 2: Verificar el Token JWT 🔑

1. Abre el navegador y presiona **F12** para abrir DevTools
2. Ve a la pestaña **Application** (o Aplicación)
3. En el menú izquierdo, expande **Local Storage**
4. Haz click en `http://localhost:8081`
5. Verifica que existan estas entradas:
   - `token` → Debe tener un valor largo (JWT)
   - `userEmail` → Tu email
   - `userRole` → USER o ADMIN

**Si no existe el token o está vacío:**

```javascript
// Abre la consola (F12 → Console) y ejecuta:
localStorage.clear();
// Luego ve a login.html e inicia sesión nuevamente
```

---

### Paso 3: Verificar que existan partidos creados 🏆

1. Ve a la página **Ver Alineaciones**
2. Abre la consola (F12)
3. Busca el mensaje: `Encontrados X partidos`

**Si sale 0 partidos:**
- Necesitas crear partidos primero
- Ve a **Crear Partido** (solo ADMIN puede hacerlo)

---

### Paso 4: Verificar que el partido tenga alineaciones ⚽

1. Selecciona un partido del dropdown
2. En la consola verás:
   ```
   📊 Total alineaciones: X
   ```

**Si sale 0 alineaciones:**
- Ese partido no tiene alineaciones creadas
- Ve a **Crear Alineación** y crea una para ese partido

---

### Paso 5: Verificar que las cards se renderizan 🃏

1. Después de seleccionar un partido
2. En la consola debe aparecer:
   ```
   🃏 Creando card para alineación ID 1, Rank #1
   ✅ Card creada correctamente
   ```

**Si NO aparece este mensaje:**
- Hay un error en el código JavaScript
- Copia el error que aparece en rojo en la consola

---

### Paso 6: Verificar que el click funciona 🖱️

1. Haz click en una tarjeta de alineación
2. En la consola debe aparecer:
   ```
   🖱️ Click en alineación ID: 1
   🔄 Toggle detalles llamado
   ✅ Detalles ahora visibles
   ```

**Si NO aparece el mensaje de click:**
- El evento no se está registrando
- Puede ser un conflicto de CSS con `pointer-events: none`

---

## 🛠️ SOLUCIONES COMUNES

### Problema 1: "Token expirado" ❌

**Síntoma:** Aparece el mensaje "Tu sesión ha expirado"

**Solución:**
```javascript
// En la consola del navegador (F12):
localStorage.clear();
// Luego ve a login.html e inicia sesión
```

---

### Problema 2: "No hay partidos disponibles" 📋

**Síntoma:** El dropdown de partidos está vacío

**Solución:**
1. Asegúrate de ser ADMIN
2. Ve a **Crear Partido**
3. Crea al menos un partido
4. Vuelve a **Ver Alineaciones**

---

### Problema 3: "No hay alineaciones para este partido" 🤷

**Síntoma:** El partido existe pero no tiene alineaciones

**Solución:**
1. Ve a **Crear Alineación**
2. Selecciona el partido
3. Selecciona un equipo (local o visitante)
4. Completa la formación
5. Selecciona los jugadores
6. Guarda la alineación
7. Vuelve a **Ver Alineaciones**

---

### Problema 4: "Las cards no responden al click" 🖱️

**Síntoma:** Haces click pero no pasa nada

**Solución 1 - Verificar CSS:**
```javascript
// En la consola:
document.querySelector('.alineacion-card').style.pointerEvents
// Debe devolver "" o "auto", NO "none"
```

**Solución 2 - Forzar refresh:**
```
Ctrl + F5  (Windows)
Cmd + Shift + R  (Mac)
```

**Solución 3 - Limpiar caché:**
1. F12 → Network (Red)
2. Click derecho → Clear browser cache
3. Refresca la página

---

### Problema 5: "Error 403 Forbidden" 🚫

**Síntoma:** En la consola aparece: `📡 Respuesta recibida - Status: 403`

**Solución:**
```javascript
// Token expirado, limpia y vuelve a iniciar sesión:
localStorage.clear();
window.location.href = 'login.html';
```

---

## 🎨 NUEVAS CARACTERÍSTICAS VISUALES

### Antes:
- Las cards no tenían indicación de que eran clickeables
- No había feedback visual al hacer click

### Ahora:
- ✅ Cursor cambia a "pointer" (manita)
- ✅ Mensaje "👆 Click para ver jugadores" al pasar el mouse
- ✅ Borde azul al expandirse
- ✅ Fondo blanco cuando está expandida
- ✅ Flecha (▼) rota al expandir
- ✅ Solo una alineación expandida a la vez

---

## 📊 CHECKLIST DE VERIFICACIÓN

Marca cada punto cuando lo hayas verificado:

- [ ] La aplicación está corriendo (puerto 8081 activo)
- [ ] He iniciado sesión correctamente
- [ ] Existe el token en localStorage
- [ ] Hay partidos creados en la base de datos
- [ ] Al menos un partido tiene alineaciones
- [ ] Puedo ver las tarjetas de alineaciones
- [ ] El cursor cambia a "pointer" al pasar sobre las cards
- [ ] Veo el mensaje "👆 Click para ver jugadores"
- [ ] Al hacer click, la consola muestra "🖱️ Click en alineación"
- [ ] Los detalles se expanden al hacer click

---

## 🔍 COMANDOS ÚTILES DE DEBUG

### En la consola del navegador (F12 → Console):

```javascript
// Ver el token actual
console.log('Token:', localStorage.getItem('token'));

// Ver todas las cards de alineaciones
console.log('Cards:', document.querySelectorAll('.alineacion-card').length);

// Verificar eventos click en la primera card
const card = document.querySelector('.alineacion-card');
console.log('Card encontrada:', !!card);
console.log('Cursor:', window.getComputedStyle(card).cursor);

// Probar el toggle manualmente
if (card) toggleDetalles(card);

// Ver estado de expansión
document.querySelectorAll('.alineacion-card').forEach((c, i) => {
    console.log(`Card ${i}:`, c.classList.contains('expanded') ? 'EXPANDIDA' : 'colapsada');
});
```

---

## 📞 INFORMACIÓN DE CONTACTO DE DEPURACIÓN

Si ninguna de las soluciones anteriores funciona, necesitaré que me proporciones:

1. **Screenshot de la consola** (F12) después de seleccionar un partido
2. **Screenshot de Application → Local Storage** mostrando el token
3. **Screenshot de la página** mostrando las alineaciones
4. El **mensaje completo de error** en rojo de la consola (si aparece)

---

## 🎓 ¿POR QUÉ PUEDE NO FUNCIONAR?

### Razones técnicas:

1. **Token JWT expirado** → Solución: Limpiar localStorage y login
2. **No hay datos** → Solución: Crear partidos y alineaciones
3. **Aplicación no corriendo** → Solución: Iniciar con mvnw
4. **Conflicto de eventos** → Solución: Refrescar con Ctrl+F5
5. **Caché del navegador** → Solución: Limpiar caché
6. **Error JavaScript** → Solución: Ver consola y reportar error

---

## ✨ PRÓXIMOS PASOS

Una vez que funcione:

1. ✅ Podrás hacer click en cualquier alineación
2. ✅ Se expandirá mostrando todos los jugadores
3. ✅ Verás número de camiseta + nombre + posición
4. ✅ Podrás votar por tus alineaciones favoritas
5. ✅ Solo una alineación expandida a la vez para mejor UX

---

**¡Sigue esta guía paso a paso y encontraremos el problema! 🚀**
