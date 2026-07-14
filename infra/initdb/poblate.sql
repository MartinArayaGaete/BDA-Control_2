-- ============================================
-- EXTENSIÓN POSTGIS
-- ============================================
CREATE EXTENSION IF NOT EXISTS postgis;

-- ============================================
-- CREACIÓN DE TABLAS
-- ============================================

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    location geometry(Point, 4326) NOT NULL
);

CREATE TABLE IF NOT EXISTS sectors (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    geometry geometry(Polygon, 4326) NOT NULL
);

CREATE TABLE IF NOT EXISTS tasks (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    due_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    user_id BIGINT REFERENCES users(id),
    location geometry(Point, 4326) NOT NULL,
    sector_id BIGINT REFERENCES sectors(id)
);

CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    message TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    task_id BIGINT,
    user_id BIGINT NOT NULL REFERENCES users(id)
);

-- ============================================
-- ÍNDICES ESPACIALES
-- ============================================

-- Índice espacial para ubicación de usuarios
CREATE INDEX IF NOT EXISTS idx_users_location ON users USING GIST (location);

-- Índice espacial para geometría de sectores
CREATE INDEX IF NOT EXISTS idx_sectors_geometry ON sectors USING GIST (geometry);

-- Índice espacial para ubicación de tareas
CREATE INDEX IF NOT EXISTS idx_tasks_location ON tasks USING GIST (location);

-- ============================================
-- ÍNDICES TRADICIONALES
-- ============================================

-- Búsqueda rápida por username (login, registro)
CREATE INDEX IF NOT EXISTS idx_users_username ON users (username);

-- Búsqueda por estado de usuario (soft delete)
CREATE INDEX IF NOT EXISTS idx_users_status ON users (status);

-- Búsqueda por estado de tarea (filtros, estadísticas)
CREATE INDEX IF NOT EXISTS idx_tasks_status ON tasks (status);

-- Búsqueda por usuario y estado (mis tareas)
CREATE INDEX IF NOT EXISTS idx_tasks_user_status ON tasks (user_id, status);

-- Búsqueda por sector (filtros)
CREATE INDEX IF NOT EXISTS idx_tasks_sector ON tasks (sector_id);

-- Búsqueda por fecha de vencimiento (scheduler de expiración)
CREATE INDEX IF NOT EXISTS idx_tasks_due_date ON tasks (due_date);

-- Búsqueda combinada para scheduler de notificaciones
CREATE INDEX IF NOT EXISTS idx_tasks_expiration ON tasks (status, due_date, user_id);

-- Búsqueda de notificaciones por usuario y estado
CREATE INDEX IF NOT EXISTS idx_notifications_user_read ON notifications (user_id, is_read);

-- Búsqueda de notificaciones por tarea y estado
CREATE INDEX IF NOT EXISTS idx_notifications_task_read ON notifications (task_id, is_read);

-- Búsqueda de notificaciones por fecha
CREATE INDEX IF NOT EXISTS idx_notifications_created ON notifications (created_at DESC);

-- ============================================
-- LIMPIEZA DE DATOS (por si ya existen)
-- ============================================
TRUNCATE TABLE notifications, tasks, sectors, users RESTART IDENTITY CASCADE;

-- ============================================
-- INSERCIÓN DE USUARIOS
-- ============================================
INSERT INTO users (username, password, location, status) VALUES
('admin', 'pass123', ST_SetSRID(ST_MakePoint(-70.6652, -33.5281), 4326), 'ACTIVE'),
('sergio_guardia', 'pass123', ST_SetSRID(ST_MakePoint(-70.6610, -33.5310), 4326), 'ACTIVE'),
('maria_conserje', 'pass123', ST_SetSRID(ST_MakePoint(-70.6685, -33.5255), 4326), 'ACTIVE'),
('juan_tecnico', 'pass123', ST_SetSRID(ST_MakePoint(-70.6630, -33.5295), 4326), 'ACTIVE'),
('rosa_aseo', 'pass123', ST_SetSRID(ST_MakePoint(-70.6670, -33.5270), 4326), 'DELETED'),
('pedro_seguridad', 'pass123', ST_SetSRID(ST_MakePoint(-70.6645, -33.5320), 4326), 'DELETED'),
('carlos_supervisor', 'pass123', ST_SetSRID(ST_MakePoint(-70.6625, -33.5265), 4326), 'ACTIVE'),
('ana_jardinera', 'pass123', ST_SetSRID(ST_MakePoint(-70.6690, -33.5290), 4326), 'ACTIVE');

