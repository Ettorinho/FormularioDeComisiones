-- Tabla para Comisiones/Grupos
CREATE TABLE comisiones (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    fecha_constitucion DATE NOT NULL,
    fecha_fin DATE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_comision_nombre UNIQUE (nombre),
    CONSTRAINT check_fechas CHECK (fecha_fin IS NULL OR fecha_fin >= fecha_constitucion)
);

-- Tabla para Miembros
CREATE TABLE miembros (
    id SERIAL PRIMARY KEY,
    nombre_apellidos VARCHAR(100) NOT NULL,
    dni_nif VARCHAR(10) NOT NULL UNIQUE,
    correo_electronico VARCHAR(100) NOT NULL,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla para relacionar Comisiones con Miembros
CREATE TABLE comision_miembros (
    comision_id INTEGER NOT NULL,
    miembro_id INTEGER NOT NULL,
    cargo VARCHAR(20) NOT NULL,
    fecha_incorporacion DATE NOT NULL,
    PRIMARY KEY (comision_id, miembro_id),
    FOREIGN KEY (comision_id) REFERENCES comisiones(id) ON DELETE CASCADE,
    FOREIGN KEY (miembro_id) REFERENCES miembros(id) ON DELETE CASCADE,
    CONSTRAINT check_cargo CHECK (cargo IN ('REFERENTE', 'RESPONSABLE', 'PRESIDENTE', 'MIEMBRO', 'SECRETARIO'))
);

-- Índices para mejorar el rendimiento
CREATE INDEX idx_comisiones_nombre ON comisiones (nombre);
CREATE INDEX idx_miembros_dni ON miembros (dni_nif);