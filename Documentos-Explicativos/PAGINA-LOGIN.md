# 🔑 Página de Login - Guía de Uso

## ✅ ¡Nueva página de Login creada!

---

## 🌐 **Cómo acceder:**

### **Opción 1: Página de Login dedicada**
```
http://localhost:8081/login
```

### **Opción 2: Página principal (con pestañas)**
```
http://localhost:8081/
```

---

## 🆚 **Diferencias entre las páginas:**

### **📄 /login (Nueva - Login Tradicional)**
- ✅ Página dedicada solo para login
- ✅ Diseño limpio y enfocado
- ✅ Checkbox "Recordarme" (localStorage vs sessionStorage)
- ✅ Link para "¿Olvidaste tu contraseña?"
- ✅ Auto-rellena el email si ya iniciaste sesión antes
- ✅ Redirige automáticamente después del login
- ✅ Link para registro ("¿No tienes cuenta?")

### **📄 / (Página Principal con Pestañas)**
- ✅ Registro y Login en una sola página
- ✅ Pestañas para cambiar entre Registro/Login
- ✅ Información del servidor en tiempo real
- ✅ Múltiples enlaces útiles

---

## 🎨 **Características de /login:**

### **Funcionalidades:**
- ✅ **Recordarme**: Si marcas el checkbox, guarda el token en localStorage (persistente)
- ✅ **Sin recordar**: Si no marcas el checkbox, guarda en sessionStorage (se borra al cerrar navegador)
- ✅ **Auto-relleno**: Si ya iniciaste sesión, auto-completa el email
- ✅ **Validación**: Valida email y contraseña antes de enviar
- ✅ **Loaders**: Muestra animación mientras procesa
- ✅ **Alertas**: Mensajes claros de éxito/error

### **Enlaces útiles:**
- 🏠 **Inicio**: Volver a la página principal
- 🗄️ **Base de Datos**: Acceso directo a H2 Console
- 💚 **Health**: Estado del servidor
- 📝 **Registro**: "¿No tienes cuenta? Regístrate aquí"

---

## 📝 **Flujo de uso:**

### **Primer uso (Registro + Login):**

1. **Registrar usuario:**
   ```
   Ve a: http://localhost:8081/
   Pestaña: Registro
   Email: tu@email.com
   Password: tu_password
   Click: "Registrarse"
   ```

2. **Iniciar sesión:**
   ```
   Ve a: http://localhost:8081/login
   Email: tu@email.com
   Password: tu_password
   ☑️ Marca "Recordarme" (opcional)
   Click: "Iniciar Sesión"
   ```

### **Usos posteriores:**

- Si marcaste "Recordarme": Tu sesión persiste incluso si cierras el navegador
- Si NO marcaste "Recordarme": Tendrás que volver a iniciar sesión

---

## 🔒 **Seguridad:**

### **localStorage vs sessionStorage:**

| Característica | localStorage (Recordarme ✅) | sessionStorage (Sin recordar ❌) |
|----------------|----------------------------|----------------------------------|
| **Duración** | Persiste indefinidamente | Se borra al cerrar navegador |
| **Seguridad** | Menos seguro | Más seguro |
| **Conveniencia** | Alta (no reiniciar sesión) | Baja (reiniciar cada vez) |
| **Uso recomendado** | Dispositivos personales | Dispositivos compartidos |

---

## 🎯 **URLs disponibles:**

| URL | Descripción |
|-----|-------------|
| `http://localhost:8081/` | 🏠 Página principal (Registro + Login) |
| `http://localhost:8081/login` | 🔑 Página de Login dedicada |
| `http://localhost:8081/h2-console` | 🗄️ Consola de base de datos |
| `http://localhost:8081/actuator/health` | 💚 Estado del servidor |

---

## 🚀 **Próximos pasos (Desarrollo futuro):**

Para hacer la página de login completamente funcional, puedes agregar:

1. ✅ **Dashboard**: Redirigir a `/dashboard` después del login
2. ✅ **Recuperación de contraseña**: Implementar "¿Olvidaste tu contraseña?"
3. ✅ **Logout**: Botón para cerrar sesión
4. ✅ **Protección de rutas**: Verificar token antes de acceder a páginas protegidas
5. ✅ **Refresh token**: Sistema de renovación de tokens

---

## 💡 **Ejemplo de uso:**

### **Escenario 1: Usuario nuevo**
```
1. Ve a http://localhost:8081/
2. Pestaña "Registro"
3. Email: nuevo@usuario.com, Password: 123456
4. Click "Registrarse"
5. Ve a http://localhost:8081/login
6. Email: nuevo@usuario.com, Password: 123456
7. Marca "Recordarme"
8. Click "Iniciar Sesión"
✅ ¡Listo! Token guardado en localStorage
```

### **Escenario 2: Usuario existente**
```
1. Ve a http://localhost:8081/login
2. El email se auto-completa (si ya iniciaste sesión antes)
3. Ingresa tu password
4. Click "Iniciar Sesión"
✅ ¡Listo!
```

---

## 🎉 **¡Todo listo!**

Ahora tienes:
- ✅ Página principal con pestañas (`/`)
- ✅ Página de login dedicada (`/login`)
- ✅ Ambas funcionan perfectamente
- ✅ Puedes elegir cuál usar según tu preferencia

**¡Disfruta tu nueva interfaz de login!** 🚀

