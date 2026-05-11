# Notas Nur 3.0 - API Backend

Sistema de Gestión Académica Universitario para la Universidad NUR, desarrollado con Spring Boot.

## 🚀 Tecnologías
*   **Java 25** / Spring Boot 4.0.6
*   **Database**: SQL Server (Transact-SQL)
*   **Security**: JWT (JSON Web Tokens)
*   **Reporting**: Apache POI (Excel) & OpenPDF (PDF)

## 📌 Documentación de la API (Endpoints)

### 🔑 Módulo de Autenticación y Seguridad
*Base Path: `/api/auth`*

| Método | Endpoint | Rol | Descripción |
| :--- | :--- | :--- | :--- |
| **POST** | `/login` | Public | Intercambia credenciales por un JWT Token. |
| **POST** | `/logout` | Public | Cierra la sesión del usuario. |
| **GET** | `/me` | All | Retorna el perfil completo del usuario logueado. |

### 📊 Módulo de Dashboards (Vistas Consolidadas)
*Base Path: `/api/dashboard`*

| Método | Endpoint | Rol | Análisis |
| :--- | :--- | :--- | :--- |
| **GET** | `/admin` | Admin | Retorna KPIs globales: total alumnos, materias críticas, y hitos de gestión. |
| **GET** | `/teacher` | Teacher | Retorna promedio de asistencia de sus grupos, actas pendientes de cierre y resumen de materias. |
| **GET** | `/student` | Student | Retorna el **GPA (Promedio) actual**, alertas de riesgo de reprobación y detalle de notas actuales. |

### 👥 Gestión de Usuarios
*Base Path: `/api/users`*

| Método | Endpoint | Rol | Descripción |
| :--- | :--- | :--- | :--- |
| **GET** | `/role/{roleName}` | Admin | Obtiene usuarios filtrados por rol (TEACHER, STUDENT, ADMIN). |
| **GET** | `/role/{roleName}/paginated` | Admin | **Versión paginada** para grandes volúmenes de usuarios. |
| **POST** | `/` | Admin | Crea nuevos usuarios (Docentes/Estudiantes). |
| **PUT** | `/{id}` | Admin | Actualiza datos de un usuario existente. |
| **DELETE** | `/{id}` | Admin | Elimina un usuario del sistema. |
| **PATCH** | `/{id}/status` | Admin | Cambia el estado de un usuario (ACTIVE, INACTIVE, GRADUATED). |

**Parámetros de Paginación:**
- `page`: Número de página (default: 0)
- `size`: Elementos por página (default: 20)
- `sort`: Campo de ordenación (default: "name")
- `direction`: Dirección (asc/desc, default: asc)

*Ejemplo:* `/api/users/role/TEACHER/paginated?page=0&size=10&sort=name,desc`

### 🏫 Gestión Académica (Estructura)
*Base Path: `/api/subjects`*

| Método | Endpoint | Rol | Propósito |
| :--- | :--- | :--- | :--- |
| **GET** | `/` | All | Lista todas las materias del sistema. |
| **GET** | `/paginated` | All | **Versión paginada** para grandes volúmenes de materias. |
| **POST** | `/` | Admin | Crea una nueva materia. |
| **GET** | `/{id}` | All | Obtiene detalles de una materia específica. |
| **PUT** | `/{id}` | Admin | Actualiza datos de una materia. |
| **DELETE** | `/{id}` | Admin | Elimina una materia. |
| **PUT** | `/{id}/activate` | Admin/Teacher | Activa materia si el plan de evaluación suma 100. |
| **PUT** | `/{id}/close` | Admin | **Cierre definitivo de acta.** Bloquea ediciones y congela notas. |

**Parámetros de Paginación:**
- `page`: Número de página (default: 0)
- `size`: Elementos por página (default: 20)
- `sort`: Ordenación en formato `campo, direccion` (ej. `name,asc` o `name,desc`; default: `name,asc`)

*Ejemplo:* `/api/subjects/paginated?page=0&size=15&sort=name,asc`

### 📋 Gestión de Semestres y Gestiones
*Base Path: `/api/managements`*

| Método | Endpoint | Rol | Propósito |
| :--- | :--- | :--- | :--- |
| **POST** | `/` | Admin | Crea una nueva gestión anual (ej: 2026). |
| **GET** | `/{id}/stats` | Admin | Análisis de tasa de aprobación y alumnos en riesgo por gestión. |

*Base Path: `/api/semesters`*

| Método | Endpoint | Rol | Propósito |
| :--- | :--- | :--- | :--- |
| **GET** | `/` | All | Lista todos los semestres disponibles. |
| **POST** | `/` | Admin | Crea un nuevo semestre académico. |
| **GET** | `/{id}` | All | Obtiene detalles de un semestre específico. |
| **PUT** | `/{id}` | Admin | Actualiza datos de un semestre. |
| **DELETE** | `/{id}` | Admin | Elimina un semestre. |

