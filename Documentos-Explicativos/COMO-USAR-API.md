# 🚀 Guía de Uso - API de Autenticación

## ⚠️ IMPORTANTE: Error "Request method 'GET' is not supported"

Este error aparece cuando intentas acceder a los endpoints de autenticación desde el navegador.

**Los navegadores usan GET por defecto**, pero los endpoints `/api/auth/register` y `/api/auth/login` solo aceptan **POST**.

---

## ✅ SOLUCIÓN: Cómo probar los endpoints correctamente

### Opción 1: Usar IntelliJ IDEA (RECOMENDADO)

1. Abre el archivo `test-endpoints.http`
2. Verás botones "▶ Run" junto a cada petición
3. Haz clic en "Run" para ejecutar la petición

### Opción 2: Usar PowerShell con cURL

**Registro:**
```powershell
curl -X POST http://localhost:8081/api/auth/register `
  -H "Content-Type: application/json" `
  -d '{\"email\":\"test@test.com\",\"password\":\"123456\"}'
```

**Login:**
```powershell
curl -X POST http://localhost:8081/api/auth/login `
  -H "Content-Type: application/json" `
  -d '{\"email\":\"test@test.com\",\"password\":\"123456\"}'
```

### Opción 3: Usar Postman

1. Abre Postman
2. Crea una nueva petición POST
3. URL: `http://localhost:8081/api/auth/register`
4. En Headers: `Content-Type: application/json`
5. En Body → raw → JSON:
```json
{
  "email": "test@test.com",
  "password": "123456"
}
```
6. Haz clic en "Send"

### Opción 4: Usar el navegador (solo para ver info)

**Página principal (GET funciona):**
```
http://localhost:8081/
```
Verás información sobre los endpoints disponibles.

**Consola H2 (GET funciona):**
```
http://localhost:8081/h2-console
```
Para ver la base de datos.

**Health check (GET funciona):**
```
http://localhost:8081/actuator/health
```
Para verificar que el servidor está corriendo.

---

## 📊 Respuesta Exitosa

Cuando hagas POST correctamente, recibirás:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0QHRlc3QuY29tIiwiaWF0IjoxNzA2MzY...",
  "email": "test@test.com"
}
```

---

## ❌ Errores Comunes

### Error: "Request method 'GET' is not supported"
- **Causa**: Estás usando GET en vez de POST
- **Solución**: Usa una de las opciones anteriores (cURL, Postman, archivo .http)

### Error: "Email ya registrado"
- **Causa**: El email ya existe en la base de datos
- **Solución**: Usa otro email o reinicia la aplicación (base de datos en memoria)

### Error: "Credenciales inválidas"
- **Causa**: Email o contraseña incorrectos
- **Solución**: Verifica que usaste el mismo email y password que en el registro

---

## 🗄️ Ver los datos en la base de datos

1. Ve a: `http://localhost:8081/h2-console`
2. JDBC URL: `jdbc:h2:mem:testdb`
3. Username: `sa`
4. Password: (dejar vacío)
5. Haz clic en "Connect"
6. Ejecuta la query:
```sql
SELECT * FROM USERS;
```

---

## 🔍 Verificar que el servidor está corriendo

Abre en el navegador:
```
http://localhost:8081/actuator/health
```

Deberías ver:
```json
{
  "status": "UP"
}
```

---

## 📝 Notas Importantes

- ✅ Los endpoints GET funcionan en el navegador
- ❌ Los endpoints POST NO funcionan en el navegador
- ✅ Usa herramientas como Postman, cURL o el archivo .http
- 🔄 La base de datos H2 es en memoria (se borra al reiniciar)
- 🔑 El token JWT se genera automáticamente en registro y login

