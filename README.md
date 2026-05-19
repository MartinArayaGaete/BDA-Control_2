# API Documentation - Control 2

Este documento contiene los endpoints disponibles en el backend para interactuar mediante Postman o cualquier otro cliente HTTP.

**Base URL:** `http://localhost:8080`

---

## 1. Usuarios (`/api/users`)

### Registrar Usuario
- **URL:** `/api/users/register`
- **Método:** `POST`
- **JSON Body:**
```json
{
  "username": "martin",
  "password": "password123",
  "longitude": -70.6483,
  "latitude": -33.4569
}
```
- **Salida Esperada:**
```json
{
  "id": 1,
  "username": "martin",
  "longitude": -70.6483,
  "latitude": -33.4569,
  "status": "ACTIVE"
}
```

### Obtener Todos los Usuarios
- **URL:** `/api/users`
- **Método:** `GET`
- **Salida Esperada:**
```json
[
  {
    "id": 1,
    "username": "martin",
    "longitude": -70.6483,
    "latitude": -33.4569,
    "status": "ACTIVE"
  }
]
```

### Iniciar Sesión (Login)
- **URL:** `/api/users/login`
- **Método:** `POST`
- **JSON Body:**
```json
{
  "username": "martin",
  "password": "password123"
}
```
- **Salida Esperada:**
```json
{
  "userId": 1,
  "username": "martin",
  "message": "Login successful"
}
```

---

## 2. Sectores (`/api/sectors`)

### Crear Sector
- **URL:** `/api/sectors`
- **Método:** `POST`
- **JSON Body:**
```json
{
  "name": "Sector Centro",
  "wkt": "POLYGON((-70.66 -33.44, -70.64 -33.44, -70.64 -33.46, -70.66 -33.46, -70.66 -33.44))"
}
```
- **Salida Esperada:**
```json
{
  "id": 1,
  "name": "Sector Centro",
  "wkt": "POLYGON((-70.66 -33.44, -70.64 -33.44, -70.64 -33.46, -70.66 -33.46, -70.66 -33.44))"
}
```

### Obtener Todos los Sectores
- **URL:** `/api/sectors`
- **Método:** `GET`
- **Salida Esperada:**
```json
[
  {
    "id": 1,
    "name": "Sector Centro",
    "wkt": "POLYGON((-70.66 -33.44, -70.64 -33.44, -70.64 -33.46, -70.66 -33.46, -70.66 -33.44))"
  }
]
```

### Sector con más tareas completadas en un radio de 2km
- **URL:** `/api/sectors/stats/top-sector/{userId}`
- **Método:** `GET`
- **Salida Esperada:**
```json
{
  "id": 1,
  "name": "Sector Centro",
  "completedTasksCount": 5
}
```

---

## 3. Tareas (`/api/tasks`)

### Crear Tarea
- **URL:** `/api/tasks`
- **Método:** `POST`
- **JSON Body:**
```json
{
  "title": "Limpiar plaza",
  "description": "Limpiar la plaza central",
  "dueDate": "2026-06-01",
  "status": "PENDING",
  "userId": 1,
  "longitude": -70.65,
  "latitude": -33.45
}
```
- **Salida Esperada:**
```json
{
  "id": 1,
  "title": "Limpiar plaza",
  "description": "Limpiar la plaza central",
  "dueDate": "2026-06-01",
  "status": "PENDING",
  "userId": 1,
  "userName": "martin",
  "longitude": -70.65,
  "latitude": -33.45
}
```

### Obtener Todas las Tareas
- **URL:** `/api/tasks`
- **Método:** `GET`
- **Salida Esperada:**
```json
[
  {
    "id": 1,
    "title": "Limpiar plaza",
    "description": "Limpiar la plaza central",
    "dueDate": "2026-06-01",
    "status": "PENDING",
    "userId": 1,
    "userName": "martin",
    "longitude": -70.65,
    "latitude": -33.45
  }
]
```

### Actualizar Tarea
- **URL:** `/api/tasks/{id}`
- **Método:** `PUT`
- **JSON Body:**
```json
{
  "title": "Limpiar plaza central",
  "description": "Limpiar toda la plaza central",
  "dueDate": "2026-06-02",
  "status": "IN_PROGRESS",
  "userId": 1,
  "longitude": -70.65,
  "latitude": -33.45
}
```
- **Salida Esperada:**
```json
{
  "id": 1,
  "title": "Limpiar plaza central",
  "description": "Limpiar toda la plaza central",
  "dueDate": "2026-06-02",
  "status": "IN_PROGRESS",
  "userId": 1,
  "userName": "martin",
  "longitude": -70.65,
  "latitude": -33.45
}
```

### Eliminar Tarea
- **URL:** `/api/tasks/{id}`
- **Método:** `DELETE`
- **Salida Esperada:** `204 No Content`

### Marcar Tarea como Completada
- **URL:** `/api/tasks/{id}/complete?userId={userId}`
- **Método:** `PATCH`
- **Salida Esperada:**
```json
{
  "id": 1,
  "title": "Limpiar plaza central",
  "description": "Limpiar toda la plaza central",
  "dueDate": "2026-06-02",
  "status": "COMPLETED",
  "userId": 1,
  "userName": "martin",
  "longitude": -70.65,
  "latitude": -33.45
}
```

### Obtener Tareas por Usuario
- **URL:** `/api/tasks/user/{userId}`
- **Método:** `GET`
- **Salida Esperada:**
```json
[
  {
    "id": 1,
    "title": "Limpiar plaza central",
    "description": "Limpiar toda la plaza central",
    "dueDate": "2026-06-02",
    "status": "COMPLETED",
    "userId": 1,
    "userName": "martin",
    "longitude": -70.65,
    "latitude": -33.45
  }
]
```

### Tareas realizadas por el usuario por sector
- **URL:** `/api/tasks/stats/user/{userId}`
- **Método:** `GET`
- **Salida Esperada:**
```json
[
  {
    "sectorName": "Sector Centro",
    "taskCount": 1
  }
]
```

### Tarea pendiente más cercana al usuario
- **URL:** `/api/tasks/closest/{userId}`
- **Método:** `GET`
- **Salida Esperada:**
```json
{
  "id": 2,
  "title": "Otra tarea",
  "description": "Tarea cercana",
  "dueDate": "2026-06-05",
  "status": "PENDING",
  "userId": 1,
  "userName": "martin",
  "longitude": -70.649,
  "latitude": -33.451
}
```

### Promedio de distancia de tareas completadas
- **URL:** `/api/tasks/stats/average-distance/{userId}`
- **Método:** `GET`
- **Salida Esperada:** `150.25` (Valor double)