### 📝 Registro y Evaluación (Operativo)
*Base Path: `/api/grades`*

| Método | Endpoint | Rol | Propósito | Regla de Negocio Aplicada |
| :--- | :--- | :--- | :--- | :--- |
| **POST** | `/` | Teacher | Carga individual de notas por componente. | Valida que la nota no exceda el peso del componente. |

*Base Path: `/api/attendance`*

| Método | Endpoint | Rol | Propósito | Regla de Negocio Aplicada |
| :--- | :--- | :--- | :--- | :--- |
| **POST** | `/bulk` | Teacher | Registro masivo de asistencia diaria. | No permite fechas futuras. |

### 📊 Gestión de Planes de Evaluación
*Base Path: `/api/evaluation-plans`*

| Método | Endpoint | Rol | Propósito |
| :--- | :--- | :--- | :--- |
| **GET** | `/subject/{subjectId}` | Teacher/Admin | Obtiene el plan de evaluación de una materia. |
| **POST** | `/` | Teacher | Crea un nuevo plan de evaluación con componentes. |
| **PUT** | `/{id}` | Teacher | Actualiza componentes y ponderaciones. |
| **DELETE** | `/{id}` | Admin | Elimina un plan de evaluación. |

### 📈 Gestión de Inscripciones
*Base Path: `/api/enrollments`*

| Método | Endpoint | Rol | Propósito |
| :--- | :--- | :--- | :--- |
| **GET** | `/my-history` | Student | **Kardex oficial del estudiante** por semestres. |
| **POST** | `/` | Admin | Inscribir estudiantes en materias. |
| **GET** | `/subject/{subjectId}` | Teacher | Lista estudiantes inscritos en una materia. |
| **DELETE** | `/{id}` | Admin | Cancela una inscripción. |

### 🖨️ Módulo de Reportes y Configuración
*Base Path: `/api/reports`*

| Método | Endpoint | Rol | Tipo | Análisis |
| :--- | :--- | :--- | :--- |
| **GET** | `/subjects/{id}/acta-notas/pdf` | Admin/Teacher | Export | Genera el **Acta de Notas** legal en formato PDF. |
| **GET** | `/subjects/{id}/asistencia/excel` | Admin/Teacher | Export | Genera el reporte detallado de asistencias en Excel. |

*Base Path: `/api/settings`*

| Método | Endpoint | Rol | Tipo | Análisis |
| :--- | :--- | :--- | :--- |
| **GET** | `/` | Admin | Config | Obtiene parámetros globales del sistema. |
| **PUT** | `/{key}` | Admin | Config | Cambia reglas del sistema en tiempo real sin reiniciar. |

### 🏢 Gestión Institucional
*Base Path: `/api/faculties`*

| Método | Endpoint | Rol | Propósito |
| :--- | :--- | :--- | :--- |
| **GET** | `/` | All | Lista todas las facultades. |
| **POST** | `/` | Admin | Crea una nueva facultad. |
| **PUT** | `/{id}` | Admin | Actualiza datos de una facultad. |
| **DELETE** | `/{id}` | Admin | Elimina una facultad. |

*Base Path: `/api/degrees`*

| Método | Endpoint | Rol | Propósito |
| :--- | :--- | :--- | :--- |
| **GET** | `/` | All | Lista todas las carreras. |
| **POST** | `/` | Admin | Crea una nueva carrera. |
| **PUT** | `/{id}` | Admin | Actualiza datos de una carrera. |
| **DELETE** | `/{id}` | Admin | Elimina una carrera. |

## 🛠️ Configuración Global
El sistema utiliza una tabla `system_setting` para gestionar parámetros sin necesidad de recompilar código:
- `NUR_ROUNDING_MODE`: Tipo de redondeo para notas (HALF_UP, HALF_DOWN, etc.).
- `MAX_ABSENCES_FACE_TO_FACE`: Límite de faltas para modalidad presencial.
 - `MAX_ABSENCES_BLENDED`: Límite de faltas para modalidad semipresencial.
 - `MAX_ABSENCES_ONLINE`: Límite de faltas para modalidad virtual.
 - `ABSENCE_LIMIT_*`: Nomenclatura legacy/alternativa para límites de faltas; mantener solo por compatibilidad si aplica, pero las claves primarias documentadas y priorizadas por el sistema son `MAX_ABSENCES_*`.

## 🔐 Seguridad y Autorizaciones
- **JWT Tokens**: Autenticación stateless con tokens firmados.
- **Role-Based Access Control**: Admin, Teacher, Student con permisos específicos.
- **CORS**: Configurado para desarrollo frontend.
- **Validaciones**: Input validation y sanitización de datos.

---
© 2026 Universidad NUR - Taller V
