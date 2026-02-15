# ✅ INSTRUCCIONES PARA COMPLETAR LA IMPLEMENTACIÓN DE COMENTARIOS

## 📋 Resumen
El sistema de comentarios ya está implementado en el código backend y frontend. Solo falta ejecutar el script SQL en tu base de datos.

---

## 🔧 PASO 1: Ejecutar Script SQL

Abre tu gestor de MySQL (MySQL Workbench, phpMyAdmin, etc.) y ejecuta el siguiente script:

```sql
USE futbol_app;

-- PASO 1: Modificar la tabla comentarios
ALTER TABLE comentarios 
    MODIFY COLUMN equipo_id INT NULL,
    ADD COLUMN alineacion_id INT NULL AFTER equipo_id,
    ADD COLUMN user_id INT NULL AFTER alineacion_id;

-- PASO 2: Agregar foreign key para alineacion_id
ALTER TABLE comentarios
    ADD CONSTRAINT fk_comentarios_alineacion
        FOREIGN KEY (alineacion_id)
        REFERENCES alineaciones(id)
        ON DELETE CASCADE;

-- PASO 3: Agregar foreign key para user_id
ALTER TABLE comentarios
    ADD CONSTRAINT fk_comentarios_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE SET NULL;

-- PASO 4: Agregar restricción de check
ALTER TABLE comentarios
    ADD CONSTRAINT chk_comentario_tipo
        CHECK (
            (equipo_id IS NOT NULL AND alineacion_id IS NULL) 
            OR 
            (equipo_id IS NULL AND alineacion_id IS NOT NULL)
        );

-- Verificar la estructura
SHOW CREATE TABLE comentarios;
```

**Archivo disponible:** `agregar-comentarios-alineaciones.sql`

---

## 🚀 PASO 2: Reiniciar la Aplicación

Después de ejecutar el script SQL, reinicia tu aplicación Spring Boot:

1. **Si está corriendo en terminal:**
   - Presiona `Ctrl + C` para detener
   - Ejecuta: `mvn spring-boot:run`

2. **Si está corriendo en IntelliJ:**
   - Haz clic en el botón Stop (cuadrado rojo)
   - Haz clic en Run (triángulo verde)

---

## 🧪 PASO 3: Probar el Sistema

1. **Abrir la aplicación:**
   ```
   http://localhost:8081/index.html
   ```

2. **Iniciar sesión** con tu usuario

3. **Ir a "Ver Alineaciones"**

4. **Seleccionar un partido** que tenga alineaciones creadas

5. **Hacer clic en una alineación** para expandirla

6. **Hacer clic en "Ver comentarios"**

7. **Escribir un comentario** y hacer clic en "📤 Enviar comentario"

8. **Probar respuestas:**
   - Hacer clic en "💬 Responder" bajo un comentario
   - Escribir una respuesta
   - Hacer clic en "📤 Enviar"

---

## ✅ Verificación

### Backend funcionando si:
- No hay errores en la consola al iniciar
- Los endpoints responden correctamente:
  - GET `/api/comentarios/alineacion/{id}` → Retorna array de comentarios
  - POST `/api/comentarios` → Crea nuevo comentario

### Frontend funcionando si:
- Se ve el botón "Ver comentarios" en cada alineación
- Al hacer clic, se muestra el formulario y la lista de comentarios
- Se pueden enviar comentarios y aparecen en la lista
- Se pueden crear respuestas a comentarios

---

## 📁 Archivos Modificados/Creados

### Backend:
- ✅ `src/main/java/com/futbol/proyectoacd/model/Comentario.java` - Actualizado
- ✅ `src/main/java/com/futbol/proyectoacd/dto/ComentarioDTO.java` - Actualizado
- ✅ `src/main/java/com/futbol/proyectoacd/dto/CrearComentarioRequest.java` - **NUEVO**
- ✅ `src/main/java/com/futbol/proyectoacd/repository/ComentarioRepository.java` - Actualizado
- ✅ `src/main/java/com/futbol/proyectoacd/service/ComentarioService.java` - **NUEVO**
- ✅ `src/main/java/com/futbol/proyectoacd/controller/ComentarioController.java` - **NUEVO**
- ✅ `src/main/java/com/futbol/proyectoacd/config/SecurityConfig.java` - Actualizado

### Frontend:
- ✅ `src/main/resources/static/ver-alineaciones.html` - Actualizado con estilos y funciones de comentarios

### Base de Datos:
- ⏳ `agregar-comentarios-alineaciones.sql` - **PENDIENTE DE EJECUTAR**

### Documentación:
- ✅ `SISTEMA-COMENTARIOS-ALINEACIONES.md` - Documentación completa del sistema

---

## 🐛 Solución de Problemas

### Error: "Cannot resolve column 'alineacion_id'"
**Causa:** El script SQL no se ha ejecutado  
**Solución:** Ejecutar el script SQL en el PASO 1

### Error: "Usuario no encontrado" al crear comentario
**Causa:** Token JWT inválido o expirado  
**Solución:** Cerrar sesión y volver a iniciar sesión

### Error 403 al crear comentario
**Causa:** No estás autenticado  
**Solución:** Iniciar sesión antes de intentar comentar

### No se muestran los comentarios
**Causa:** No hay comentarios creados aún  
**Solución:** Crear el primer comentario para probar

### Error en consola del navegador
**Causa:** Posible error de JavaScript  
**Solución:** Abrir consola del navegador (F12) y ver el error específico

---

## 📊 Flujo de Uso Normal

```
1. Usuario inicia sesión
   ↓
2. Va a "Ver Alineaciones"
   ↓
3. Selecciona un partido
   ↓
4. Hace clic en una alineación para expandir
   ↓
5. Hace clic en "Ver comentarios"
   ↓
6. Escribe un comentario
   ↓
7. Hace clic en "Enviar comentario"
   ↓
8. El comentario aparece en la lista
   ↓
9. Otros usuarios pueden responder
```

---

## 🎯 Características Implementadas

✅ Ver comentarios de una alineación (público)  
✅ Crear comentarios (requiere login)  
✅ Responder a comentarios (requiere login)  
✅ Ver respuestas anidadas  
✅ Mostrar autor y fecha de cada comentario  
✅ Prevención de XSS (escape de HTML)  
✅ Validación de autenticación  
✅ Manejo de errores y tokens expirados  
✅ Interfaz intuitiva y responsive  
✅ Animaciones y feedback visual  

---

## 📖 Documentación Adicional

Para entender en profundidad cómo funciona el sistema de comentarios, consulta:

**`SISTEMA-COMENTARIOS-ALINEACIONES.md`** - Documentación técnica completa que incluye:
- Arquitectura de base de datos
- Estructura del backend (modelos, DTOs, servicios, controladores)
- Funciones JavaScript del frontend
- Flujos de datos completos
- Diagramas de flujo
- Guía de mantenimiento
- Posibles extensiones futuras

---

## 🎉 ¡Todo Listo!

Una vez ejecutes el script SQL y reinicies la aplicación, el sistema de comentarios estará completamente funcional. Los usuarios podrán:

- 💬 Comentar en las alineaciones de otros usuarios
- 🔄 Responder a comentarios existentes
- 👀 Ver todas las conversaciones
- ⚡ Recibir feedback inmediato

---

**¿Necesitas ayuda?** Consulta la sección de Troubleshooting o la documentación completa.
