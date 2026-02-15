-- =========================
-- DATOS DE PRUEBA
-- =========================

USE futbol_app;

-- Insertar usuarios de prueba
-- Nota: Las contraseñas deben estar encriptadas con BCrypt en producción
-- Estas son contraseñas de ejemplo (password: "admin123" y "user123")
INSERT INTO users (email, password, role) VALUES
('admin@futbol.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J2sOVUPbLvE1BgGzWV.FKv7z7X/w2C', 'admin'),
('usuario1@futbol.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J2sOVUPbLvE1BgGzWV.FKv7z7X/w2C', 'user'),
('usuario2@futbol.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J2sOVUPbLvE1BgGzWV.FKv7z7X/w2C', 'user');

-- Insertar equipos de prueba
INSERT INTO equipos (id_user, nombre, votos, alineacion) VALUES
(2, 'Los Galácticos', 15, '{"formacion": "4-3-3", "entrenador": "Zidane", "posicionJugador": ["Casillas", "Ramos", "Pique", "Marcelo", "Alves", "Xavi", "Iniesta", "Busquets", "Messi", "Ronaldo", "Neymar"], "reservas": ["Buffon", "Puyol"]}'),
(2, 'Dream Team', 8, '{"formacion": "4-4-2", "entrenador": "Guardiola", "posicionJugador": ["Neuer", "Lahm", "Boateng", "Hummels", "Alaba", "Kroos", "Modric", "Muller", "Robben", "Lewandowski", "Benzema"], "reservas": ["Ter Stegen"]}'),
(3, 'Los Invencibles', 22, '{"formacion": "3-5-2", "entrenador": "Mourinho", "posicionJugador": ["De Gea", "Chiellini", "Van Dijk", "Silva", "Kante", "De Bruyne", "Pogba", "Salah", "Hazard", "Suarez", "Aguero"], "reservas": ["Courtois", "Varane"]}');

-- Insertar partidos de prueba
INSERT INTO partidos (equipo_local_id, equipo_visitante_id, fecha, creado_por) VALUES
(1, 2, '2026-02-15 18:00:00', 1),
(2, 3, '2026-02-20 20:00:00', 2),
(1, 3, '2026-02-25 19:00:00', 1);

-- Insertar alineaciones para partidos
INSERT INTO alineaciones (partido_id, equipo_id, alineacion) VALUES
(1, 1, '{"formacion": "4-3-3", "entrenador": "Zidane", "posicionJugador": ["Casillas", "Ramos", "Pique", "Marcelo", "Alves", "Xavi", "Iniesta", "Busquets", "Messi", "Ronaldo", "Neymar"], "reservas": ["Buffon"]}'),
(1, 2, '{"formacion": "4-4-2", "entrenador": "Guardiola", "posicionJugador": ["Neuer", "Lahm", "Boateng", "Hummels", "Alaba", "Kroos", "Modric", "Muller", "Robben", "Lewandowski", "Benzema"], "reservas": ["Ter Stegen"]}');

-- Insertar comentarios de prueba
INSERT INTO comentarios (equipo_id, mensaje, responde_a) VALUES
(1, '¡Increíble alineación! Los mejores de todos los tiempos.', NULL),
(1, 'Estoy de acuerdo, pero falta Di Stefano.', 1),
(2, 'El Dream Team tiene la mejor defensa.', NULL),
(3, 'Los Invencibles son mi favorito, tienen el equilibrio perfecto.', NULL);

-- Insertar APIs de prueba (para tracking de integraciones)
INSERT INTO apis (nombre, id_api) VALUES
('SportsMonks Football API', 'sportmonks_football_v3'),
('API-Football RapidAPI', 'api_football_rapid'),
('TheSportsDB', 'thesportsdb_v1');
