-- ========================================
-- ESQUEMA COMPLETO DE BASE DE DATOS
-- Incluye todas las migraciones: 001, 002 y 003
-- Fecha: 2026-02-11
-- ========================================

-- ========================================
-- TIPOS ENUM
-- ========================================

-- Tipo para área de comisión
CREATE TYPE area_type AS ENUM ('ATENCION_ESPECIALIZADA', 'ATENCION_PRIMARIA', 'MIXTA');

-- Tipo para tipo de comisión
CREATE TYPE tipo_type AS ENUM ('COMISION', 'GRUPO_TRABAJO', 'GRUPO_MEJORA');

-- ========================================
-- TABLA: comisiones
-- ========================================

CREATE TABLE comisiones (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    area area_type NOT NULL,
    tipo tipo_type NOT NULL,
    fecha_constitucion DATE NOT NULL,
    fecha_fin DATE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_comision_nombre_area_tipo UNIQUE (nombre, area, tipo),
    CONSTRAINT check_fechas CHECK (fecha_fin IS NULL OR fecha_fin >= fecha_constitucion)
);

-- ========================================
-- TABLA: miembros
-- ========================================

CREATE TABLE miembros (
    id BIGSERIAL PRIMARY KEY,
    nombre_apellidos VARCHAR(100) NOT NULL,
    dni_nif VARCHAR(15) NOT NULL UNIQUE,
    correo_electronico VARCHAR(100) NOT NULL,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT check_email CHECK (correo_electronico ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);

-- ========================================
-- TABLA: comision_miembros
-- ========================================

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

-- ========================================
-- TABLA: actas
-- ========================================

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

-- ========================================
-- TABLA: asistencias_actas
-- ========================================

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

-- ========================================
-- TABLA: comision_miembro_historial_cargos
-- (De migración 002)
-- ========================================

CREATE TABLE comision_miembro_historial_cargos (
    id BIGSERIAL PRIMARY KEY,
    comision_id BIGINT NOT NULL,
    miembro_id BIGINT NOT NULL,
    cargo_anterior VARCHAR(100),
    cargo_nuevo VARCHAR(100) NOT NULL,
    fecha_cambio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    motivo TEXT,
    usuario_modificacion VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) DEFAULT 'SYSTEM',
    
    -- Referencia a la tabla con clave compuesta
    FOREIGN KEY (comision_id, miembro_id) 
        REFERENCES comision_miembros(comision_id, miembro_id) 
        ON DELETE CASCADE,
    
    -- Validar que el cargo cambió realmente
    CONSTRAINT check_cargo_diferente 
        CHECK (cargo_anterior IS NULL OR cargo_anterior != cargo_nuevo)
);

-- ========================================
-- ÍNDICES
-- ========================================

-- Índices para comisiones
CREATE INDEX idx_comisiones_nombre ON comisiones (nombre);
CREATE INDEX idx_comisiones_area ON comisiones (area);
CREATE INDEX idx_comisiones_tipo ON comisiones (tipo);

-- Índices para miembros
CREATE INDEX idx_miembros_dni ON miembros (dni_nif);

-- Índices para historial de cargos
CREATE INDEX idx_historial_cargos_comision_miembro 
    ON comision_miembro_historial_cargos(comision_id, miembro_id);

CREATE INDEX idx_historial_cargos_fecha 
    ON comision_miembro_historial_cargos(fecha_cambio DESC);

CREATE INDEX idx_historial_cargos_usuario 
    ON comision_miembro_historial_cargos(usuario_modificacion);

-- ========================================
-- FUNCIONES
-- ========================================

-- Función para actualizar automáticamente fecha_modificacion
CREATE OR REPLACE FUNCTION actualizar_fecha_modificacion()
RETURNS TRIGGER AS $$
BEGIN
    NEW.fecha_modificacion = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Función para registrar cambios de cargo (De migración 002)
CREATE OR REPLACE FUNCTION registrar_cambio_cargo()
RETURNS TRIGGER AS $$
BEGIN
    -- Solo registrar si el cargo realmente cambió
    IF OLD.cargo IS DISTINCT FROM NEW.cargo THEN
        INSERT INTO comision_miembro_historial_cargos (
            comision_id,
            miembro_id,
            cargo_anterior,
            cargo_nuevo,
            fecha_cambio,
            usuario_modificacion
        ) VALUES (
            NEW.comision_id,
            NEW.miembro_id,
            OLD.cargo,
            NEW.cargo,
            CURRENT_TIMESTAMP,
            COALESCE(current_user, 'SYSTEM')
        );
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ========================================
-- TRIGGERS
-- ========================================

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

-- Trigger para registrar cambios de cargo (De migración 002)
CREATE TRIGGER trigger_cambio_cargo
    AFTER UPDATE OF cargo ON comision_miembros
    FOR EACH ROW
    WHEN (OLD.cargo IS DISTINCT FROM NEW.cargo)
    EXECUTE FUNCTION registrar_cambio_cargo();

-- ========================================
-- COMENTARIOS
-- ========================================

COMMENT ON TYPE area_type IS 'Tipos de área: ATENCION_ESPECIALIZADA, ATENCION_PRIMARIA, MIXTA';
COMMENT ON TYPE tipo_type IS 'Tipos de comisión: COMISION, GRUPO_TRABAJO, GRUPO_MEJORA';

COMMENT ON TABLE comision_miembro_historial_cargos IS 'Historial completo de cambios de cargo de miembros en comisiones';
COMMENT ON COLUMN comision_miembro_historial_cargos.comision_id IS 'ID de la comisión (parte de clave compuesta)';
COMMENT ON COLUMN comision_miembro_historial_cargos.miembro_id IS 'ID del miembro (parte de clave compuesta)';
COMMENT ON COLUMN comision_miembro_historial_cargos.cargo_anterior IS 'Cargo antes del cambio (NULL en primer registro)';
COMMENT ON COLUMN comision_miembro_historial_cargos.cargo_nuevo IS 'Cargo después del cambio';
COMMENT ON COLUMN comision_miembro_historial_cargos.fecha_cambio IS 'Timestamp exacto del cambio';
COMMENT ON COLUMN comision_miembro_historial_cargos.motivo IS 'Motivo opcional del cambio de cargo';
COMMENT ON COLUMN comision_miembro_historial_cargos.usuario_modificacion IS 'Usuario que realizó el cambio';

COMMENT ON FUNCTION actualizar_fecha_modificacion() IS 'Actualiza automáticamente el campo fecha_modificacion';
COMMENT ON FUNCTION registrar_cambio_cargo() IS 'Registra automáticamente cambios de cargo en el historial';

COMMENT ON TRIGGER trigger_cambio_cargo ON comision_miembros IS 'Trigger que registra cambios de cargo automáticamente';