-- LIMPIEZA DE DATOS
TRUNCATE TABLE tasks, sectors, users RESTART IDENTITY;

-- USUARIOS
-- Posicionados en distintos puntos de La Cisterna
INSERT INTO users (username, password, location, status) VALUES 
('admin', 'pass123', ST_SetSRID(ST_MakePoint(-70.6652, -33.5281), 4326), 'ACTIVE'),
('sergio_guardia', 'pass123', ST_SetSRID(ST_MakePoint(-70.6610, -33.5310), 4326), 'ACTIVE'),
('maria_conserje', 'pass123', ST_SetSRID(ST_MakePoint(-70.6685, -33.5255), 4326), 'ACTIVE');

-- SECTORES (Polígonos)
-- Sector 1: Condominio Principal
INSERT INTO sectors (name, geometry) VALUES 
('Condominio Albano 2', ST_SetSRID(ST_GeomFromText('POLYGON((-70.668 -33.530, -70.660 -33.530, -70.660 -33.525, -70.668 -33.525, -70.668 -33.530))'), 4326));

-- Sector 2: Zona de Estacionamientos
INSERT INTO sectors (name, geometry) VALUES 
('Estacionamientos Sur', ST_SetSRID(ST_GeomFromText('POLYGON((-70.663 -33.534, -70.661 -33.534, -70.661 -33.532, -70.663 -33.532, -70.663 -33.534))'), 4326));

-- Tareas dentro de 'Condominio Albano 2' (Sector 1)
INSERT INTO tasks (title, description, due_date, status, user_id, location) VALUES 
('Limpieza Hall Torre A', 'Limpiar vidrios entrada', '2026-05-10', 'COMPLETED', 1, ST_SetSRID(ST_MakePoint(-70.665, -33.528), 4326)),
('Revisión Extintores', 'Verificar carga piso 5', '2026-05-12', 'COMPLETED', 1, ST_SetSRID(ST_MakePoint(-70.664, -33.527), 4326)),
('Ampolleta Pasillo', 'Cambiar luz quemada', '2026-05-20', 'PENDING', NULL, ST_SetSRID(ST_MakePoint(-70.666, -33.529), 4326));

-- Tareas dentro de 'Estacionamientos Sur' (Sector 2)
INSERT INTO tasks (title, description, due_date, status, user_id, location) VALUES 
('Ronda Portón', 'Cierre de acceso vehicular', '2026-05-14', 'COMPLETED', 2, ST_SetSRID(ST_MakePoint(-70.662, -33.533), 4326)),
('Pintura Demarcación', 'Pintar líneas amarillas', '2026-05-25', 'PENDING', NULL, ST_SetSRID(ST_MakePoint(-70.6615, -33.5335), 4326));

-- Tarea fuera de los sectores definidos
INSERT INTO tasks (title, description, due_date, status, user_id, location) VALUES 
('Basura Exterior', 'Recoger escombros vereda', '2026-05-21', 'PENDING', NULL, ST_SetSRID(ST_MakePoint(-70.675, -33.540), 4326));