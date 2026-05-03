# 📋 Guía de Pruebas - US-10: Catálogos Institucionales (CRUD Faculty & Degree)

## 🔐 Paso 1: Obtener el Token ADMIN

**Endpoint:**
```
POST http://localhost:8080/api/auth/login
```

**Headers:**
```
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "email": "admin@nur.edu.bo",
  "password": "admin123"
}
```

**Response esperado:**
```json
{
  "success": true,
  "message": "Login exitoso",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": "uuid-here",
    "email": "admin@example.com",
    "role": "ADMIN"
  }
}
```

**⚠️ Guarda el token** para usarlo en las próximas peticiones.

---

## 📌 Paso 2: Configurar Headers en Postman

Para todas las peticiones siguientes, agrega estos headers:

```
Authorization: Bearer <TU_TOKEN_AQUI>
Content-Type: application/json
```

---

## 🏫 ENDPOINTS FACULTY - CRUD COMPLETO

### 1️⃣ **GET /api/faculties** - Listar todas las facultades

```
GET http://localhost:8080/api/faculties
```

**Response esperado (200 OK):**
```json
{
  "success": true,
  "message": "Facultades obtenidas exitosamente",
  "data": [
    {
      "id": 1,
      "name": "Facultad de Ingeniería",
      "code": "FI",
      "createdAt": "2026-04-28T01:00:00",
      "updatedAt": "2026-04-28T01:00:00"
    },
    {
      "id": 2,
      "name": "Facultad de Derecho",
      "code": "FD",
      "createdAt": "2026-04-28T01:05:00",
      "updatedAt": "2026-04-28T01:05:00"
    }
  ]
}
```

---

### 2️⃣ **GET /api/faculties/{id}** - Obtener facultad por ID

```
GET http://localhost:8080/api/faculties/1
```

**Response esperado (200 OK):**
```json
{
  "success": true,
  "message": "Facultad obtenida exitosamente",
  "data": {
    "id": 1,
    "name": "Facultad de Ingeniería",
    "code": "FI",
    "createdAt": "2026-04-28T01:00:00",
    "updatedAt": "2026-04-28T01:00:00"
  }
}
```

---

### 3️⃣ **POST /api/faculties** - Crear nueva facultad

```
POST http://localhost:8080/api/faculties
```

**Body (JSON):**
```json
{
  "name": "Facultad de Medicina",
  "code": "FM"
}
```

**Response esperado (201 Created):**
```json
{
  "success": true,
  "message": "Facultad creada exitosamente",
  "data": {
    "id": 3,
    "name": "Facultad de Medicina",
    "code": "FM",
    "createdAt": "2026-04-28T01:18:00",
    "updatedAt": "2026-04-28T01:18:00"
  }
}
```

**⚠️ Validaciones:**
- Si el `code` ya existe → **409 Conflict**
- Si el `name` ya existe → **409 Conflict**
- Si falta `name` o `code` → **400 Bad Request**

---

### 4️⃣ **PUT /api/faculties/{id}** - Actualizar facultad

```
PUT http://localhost:8080/api/faculties/3
```

**Body (JSON):**
```json
{
  "name": "Facultad de Ciencias Médicas",
  "code": "FCM"
}
```

**Response esperado (200 OK):**
```json
{
  "success": true,
  "message": "Facultad actualizada exitosamente",
  "data": {
    "id": 3,
    "name": "Facultad de Ciencias Médicas",
    "code": "FCM",
    "createdAt": "2026-04-28T01:18:00",
    "updatedAt": "2026-04-28T01:20:00"
  }
}
```

---

### 5️⃣ **DELETE /api/faculties/{id}** - Eliminar facultad

```
DELETE http://localhost:8080/api/faculties/3
```

**Response esperado (200 OK):**
```json
{
  "success": true,
  "message": "Facultad eliminada exitosamente",
  "data": null
}
```

**⚠️ Validaciones:**
- Si la facultad tiene carreras asociadas → **400 Bad Request**
  ```json
  {
    "success": false,
    "message": "No se puede eliminar la facultad porque tiene 2 carrera(s) asociada(s).",
    "data": null
  }
  ```

---

