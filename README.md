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
| **POST** | `/register` | Admin | Registro de nuevos usuarios (Docentes/Estudiantes). |
| **GET** | `/me` | All | Retorna el perfil completo del usuario logueado. |

### 📊 Módulo de Dashboards (Vistas Consolidadas)
*Base Path: `/api/dashboard`*

| Método | Endpoint | Rol | Análisis |
| :--- | :--- | :--- | :--- |
| **GET** | `/admin` | Admin | Retorna KPIs globales: total alumnos, materias críticas, y hitos de gestión. |
| **GET** | `/teacher` | Teacher | Retorna promedio de asistencia de sus grupos, actas pendientes de cierre y resumen de materias. |
| **GET** | `/student` | Student | Retorna el **GPA (Promedio) actual**, alertas de riesgo de reprobación y detalle de notas actuales. |

### 🏫 Gestión Académica (Estructura)
*Base Path: `/api`*

| Método | Endpoint | Entidad | Propósito |
| :--- | :--- | :--- | :--- |
| **POST** | `/managements` | Admin | Crea una nueva gestión anual (ej: 2026). |
| **GET** | `/managements/{id}/stats` | Admin | Análisis de tasa de aprobación y alumnos en riesgo por gestión. |
| **PUT** | `/subjects/{id}/close` | Admin/Teacher | **Cierre definitivo de acta.** Bloquea ediciones y congela notas. |
| **POST** | `/subjects/{id}/activate` | Admin | Cambia materia de DRAFT a ACTIVE si el plan de evaluación suma 100. |
| **GET** | `/enrollments/my-history` | Estudiante | Kardex oficial por semestres. |

### 📝 Registro y Evaluación (Operativo)
*Base Path: `/api`*

| Método | Endpoint | Propósito | Regla de Negocio Aplicada |
| :--- | :--- | :--- | :--- |
| **POST** | `/attendance/bulk` | Registro masivo de asistencia diaria. | No permite fechas futuras. |
| **POST** | `/grades` | Carga de notas por componente. | Valida que la nota no exceda el peso del componente. |
| **GET** | `/enrollments/my-history` | **Kardex del Estudiante.** | Agrupa todo el historial académico por Semestres. |

### 🖨️ Módulo de Reportes y Configuración
*Base Path: `/api`*

| Método | Endpoint | Tipo | Análisis |
| :--- | :--- | :--- | :--- |
| **GET** | `/reports/subjects/{id}/pdf` | Export | Genera el **Acta de Notas** legal en formato PDF. |
| **GET** | `/reports/subjects/{id}/excel` | Export | Genera el reporte detallado de asistencias en Excel. |
| **GET** | `/settings` | Config | Obtiene parámetros globales (ej: límites de faltas). |
| **PUT** | `/settings/{key}` | Config | Cambia reglas del sistema en tiempo real sin reiniciar el servidor. |

## 🛠️ Configuración Global
El sistema utiliza una tabla `system_setting` para gestionar parámetros sin necesidad de recompilar código:
- `ABSENCE_LIMIT_FACE_TO_FACE`: Límite de faltas para modalidad presencial.
- `ABSENCE_LIMIT_BLENDED`: Límite de faltas para modalidad semipresencial.
- `ABSENCE_LIMIT_ONLINE`: Límite de faltas para modalidad virtual.
---
© 2026 Universidad NUR - Taller V
