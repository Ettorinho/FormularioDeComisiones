-- Tabla para Comisiones/Grupos
CREATE TABLE comisiones (
    id BIGSERIAL PRIMARY KEY,
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
    id BIGSERIAL PRIMARY KEY,
    nombre_apellidos VARCHAR(100) NOT NULL,
    dni_nif VARCHAR(15) NOT NULL UNIQUE,
    correo_electronico VARCHAR(100) NOT NULL,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT check_email CHECK (correo_electronico ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);

-- Tabla para relacionar Comisiones con Miembros
CREATE TABLE comision_miembros (
    comision_id BIGINT NOT NULL,
    miembro_id BIGINT NOT NULL,
    cargo VARCHAR(30) NOT NULL,
    fecha_incorporacion DATE NOT NULL,
    fecha_baja DATE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (comision_id, miembro_id),
    FOREIGN KEY (comision_id) REFERENCES comisiones(id) ON DELETE CASCADE,
    FOREIGN KEY (miembro_id) REFERENCES miembros(id) ON DELETE CASCADE,
    CONSTRAINT check_cargo CHECK (cargo IN ('REFERENTE', 'RESPONSABLE', 'PRESIDENTE', 'MIEMBRO', 'SECRETARIO', 'PARTICIPANTE', 'INVESTIGADOR_PRINCIPAL', 'INVESTIGADOR_COLABORADOR'))
);

-- Tabla para Actas
CREATE TABLE actas (
    id BIGSERIAL PRIMARY KEY,
    comision_id BIGINT NOT NULL,
    fecha_reunion DATE NOT NULL,
    observaciones TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    pdf_nombre VARCHAR(255),
    pdf_contenido BYTEA,
    pdf_tipo_mime VARCHAR(100),
    FOREIGN KEY (comision_id) REFERENCES comisiones(id) ON DELETE CASCADE,
    CONSTRAINT check_fecha_reunion CHECK (fecha_reunion <= CURRENT_DATE)
);

-- Tabla para Asistencias a Actas
CREATE TABLE asistencias_actas (
    id BIGSERIAL PRIMARY KEY,
    acta_id BIGINT NOT NULL,
    miembro_id BIGINT NOT NULL,
    asistio BOOLEAN DEFAULT false NOT NULL,
    justificacion TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (acta_id) REFERENCES actas(id) ON DELETE CASCADE,
    FOREIGN KEY (miembro_id) REFERENCES miembros(id) ON DELETE CASCADE,
    CONSTRAINT asistencia_unica UNIQUE (acta_id, miembro_id)
);

-- Índices para mejorar el rendimiento
CREATE INDEX idx_comisiones_nombre ON comisiones (nombre);
CREATE INDEX idx_miembros_dni ON miembros (dni_nif);

-- Función para actualizar automáticamente fecha_modificacion
CREATE OR REPLACE FUNCTION actualizar_fecha_modificacion()
RETURNS TRIGGER AS $$
BEGIN
    NEW.fecha_modificacion = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Triggers para actualización automática de fecha_modificacion
CREATE TRIGGER trigger_comisiones_modificacion
BEFORE UPDATE ON comisiones
FOR EACH ROW
EXECUTE FUNCTION actualizar_fecha_modificacion();

CREATE TRIGGER trigger_miembros_modificacion
BEFORE UPDATE ON miembros
FOR EACH ROW
EXECUTE FUNCTION actualizar_fecha_modificacion();

CREATE TRIGGER trigger_actas_modificacion
BEFORE UPDATE ON actas
FOR EACH ROW
EXECUTE FUNCTION actualizar_fecha_modificacion();

CREATE TRIGGER trigger_comision_miembros_modificacion
BEFORE UPDATE ON comision_miembros
FOR EACH ROW
EXECUTE FUNCTION actualizar_fecha_modificacion();

CREATE TRIGGER trigger_asistencias_modificacion
BEFORE UPDATE ON asistencias_actas
FOR EACH ROW
EXECUTE FUNCTION actualizar_fecha_modificacion();