### 6️⃣ **GET /api/faculties/{id}/stats** - Obtener estadísticas de facultad

```
GET http://localhost:8080/api/faculties/1/stats
```

**Response esperado (200 OK):**
```json
{
  "success": true,
  "message": "Estadísticas obtenidas",
  "data": {
    "facultyName": "Facultad de Ingeniería",
    "activeStudentsCount": 150
  }
}
```

---

## 🎓 ENDPOINTS DEGREE - CRUD COMPLETO

### 1️⃣ **GET /api/degrees** - Listar todas las carreras

```
GET http://localhost:8080/api/degrees
```

**Response esperado (200 OK):**
```json
{
  "success": true,
  "message": "Carreras obtenidas exitosamente",
  "data": [
    {
      "id": 1,
      "name": "Ingeniería en Sistemas",
      "code": "IS",
      "facultyId": 1,
      "facultyName": "Facultad de Ingeniería",
      "createdAt": "2026-04-28T01:00:00",
      "updatedAt": "2026-04-28T01:00:00"
    },
    {
      "id": 2,
      "name": "Ingeniería Civil",
      "code": "IC",
      "facultyId": 1,
      "facultyName": "Facultad de Ingeniería",
      "createdAt": "2026-04-28T01:05:00",
      "updatedAt": "2026-04-28T01:05:00"
    }
  ]
}
```

---

### 2️⃣ **GET /api/degrees/{id}** - Obtener carrera por ID

```
GET http://localhost:8080/api/degrees/1
```

**Response esperado (200 OK):**
```json
{
  "success": true,
  "message": "Carrera obtenida exitosamente",
  "data": {
    "id": 1,
    "name": "Ingeniería en Sistemas",
    "code": "IS",
    "facultyId": 1,
    "facultyName": "Facultad de Ingeniería",
    "createdAt": "2026-04-28T01:00:00",
    "updatedAt": "2026-04-28T01:00:00"
  }
}
```

---

### 3️⃣ **POST /api/degrees** - Crear nueva carrera

```
POST http://localhost:8080/api/degrees
```

**Body (JSON):**
```json
{
  "name": "Ingeniería Electrónica",
  "code": "IE",
  "facultyId": 1
}
```

**Response esperado (201 Created):**
```json
{
  "success": true,
  "message": "Carrera creada exitosamente",
  "data": {
    "id": 3,
    "name": "Ingeniería Electrónica",
    "code": "IE",
    "facultyId": 1,
    "facultyName": "Facultad de Ingeniería",
    "createdAt": "2026-04-28T01:20:00",
    "updatedAt": "2026-04-28T01:20:00"
  }
}
```

**⚠️ Validaciones:**
- Si `facultyId` no existe → **404 Not Found**
- Si el `code` ya existe → **409 Conflict**
- Si el `name` ya existe → **409 Conflict**
- Si falta algún campo → **400 Bad Request**

---

### 4️⃣ **PUT /api/degrees/{id}** - Actualizar carrera

```
PUT http://localhost:8080/api/degrees/3
```

**Body (JSON):**
```json
{
  "name": "Ingeniería en Electrónica y Control",
  "code": "IEC",
  "facultyId": 1
}
```

**Response esperado (200 OK):**
```json
{
  "success": true,
  "message": "Carrera actualizada exitosamente",
  "data": {
    "id": 3,
    "name": "Ingeniería en Electrónica y Control",
    "code": "IEC",
    "facultyId": 1,
    "facultyName": "Facultad de Ingeniería",
    "createdAt": "2026-04-28T01:20:00",
    "updatedAt": "2026-04-28T01:22:00"
  }
}
```

---

### 5️⃣ **DELETE /api/degrees/{id}** - Eliminar carrera

```
DELETE http://localhost:8080/api/degrees/3
```

**Response esperado (200 OK):**
```json
{
  "success": true,
  "message": "Carrera eliminada exitosamente",
  "data": null
}
```

**⚠️ Validaciones:**
- Si la carrera tiene estudiantes inscritos → **400 Bad Request**
  ```json
  {
    "success": false,
    "message": "No se puede eliminar la carrera porque tiene 45 expediente(s) académico(s) asociado(s).",
    "data": null
  }
  ```

