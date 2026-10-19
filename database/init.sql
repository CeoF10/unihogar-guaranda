-- Script de inicialización según modelo relacional (Anexo 3 del informe)

CREATE TABLE IF NOT EXISTS rol (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(30) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS campus (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(80) NOT NULL,
    latitud DECIMAL(10, 8) NOT NULL,
    longitud DECIMAL(11, 8) NOT NULL
);

CREATE TABLE IF NOT EXISTS usuario (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    hash_clave VARCHAR(255) NOT NULL,
    estado_email VARCHAR(20) DEFAULT 'PENDIENTE',
    telefono VARCHAR(20),
    fecha_alta TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS usuario_rol (
    usuario_id BIGINT REFERENCES usuario(id) ON DELETE CASCADE,
    rol_id INT REFERENCES rol(id) ON DELETE CASCADE,
    PRIMARY KEY (usuario_id, rol_id)
);

CREATE TABLE IF NOT EXISTS inmueble (
    id BIGSERIAL PRIMARY KEY,
    propietario_id BIGINT REFERENCES usuario(id) ON DELETE CASCADE,
    campus_id INT REFERENCES campus(id) ON DELETE SET NULL,
    titulo VARCHAR(120) NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    precio_mensual DECIMAL(10, 2) NOT NULL,
    deposito DECIMAL(10, 2) DEFAULT 0.00,
    servicios_declarados TEXT,
    latitud DECIMAL(10, 8) NOT NULL,
    longitud DECIMAL(11, 8) NOT NULL,
    estado VARCHAR(20) DEFAULT 'DISPONIBLE'
);

CREATE TABLE IF NOT EXISTS foto (
    id BIGSERIAL PRIMARY KEY,
    inmueble_id BIGINT REFERENCES inmueble(id) ON DELETE CASCADE,
    url_archivo VARCHAR(255) NOT NULL,
    orden INT DEFAULT 1,
    tipo VARCHAR(20) DEFAULT 'NORMAL'
);

CREATE TABLE IF NOT EXISTS solicitud_visita (
    id BIGSERIAL PRIMARY KEY,
    estudiante_id BIGINT REFERENCES usuario(id) ON DELETE CASCADE,
    inmueble_id BIGINT REFERENCES inmueble(id) ON DELETE CASCADE,
    fecha_propuesta TIMESTAMP NOT NULL,
    estado VARCHAR(25) DEFAULT 'PENDIENTE'
);

CREATE TABLE IF NOT EXISTS favorito (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT REFERENCES usuario(id) ON DELETE CASCADE,
    inmueble_id BIGINT REFERENCES inmueble(id) ON DELETE CASCADE,
    fecha_agregado TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (usuario_id, inmueble_id)
);

-- Datos semilla iniciales
INSERT INTO rol (id, nombre) VALUES (1, 'ROLE_ESTUDIANTE'), (2, 'ROLE_PROPIETARIO'), (3, 'ROLE_ADMIN') ON CONFLICT DO NOTHING;
INSERT INTO campus (id, nombre, latitud, longitud) VALUES 
(1, 'Campus Matriz Alpachaca', -1.591234, -79.004567),
(2, 'Campus Laguacoto', -1.602345, -78.995432)
ON CONFLICT DO NOTHING;