-- ============================================
-- INSERCIÓN DE SECTORES
-- ============================================

-- Sector 1: Condominio Principal (polígono grande)
INSERT INTO sectors (name, geometry) VALUES
('Condominio Albano 2', ST_SetSRID(ST_GeomFromText('POLYGON((-70.668 -33.530, -70.660 -33.530, -70.660 -33.525, -70.668 -33.525, -70.668 -33.530))'), 4326));

-- Sector 2: Estacionamientos Sur (polígono pequeño)
INSERT INTO sectors (name, geometry) VALUES
('Estacionamientos Sur', ST_SetSRID(ST_GeomFromText('POLYGON((-70.663 -33.534, -70.661 -33.534, -70.661 -33.532, -70.663 -33.532, -70.663 -33.534))'), 4326));

-- Sector 3: Área de Jardines (nuevo polígono)
INSERT INTO sectors (name, geometry) VALUES
('Jardines Norte', ST_SetSRID(ST_GeomFromText('POLYGON((-70.670 -33.524, -70.666 -33.524, -70.666 -33.522, -70.670 -33.522, -70.670 -33.524))'), 4326));

-- Sector 4: Zona de Piscina (polígono pequeño)
INSERT INTO sectors (name, geometry) VALUES
('Zona Piscina', ST_SetSRID(ST_GeomFromText('POLYGON((-70.664 -33.536, -70.662 -33.536, -70.662 -33.534, -70.664 -33.534, -70.664 -33.536))'), 4326));

-- ============================================
-- INSERCIÓN DE TAREAS
-- ============================================

-- Tareas en Condominio Albano 2 (Sector 1)
INSERT INTO tasks (title, description, due_date, status, user_id, location, sector_id) VALUES
(
    'Limpieza Hall Torre A',
    'Limpiar vidrios de la entrada principal del hall. Usar productos no abrasivos.',
    '2026-08-15', 'COMPLETED', 1,
    ST_SetSRID(ST_MakePoint(-70.665, -33.528), 4326), 1
),
(
    'Revisión Extintores',
    'Verificar carga y presión de extintores del piso 5. Registrar fecha de revisión.',
    '2026-07-20', 'COMPLETED', 1,
    ST_SetSRID(ST_MakePoint(-70.664, -33.527), 4326), 1
),
(
    'Ampolleta Pasillo Piso 3',
    'Cambiar luz quemada en el pasillo del piso 3. Usar ampolleta LED 30W luz cálida.',
    '2026-07-20', 'PENDING', NULL,
    ST_SetSRID(ST_MakePoint(-70.666, -33.529), 4326), 1
),
(
    'Reparación Puerta Principal',
    'Arreglar bisagra de la puerta principal de acceso. Necesita lubricación.',
    '2026-08-30', 'PENDING', NULL,
    ST_SetSRID(ST_MakePoint(-70.665, -33.526), 4326), 1
),
(
    'Pintura Muro Perimetral',
    'Pintar muro perimetral lado norte con pintura antihongos color blanco.',
    '2026-07-15', 'IN_PROGRESS', 7,
    ST_SetSRID(ST_MakePoint(-70.667, -33.527), 4326), 1
);

-- Tareas en Estacionamientos Sur (Sector 2)
INSERT INTO tasks (title, description, due_date, status, user_id, location, sector_id) VALUES
(
    'Ronda Portón Sur',
    'Cierre de acceso vehicular nocturno. Verificar que no queden autos sin permiso.',
    '2026-08-10', 'COMPLETED', 2,
    ST_SetSRID(ST_MakePoint(-70.662, -33.533), 4326), 2
),
(
    'Pintura Demarcación',
    'Pintar líneas amarillas de estacionamientos. Usar pintura acrílica para exterior.',
    '2026-09-15', 'PENDING', NULL,
    ST_SetSRID(ST_MakePoint(-70.6615, -33.5335), 4326), 2
);