---

## 🧪 CASOS DE ERROR A PROBAR

### ❌ Crear Faculty con código duplicado
```
POST http://localhost:8080/api/faculties
```
```json
{
  "name": "Facultad Nueva",
  "code": "FI"  // ← Ya existe
}
```
**Response: 409 Conflict**

---

### ❌ Crear Degree sin facultad válida
```
POST http://localhost:8080/api/degrees
```
```json
{
  "name": "Carrera Test",
  "code": "TEST",
  "facultyId": 999  // ← No existe
}
```
**Response: 404 Not Found**

---

### ❌ Eliminar Faculty con carreras
```
DELETE http://localhost:8080/api/faculties/1
```
**Response: 400 Bad Request**
```json
{
  "success": false,
  "message": "No se puede eliminar la facultad porque tiene 3 carrera(s) asociada(s).",
  "data": null
}
```

---

### ❌ Sin token de autenticación
```
GET http://localhost:8080/api/faculties
```
(Sin header `Authorization`)
**Response: 401 Unauthorized**

---

### ❌ Token inválido o expirado
```
GET http://localhost:8080/api/faculties
Authorization: Bearer token_invalido
```
**Response: 401 Unauthorized**

---

## 📊 RESUMEN DE ENDPOINTS

| Método | Endpoint | Descripción | Status Code |
|--------|----------|-------------|------------|
| GET | `/api/faculties` | Listar todas | 200 |
| GET | `/api/faculties/{id}` | Obtener por ID | 200 / 404 |
| GET | `/api/faculties/{id}/stats` | Estadísticas | 200 / 404 |
| POST | `/api/faculties` | Crear | 201 / 400 / 409 |
| PUT | `/api/faculties/{id}` | Actualizar | 200 / 400 / 404 / 409 |
| DELETE | `/api/faculties/{id}` | Eliminar | 200 / 400 / 404 |
| GET | `/api/degrees` | Listar todas | 200 |
| GET | `/api/degrees/{id}` | Obtener por ID | 200 / 404 |
| POST | `/api/degrees` | Crear | 201 / 400 / 404 / 409 |
| PUT | `/api/degrees/{id}` | Actualizar | 200 / 400 / 404 / 409 |
| DELETE | `/api/degrees/{id}` | Eliminar | 200 / 400 / 404 |

---

## ✅ Checklist de Validación

- [ ] Login ADMIN y obtener token
- [ ] GET /api/faculties (listar todas)
- [ ] GET /api/faculties/1 (obtener por ID existente)
- [ ] GET /api/faculties/999 (ID no existe → 404)
- [ ] POST /api/faculties (crear nueva)
- [ ] POST /api/faculties (código duplicado → 409)
- [ ] PUT /api/faculties/1 (actualizar)
- [ ] GET /api/faculties/1/stats (estadísticas)
- [ ] DELETE /api/faculties/999 (no existe → 404)
- [ ] GET /api/degrees (listar todas)
- [ ] GET /api/degrees/1 (obtener por ID existente)
- [ ] POST /api/degrees (crear nueva)
- [ ] POST /api/degrees (facultyId no existe → 404)
- [ ] POST /api/degrees (código duplicado → 409)
- [ ] PUT /api/degrees/1 (actualizar)
- [ ] DELETE /api/degrees/1 (con dependencias → 400)

---

## 📸 Para Marcelo: Captura Requerida

Toma una captura (screenshot) del response JSON de:

```
GET http://localhost:8080/api/faculties
Authorization: Bearer <TU_TOKEN>
```

O del endpoint de estadísticas si prefieres mostrar el dominio de FacultyService:

```
GET http://localhost:8080/api/faculties/1/stats
Authorization: Bearer <TU_TOKEN>
```

**Incluye en el PR:**
- Capturas de al menos 3 endpoints funcionando
- El código compilando sin errores (BUILD SUCCESS)
- Evidencia de las validaciones (ej: 409 Conflict cuando hay duplicados)

---

## 🚀 Para Iniciar el Servidor

```bash
git clone <URL_DEL_REPOSITORIO>
cd notasnurv3_api
./mvnw spring-boot:run
```

El servidor estará en `http://localhost:8080`
