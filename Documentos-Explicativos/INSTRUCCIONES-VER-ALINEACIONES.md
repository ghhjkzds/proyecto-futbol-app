# 🚀 INSTRUCCIONES RÁPIDAS - Ver Alineaciones

## ⚡ SOLUCIÓN RÁPIDA (30 segundos)

### Si ves un mensaje de "sesión expirada":

1. Presiona **F12** en tu navegador
2. Ve a la pestaña **Console**
3. Escribe:
   ```javascript
   localStorage.clear();
   ```
4. Presiona Enter
5. Ve a `/login.html` e inicia sesión
6. Vuelve a `/ver-alineaciones.html`

---

## 🔍 CÓMO USAR LA NUEVA FUNCIONALIDAD

### Paso 1: Selecciona un Partido

En el dropdown superior, selecciona el partido que quieres ver.

### Paso 2: Observa las Alineaciones

Verás las alineaciones organizadas por equipo:
- **Izquierda**: Equipo local
- **Derecha**: Equipo visitante

### Paso 3: Haz Click para Ver Detalles

1. **Pasa el mouse** sobre una tarjeta de alineación
2. Verás el mensaje: **"👆 Click para ver jugadores"**
3. **Haz click** en la tarjeta
4. Se expandirá mostrando:
   - Formación táctica (ej: 4-3-3)
   - Todos los jugadores organizados por posición
   - Número de camiseta de cada jugador
   - Suplentes (si hay)

### Paso 4: Vota por tus Favoritas

Puedes votar por las alineaciones que más te gusten haciendo click en el botón **"👍 Votar"**

---

## 🐛 SI NO FUNCIONA

### Abre la Consola del Navegador

1. Presiona **F12**
2. Ve a la pestaña **Console**
3. Busca mensajes con emojis:

**Mensajes buenos (todo OK):**
```
✅ Detalles ahora visibles
🎯 Alineaciones por equipo: {...}
📊 Total alineaciones: 5
```

**Mensajes malos (hay un problema):**
```
❌ Token expirado
❌ Error en la respuesta
💥 Error completo
```

### Si ves errores:

1. **Copia el mensaje de error completo**
2. **Haz un screenshot** de la consola
3. **Envía la información** para que pueda ayudarte

---

## 📋 CHECKLIST RÁPIDO

Antes de reportar un problema, verifica:

- [ ] La aplicación está corriendo (`http://localhost:8081`)
- [ ] Has iniciado sesión
- [ ] Existen partidos creados
- [ ] El partido seleccionado tiene alineaciones
- [ ] Has refrescado la página (Ctrl + F5)

---

## 💡 TIPS

### Tip 1: Ver Logs
Los mensajes en la consola tienen emojis para facilitar la lectura:
- 🔄 = Cargando
- ✅ = Éxito
- ❌ = Error
- 🖱️ = Click detectado
- 📊 = Datos

### Tip 2: Hard Refresh
Si ves una versión antigua de la página:
- **Windows**: Ctrl + F5
- **Mac**: Cmd + Shift + R

### Tip 3: Un Solo Click
Solo necesitas hacer un click en la tarjeta para expandirla. Si haces click de nuevo, se cierra.

---

## 🎯 CARACTERÍSTICAS

### Interactividad
- ✅ Click para expandir/contraer
- ✅ Solo una alineación expandida a la vez
- ✅ Cursor "pointer" (manita) al pasar el mouse
- ✅ Indicador visual "Click para ver jugadores"

### Información Mostrada
- ✅ Formación táctica
- ✅ Jugadores por posición
- ✅ Número de camiseta
- ✅ Suplentes
- ✅ Usuario creador
- ✅ Cantidad de votos

### Visual
- ✅ Animaciones suaves
- ✅ Borde azul al expandir
- ✅ Fondo blanco cuando expandida
- ✅ Flecha que rota
- ✅ Ranking visual (#1, #2, #3...)

---

## 📞 NECESITAS MÁS AYUDA?

Revisa estos documentos:

1. **SOLUCION-NO-PUEDO-VER-ALINEACIONES.md** → Guía detallada paso a paso
2. **RESUMEN-MEJORAS-INTERACTIVIDAD.md** → Resumen de cambios técnicos
3. **DIAGNOSTICO-ERRORES-API.md** → Solución al problema de token expirado

---

**¡Disfruta viendo las alineaciones! ⚽✨**
