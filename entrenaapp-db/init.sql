-- Crear tablas para EntrenaApp MVP

CREATE TABLE IF NOT EXISTS usuarios (
    id VARCHAR(36) PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    rol VARCHAR(20) DEFAULT 'ENTRENADOR',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS deportistas (
    id VARCHAR(36) PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    documento VARCHAR(20) UNIQUE NOT NULL,
    edad INT NOT NULL,
    disciplina VARCHAR(50) NOT NULL,
    foto_path TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS entrenamientos (
    id VARCHAR(36) PRIMARY KEY,
    fecha DATE NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    duracion_min INT NOT NULL,
    intensidad VARCHAR(20) NOT NULL,
    latitud DOUBLE PRECISION,
    longitud DOUBLE PRECISION,
    observacion_audio_path TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS asistencias (
    id VARCHAR(36) PRIMARY KEY,
    entrenamiento_id VARCHAR(36) REFERENCES entrenamientos(id) ON DELETE CASCADE,
    deportista_id VARCHAR(36) REFERENCES deportistas(id) ON DELETE CASCADE,
    asistio BOOLEAN NOT NULL DEFAULT TRUE,
    observacion TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);