-- Tareas en Jardines Norte (Sector 3)
INSERT INTO tasks (title, description, due_date, status, user_id, location, sector_id) VALUES
(
    'Poda de Árboles',
    'Podar ramas altas de los árboles del jardín norte. Usar escalera certificada.',
    '2026-08-05', 'PENDING', NULL,
    ST_SetSRID(ST_MakePoint(-70.668, -33.523), 4326), 3
),
(
    'Riego Automático',
    'Reparar sistema de riego automático. Cambiar aspersores dañados.',
    '2026-07-18', 'IN_PROGRESS', 8,
    ST_SetSRID(ST_MakePoint(-70.667, -33.523), 4326), 3
);

-- Tareas en Zona Piscina (Sector 4)
INSERT INTO tasks (title, description, due_date, status, user_id, location, sector_id) VALUES
(
    'Control pH Piscina',
    'Medir y ajustar niveles de pH y cloro. Registrar en bitácora.',
    '2026-07-22', 'PENDING', NULL,
    ST_SetSRID(ST_MakePoint(-70.663, -33.535), 4326), 4
);

-- Tareas FUERA de sectores (sector_id = NULL)
INSERT INTO tasks (title, description, due_date, status, user_id, location, sector_id) VALUES
(
    'Basura Exterior',
    'Recoger escombros de la vereda perimetral exterior.',
    '2026-08-21', 'PENDING', NULL,
    ST_SetSRID(ST_MakePoint(-70.675, -33.540), 4326), NULL
),
(
    'Mantenimiento Señalética',
    'Revisar y limpiar señaléticas de tránsito en calles aledañas.',
    '2026-07-25', 'PENDING', 3,
    ST_SetSRID(ST_MakePoint(-70.658, -33.538), 4326), NULL
);

-- Tareas completadas por otros usuarios (para estadísticas)
INSERT INTO tasks (title, description, due_date, status, user_id, location, sector_id) VALUES
(
    'Limpieza Ventanas Torre B',
    'Lavado de ventanas exteriores de la Torre B.',
    '2026-07-10', 'COMPLETED', 3,
    ST_SetSRID(ST_MakePoint(-70.665, -33.528), 4326), 1
),
(
    'Revisión Cámaras Seguridad',
    'Verificar funcionamiento de cámaras del perímetro sur.',
    '2026-07-08', 'COMPLETED', 2,
    ST_SetSRID(ST_MakePoint(-70.662, -33.533), 4326), 2
),
(
    'Fertilización Jardines',
    'Aplicar fertilizante en jardines del sector norte.',
    '2026-07-05', 'COMPLETED', 8,
    ST_SetSRID(ST_MakePoint(-70.668, -33.523), 4326), 3
),
(
    'Reparación Bomba Piscina',
    'Cambiar filtro de la bomba principal de la piscina.',
    '2026-07-12', 'COMPLETED', 1,
    ST_SetSRID(ST_MakePoint(-70.663, -33.535), 4326), 4
),
(
    'Mantenimiento Portón',
    'Lubricar rieles del portón de acceso principal.',
    '2026-07-11', 'COMPLETED', 7,
    ST_SetSRID(ST_MakePoint(-70.664, -33.527), 4326), 1
);

-- ============================================
-- ESTADÍSTICAS DE TABLAS
-- ============================================
SELECT 'users' AS tabla, COUNT(*) AS total FROM users
UNION ALL
SELECT 'sectors', COUNT(*) FROM sectors
UNION ALL
SELECT 'tasks', COUNT(*) FROM tasks
UNION ALL
SELECT 'notifications', COUNT(*) FROM notifications;

-- ============================================
-- VERIFICACIÓN DE DATOS ESPACIALES
-- ============================================
SELECT
    u.username,
    ST_AsText(u.location) AS ubicacion,
    s.name AS sector_cercano
FROM users u
LEFT JOIN sectors s ON ST_DWithin(u.location, s.geometry, 0.01);