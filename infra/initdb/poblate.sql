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
CREATE INDEX IF NOT EXISTS idx_users_location ON users USING GIST (location);
CREATE INDEX IF NOT EXISTS idx_sectors_geometry ON sectors USING GIST (geometry);
CREATE INDEX IF NOT EXISTS idx_tasks_location ON tasks USING GIST (location);

-- ============================================
-- ÍNDICES TRADICIONALES
-- ============================================
CREATE INDEX IF NOT EXISTS idx_users_username ON users (username);
CREATE INDEX IF NOT EXISTS idx_users_status ON users (status);
CREATE INDEX IF NOT EXISTS idx_tasks_status ON tasks (status);
CREATE INDEX IF NOT EXISTS idx_tasks_user_status ON tasks (user_id, status);
CREATE INDEX IF NOT EXISTS idx_tasks_sector ON tasks (sector_id);
CREATE INDEX IF NOT EXISTS idx_tasks_due_date ON tasks (due_date);
CREATE INDEX IF NOT EXISTS idx_tasks_expiration ON tasks (status, due_date, user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_user_read ON notifications (user_id, is_read);
CREATE INDEX IF NOT EXISTS idx_notifications_task_read ON notifications (task_id, is_read);
CREATE INDEX IF NOT EXISTS idx_notifications_created ON notifications (created_at DESC);

-- ============================================
-- LIMPIEZA DE DATOS
-- ============================================
TRUNCATE TABLE notifications, tasks, sectors, users RESTART IDENTITY CASCADE;

-- ============================================
-- INSERCIÓN DE USUARIOS
-- ============================================
INSERT INTO users (username, password, location, status) VALUES
('admin', 'pass123', ST_SetSRID(ST_MakePoint(-70.6500, -33.4450), 4326), 'ACTIVE'),
('sergio_guardia', 'pass123', ST_SetSRID(ST_MakePoint(-70.5800, -33.4200), 4326), 'ACTIVE'),
('maria_conserje', 'pass123', ST_SetSRID(ST_MakePoint(-70.7200, -33.3800), 4326), 'ACTIVE'),
('juan_tecnico', 'pass123', ST_SetSRID(ST_MakePoint(-70.7500, -33.5000), 4326), 'ACTIVE'),
('rosa_aseo', 'pass123', ST_SetSRID(ST_MakePoint(-70.5500, -33.4700), 4326), 'DELETED'),
('pedro_seguridad', 'pass123', ST_SetSRID(ST_MakePoint(-70.6800, -33.3500), 4326), 'DELETED'),
('carlos_supervisor', 'pass123', ST_SetSRID(ST_MakePoint(-70.6000, -33.3800), 4326), 'ACTIVE'),
('ana_jardinera', 'pass123', ST_SetSRID(ST_MakePoint(-70.7000, -33.4400), 4326), 'ACTIVE');

-- ============================================
-- INSERCIÓN DE SECTORES (10 sectores ENORMES)
-- Cubren desde -70.800 hasta -70.500 (longitud)
--           desde -33.350 hasta -33.550 (latitud)
-- Cada sector mide ~0.10° × 0.10° (aprox 11km × 11km)
-- ============================================

-- FILA SUPERIOR (lat -33.350 a -33.450)
INSERT INTO sectors (name, geometry) VALUES
('Zona Norte Poniente', ST_SetSRID(ST_GeomFromText('POLYGON((-70.800 -33.350, -70.700 -33.350, -70.700 -33.450, -70.800 -33.450, -70.800 -33.350))'), 4326)),
('Zona Norte Centro', ST_SetSRID(ST_GeomFromText('POLYGON((-70.700 -33.350, -70.600 -33.350, -70.600 -33.450, -70.700 -33.450, -70.700 -33.350))'), 4326)),
('Zona Norte Oriente', ST_SetSRID(ST_GeomFromText('POLYGON((-70.600 -33.350, -70.500 -33.350, -70.500 -33.450, -70.600 -33.450, -70.600 -33.350))'), 4326));

-- FILA CENTRAL (lat -33.450 a -33.550)
INSERT INTO sectors (name, geometry) VALUES
('Zona Central Poniente', ST_SetSRID(ST_GeomFromText('POLYGON((-70.800 -33.450, -70.700 -33.450, -70.700 -33.550, -70.800 -33.550, -70.800 -33.450))'), 4326)),
('Zona Central Centro', ST_SetSRID(ST_GeomFromText('POLYGON((-70.700 -33.450, -70.600 -33.450, -70.600 -33.550, -70.700 -33.550, -70.700 -33.450))'), 4326)),
('Zona Central Oriente', ST_SetSRID(ST_GeomFromText('POLYGON((-70.600 -33.450, -70.500 -33.450, -70.500 -33.550, -70.600 -33.550, -70.600 -33.450))'), 4326));

-- FILA SUR (lat -33.550 a -33.650)
INSERT INTO sectors (name, geometry) VALUES
('Zona Sur Poniente', ST_SetSRID(ST_GeomFromText('POLYGON((-70.800 -33.550, -70.700 -33.550, -70.700 -33.650, -70.800 -33.650, -70.800 -33.550))'), 4326)),
('Zona Sur Centro', ST_SetSRID(ST_GeomFromText('POLYGON((-70.700 -33.550, -70.600 -33.550, -70.600 -33.650, -70.700 -33.650, -70.700 -33.550))'), 4326)),
('Zona Sur Oriente', ST_SetSRID(ST_GeomFromText('POLYGON((-70.600 -33.550, -70.500 -33.550, -70.500 -33.650, -70.600 -33.650, -70.600 -33.550))'), 4326));

-- Sector extra: Aeropuerto (norte extremo)
INSERT INTO sectors (name, geometry) VALUES
('Zona Aeropuerto', ST_SetSRID(ST_GeomFromText('POLYGON((-70.800 -33.250, -70.700 -33.250, -70.700 -33.350, -70.800 -33.350, -70.800 -33.250))'), 4326));

-- ============================================
-- INSERCIÓN DE TAREAS
-- ============================================

-- Tareas en Zona Central Centro (id=5)
INSERT INTO tasks (title, description, due_date, status, user_id, location, sector_id) VALUES
('Limpieza Plaza de Armas', 'Limpiar bancas y monumentos de la plaza principal.', '2026-08-15', 'COMPLETED', 1, ST_SetSRID(ST_MakePoint(-70.650, -33.500), 4326), 5),
('Revisión Extintores Municipalidad', 'Verificar carga de extintores en edificio consistorial.', '2026-07-20', 'COMPLETED', 1, ST_SetSRID(ST_MakePoint(-70.660, -33.480), 4326), 5),
('Ampolleta Pasillo Municipal', 'Cambiar luz quemada en pasillo del 3er piso.', '2026-07-20', 'PENDING', NULL, ST_SetSRID(ST_MakePoint(-70.640, -33.490), 4326), 5),
('Reparación Puerta Principal', 'Arreglar bisagra de la puerta de acceso principal.', '2026-08-30', 'PENDING', NULL, ST_SetSRID(ST_MakePoint(-70.650, -33.510), 4326), 5),
('Pintura Muro Perimetral', 'Pintar muro perimetral con pintura antihongos.', '2026-07-15', 'IN_PROGRESS', 7, ST_SetSRID(ST_MakePoint(-70.660, -33.520), 4326), 5);

-- Tareas en Zona Norte Centro (id=2)
INSERT INTO tasks (title, description, due_date, status, user_id, location, sector_id) VALUES
('Poda de Árboles Parque', 'Podar ramas altas de árboles en parque central.', '2026-08-05', 'PENDING', NULL, ST_SetSRID(ST_MakePoint(-70.650, -33.400), 4326), 2),
('Riego Automático Jardines', 'Reparar sistema de riego del bandejón central.', '2026-07-18', 'IN_PROGRESS', 8, ST_SetSRID(ST_MakePoint(-70.660, -33.380), 4326), 2);

-- Tareas en Zona Sur Centro (id=8)
INSERT INTO tasks (title, description, due_date, status, user_id, location, sector_id) VALUES
('Ronda Portón Sur', 'Cierre de acceso vehicular nocturno sector sur.', '2026-08-10', 'COMPLETED', 2, ST_SetSRID(ST_MakePoint(-70.650, -33.580), 4326), 8),
('Pintura Demarcación Estacionamiento', 'Pintar líneas amarillas en estacionamiento municipal.', '2026-09-15', 'PENDING', NULL, ST_SetSRID(ST_MakePoint(-70.660, -33.600), 4326), 8);

-- Tareas en Zona Sur Poniente (id=7)
INSERT INTO tasks (title, description, due_date, status, user_id, location, sector_id) VALUES
('Control pH Piscina Municipal', 'Medir y ajustar niveles de pH y cloro.', '2026-07-22', 'PENDING', NULL, ST_SetSRID(ST_MakePoint(-70.750, -33.580), 4326), 7);

-- Tareas en Zona Central Oriente (id=6)
INSERT INTO tasks (title, description, due_date, status, user_id, location, sector_id) VALUES
('Mantenimiento Luminarias', 'Revisar luminarias públicas del sector oriente.', '2026-08-01', 'COMPLETED', 1, ST_SetSRID(ST_MakePoint(-70.550, -33.500), 4326), 6),
('Reparación Veredas', 'Arreglar veredas rotas en avenida principal.', '2026-09-01', 'PENDING', NULL, ST_SetSRID(ST_MakePoint(-70.530, -33.480), 4326), 6);

-- Tareas en Zona Norte Poniente (id=1)
INSERT INTO tasks (title, description, due_date, status, user_id, location, sector_id) VALUES
('Limpieza Canal San Carlos', 'Retirar escombros y basura del canal.', '2026-07-30', 'PENDING', 3, ST_SetSRID(ST_MakePoint(-70.750, -33.400), 4326), 1);

-- Tareas en Zona Aeropuerto (id=10)
INSERT INTO tasks (title, description, due_date, status, user_id, location, sector_id) VALUES
('Mantenimiento Pista Auxiliar', 'Revisar señalización de pista auxiliar.', '2026-07-28', 'PENDING', NULL, ST_SetSRID(ST_MakePoint(-70.750, -33.300), 4326), 10);

-- Tareas FUERA de sectores
INSERT INTO tasks (title, description, due_date, status, user_id, location, sector_id) VALUES
('Basura Exterior Carretera', 'Recoger escombros de la vereda de la carretera.', '2026-08-21', 'PENDING', NULL, ST_SetSRID(ST_MakePoint(-70.850, -33.600), 4326), NULL),
('Mantenimiento Señalética Autopista', 'Revisar señaléticas de tránsito en autopista.', '2026-07-25', 'PENDING', 3, ST_SetSRID(ST_MakePoint(-70.480, -33.550), 4326), NULL);

-- Tareas completadas por varios usuarios (para estadísticas)
INSERT INTO tasks (title, description, due_date, status, user_id, location, sector_id) VALUES
('Limpieza Ventanas Edificio B', 'Lavado de ventanas exteriores del edificio B.', '2026-07-10', 'COMPLETED', 3, ST_SetSRID(ST_MakePoint(-70.650, -33.490), 4326), 5),
('Revisión Cámaras Seguridad Sur', 'Verificar funcionamiento de cámaras del perímetro sur.', '2026-07-08', 'COMPLETED', 2, ST_SetSRID(ST_MakePoint(-70.650, -33.580), 4326), 8),
('Fertilización Jardines Norte', 'Aplicar fertilizante en jardines del sector norte.', '2026-07-05', 'COMPLETED', 8, ST_SetSRID(ST_MakePoint(-70.650, -33.390), 4326), 2),
('Reparación Bomba Piscina', 'Cambiar filtro de la bomba principal de la piscina.', '2026-07-12', 'COMPLETED', 1, ST_SetSRID(ST_MakePoint(-70.750, -33.570), 4326), 7),
('Mantenimiento Portón Acceso', 'Lubricar rieles del portón de acceso principal.', '2026-07-11', 'COMPLETED', 7, ST_SetSRID(ST_MakePoint(-70.650, -33.510), 4326), 5);

-- Tareas EXPIRADAS
INSERT INTO tasks (title, description, due_date, status, user_id, location, sector_id) VALUES
('Reparación Ascensor Municipal', 'El ascensor del edificio A dejó de funcionar.', '2026-06-01', 'EXPIRED', 1, ST_SetSRID(ST_MakePoint(-70.650, -33.500), 4326), 5),
('Cambio Filtros Aire Acondicionado', 'Cambiar filtros de aire del lobby central.', '2026-06-15', 'EXPIRED', 4, ST_SetSRID(ST_MakePoint(-70.650, -33.590), 4326), 8),
('Desinfección Pasillos Comunes', 'Desinfección profunda de pasillos tras caso de gripe.', '2026-05-20', 'EXPIRED', NULL, ST_SetSRID(ST_MakePoint(-70.650, -33.400), 4326), 2),
('Reparación Barrera Entrada', 'La barrera automática no reconoce tags RFID.', '2026-06-10', 'EXPIRED', 2, ST_SetSRID(ST_MakePoint(-70.750, -33.580), 4326), 7),
('Limpieza Derrame Aceite', 'Mancha grande de aceite en espacio 34.', '2026-06-20', 'EXPIRED', NULL, ST_SetSRID(ST_MakePoint(-70.640, -33.520), 4326), 5),
('Cambio Tierra Maceteros', 'Renovar la tierra de los maceteros grandes.', '2026-06-05', 'EXPIRED', 8, ST_SetSRID(ST_MakePoint(-70.660, -33.390), 4326), 2),
('Reparación Filtro Piscina', 'El filtro principal hace ruido extraño.', '2026-06-18', 'EXPIRED', 1, ST_SetSRID(ST_MakePoint(-70.740, -33.570), 4326), 7),
('Poda Árbol Caído', 'Un árbol cayó sobre la vereda tras tormenta.', '2026-06-12', 'EXPIRED', 7, ST_SetSRID(ST_MakePoint(-70.560, -33.510), 4326), 6),
('Reparación Luminaria Calle', 'La luminaria pública parpadea constantemente.', '2026-06-25', 'EXPIRED', NULL, ST_SetSRID(ST_MakePoint(-70.760, -33.410), 4326), 1);

-- PENDING - Admin (Zona Central Centro - id=5)
INSERT INTO tasks (title, description, due_date, status, user_id, location, sector_id) VALUES
('Revisión Sistema Eléctrico', 'Verificar tableros eléctricos del edificio central.', '2026-09-10', 'PENDING', 1, ST_SetSRID(ST_MakePoint(-70.645, -33.495), 4326), 5),
('Instalación Cámaras Nuevas', 'Instalar 3 cámaras de seguridad en acceso norte.', '2026-08-25', 'PENDING', 1, ST_SetSRID(ST_MakePoint(-70.655, -33.505), 4326), 5);

-- IN_PROGRESS - Admin (Zona Central Centro - id=5)
INSERT INTO tasks (title, description, due_date, status, user_id, location, sector_id) VALUES
('Reparación Calefacción Central', 'Arreglar sistema de calefacción antes del invierno.', '2026-07-30', 'IN_PROGRESS', 1, ST_SetSRID(ST_MakePoint(-70.650, -33.500), 4326), 5);

-- COMPLETED - Admin (Zona Central Centro - id=5)
INSERT INTO tasks (title, description, due_date, status, user_id, location, sector_id) VALUES
('Pintura Oficinas Administrativas', 'Pintar oficinas del 2do piso color blanco humo.', '2026-07-01', 'COMPLETED', 1, ST_SetSRID(ST_MakePoint(-70.648, -33.502), 4326), 5),
('Mantenimiento Aire Acondicionado', 'Limpieza de filtros y revisión de gas refrigerante.', '2026-06-28', 'COMPLETED', 1, ST_SetSRID(ST_MakePoint(-70.652, -33.498), 4326), 5);

-- EXPIRED - Admin (Zona Central Centro - id=5)
INSERT INTO tasks (title, description, due_date, status, user_id, location, sector_id) VALUES
('Cambio Cerraduras Oficinas', 'Cambiar cerraduras de oficinas tras pérdida de llaves.', '2026-06-10', 'EXPIRED', 1, ST_SetSRID(ST_MakePoint(-70.650, -33.500), 4326), 5);

-- PENDING - Admin (Zona Norte Centro - id=2)
INSERT INTO tasks (title, description, due_date, status, user_id, location, sector_id) VALUES
('Limpieza Canaletas Techo', 'Limpiar canaletas de aguas lluvia del techo norte.', '2026-08-20', 'PENDING', 1, ST_SetSRID(ST_MakePoint(-70.650, -33.400), 4326), 2);

-- COMPLETED - Admin (Zona Norte Centro - id=2)
INSERT INTO tasks (title, description, due_date, status, user_id, location, sector_id) VALUES
('Cambio Ampolletas Pasillo Norte', 'Cambiar todas las ampolletas del pasillo norte a LED.', '2026-07-05', 'COMPLETED', 1, ST_SetSRID(ST_MakePoint(-70.645, -33.390), 4326), 2);

-- IN_PROGRESS - Admin (Zona Sur Centro - id=8)
INSERT INTO tasks (title, description, due_date, status, user_id, location, sector_id) VALUES
('Reparación Rejas Perimetrales', 'Soldar rejas rotas del perímetro sur.', '2026-08-01', 'IN_PROGRESS', 1, ST_SetSRID(ST_MakePoint(-70.650, -33.580), 4326), 8);

-- PENDING - Admin (Zona Central Oriente - id=6)
INSERT INTO tasks (title, description, due_date, status, user_id, location, sector_id) VALUES
('Mantenimiento Generador Eléctrico', 'Revisar generador de emergencia y cambiar aceite.', '2026-09-05', 'PENDING', 1, ST_SetSRID(ST_MakePoint(-70.550, -33.500), 4326), 6);

-- COMPLETED - Admin (Zona Sur Poniente - id=7)
INSERT INTO tasks (title, description, due_date, status, user_id, location, sector_id) VALUES
('Impermeabilización Terraza', 'Aplicar impermeabilizante en terraza del 5to piso.', '2026-07-08', 'COMPLETED', 1, ST_SetSRID(ST_MakePoint(-70.750, -33.580), 4326), 7);

-- EXPIRED - Admin (Zona Central Poniente - id=4)
INSERT INTO tasks (title, description, due_date, status, user_id, location, sector_id) VALUES
('Reparación Fuga Agua', 'Reparar fuga de agua en baño del 1er piso.', '2026-06-20', 'EXPIRED', 1, ST_SetSRID(ST_MakePoint(-70.750, -33.500), 4326), 4);

-- PENDING - Admin (Sin sector)
INSERT INTO tasks (title, description, due_date, status, user_id, location, sector_id) VALUES
('Coordinación Retiro Escombros', 'Coordinar con municipalidad retiro de escombros calle exterior.', '2026-08-15', 'PENDING', 1, ST_SetSRID(ST_MakePoint(-70.820, -33.550), 4326), NULL);

-- COMPLETED - Admin (Sin sector)
INSERT INTO tasks (title, description, due_date, status, user_id, location, sector_id) VALUES
('Informe Mensual Seguridad', 'Redactar informe mensual de novedades de seguridad.', '2026-07-01', 'COMPLETED', 1, ST_SetSRID(ST_MakePoint(-70.500, -33.600), 4326), NULL);

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