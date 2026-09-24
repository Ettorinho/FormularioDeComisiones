-- =====================================================================
-- ESQUEMA CONSOLIDADO — Creación completa de la base de datos desde cero
-- Generado a partir del estado final de V1..V21 (database/migrations/)
-- Fecha de generación: 2026-09-24
--
-- USO:
--   Este script crea el esquema completo y actualizado en una base de
--   datos VACÍA (nueva), sin necesidad de aplicar las migraciones V1..V21
--   una a una. Es el equivalente a haber aplicado toda la cadena de
--   migraciones hasta V21 inclusive, incluyendo todos los tipos ENUM en
--   su forma final, todas las tablas, columnas, constraints, índices,
--   funciones y triggers vigentes.
--
--   NO usar sobre una base de datos que ya tenga aplicadas migraciones
--   Flyway previas (V1..V21): en ese caso sigue usando el flujo normal
--   de migraciones incrementales. Este script es solo para instalaciones
--   nuevas / entornos de desarrollo / recreación tras vaciado de pruebas.
--
--   Si usas Flyway y quieres que reconozca este script como baseline,
--   ejecuta después:
--     flyway baseline -baselineVersion=21
-- =====================================================================

BEGIN;

-- ========================================
-- TIPOS ENUM (estado final tras V8, V15)
-- ========================================

CREATE TYPE area_type AS ENUM (
    'ATENCION_ESPECIALIZADA',
    'ATENCION_PRIMARIA',
    'MIXTA'
);
COMMENT ON TYPE area_type IS 'Tipos de área: ATENCION_ESPECIALIZADA, ATENCION_PRIMARIA, MIXTA';

CREATE TYPE tipo_type AS ENUM (
    'COMISION',
    'GRUPO_TRABAJO',
    'GRUPO_MEJORA'
);
COMMENT ON TYPE tipo_type IS 'Tipos de comisión: COMISION, GRUPO_TRABAJO, GRUPO_MEJORA';

CREATE TYPE cargo_type AS ENUM (
    'REFERENTE',
    'RESPONSABLE',
    'PRESIDENTE',
    'PARTICIPANTE',
    'SECRETARIO',
    'INVESTIGADOR_PRINCIPAL',
    'INVESTIGADOR_COLABORADOR',
    'FIRMANTE'
);
COMMENT ON TYPE cargo_type IS 'Tipos de cargo en comisión: REFERENTE, RESPONSABLE, PRESIDENTE, PARTICIPANTE, SECRETARIO, INVESTIGADOR_PRINCIPAL, INVESTIGADOR_COLABORADOR, FIRMANTE';

-- ========================================
-- TABLA: comisiones (nombre ampliado a VARCHAR(200) por V18)
-- ========================================

CREATE TABLE comisiones (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
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
-- TABLA: miembros (nombre_apellidos ampliado a VARCHAR(200) por V18)
-- ========================================

CREATE TABLE miembros (
    id BIGSERIAL PRIMARY KEY,
    nombre_apellidos VARCHAR(200) NOT NULL,
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
    cargo cargo_type NOT NULL,
    fecha_incorporacion DATE NOT NULL,
    fecha_baja DATE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (comision_id, miembro_id),
    FOREIGN KEY (comision_id) REFERENCES comisiones(id) ON DELETE CASCADE,
    FOREIGN KEY (miembro_id) REFERENCES miembros(id) ON DELETE CASCADE
);

-- ========================================
-- TABLA: actas (incluye titulo de V14 y campos oficiales de V21)
-- ========================================

CREATE TABLE actas (
    id BIGSERIAL PRIMARY KEY,
    comision_id BIGINT NOT NULL,
    titulo VARCHAR(200),
    fecha_reunion DATE NOT NULL,
    observaciones TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    pdf_nombre VARCHAR(255),
    pdf_contenido BYTEA,
    pdf_tipo_mime VARCHAR(100),
    -- Campos del acta oficial (V21)
    hora_inicio VARCHAR(5),
    hora_fin VARCHAR(5),
    duracion VARCHAR(100),
    tipo_reunion VARCHAR(20),
    tipo_reunion_otros_detalle TEXT,
    orden_dia TEXT,
    excusa_asistencia TEXT,
    FOREIGN KEY (comision_id) REFERENCES comisiones(id) ON DELETE CASCADE,
    CONSTRAINT check_fecha_reunion CHECK (fecha_reunion <= CURRENT_DATE)
);

COMMENT ON COLUMN actas.titulo IS 'Título descriptivo del acta de reunión';

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
-- (cargo_anterior/cargo_nuevo ya como ENUM tras V12, con FIRMANTE de V15)
-- ========================================

CREATE TABLE comision_miembro_historial_cargos (
    id BIGSERIAL PRIMARY KEY,
    comision_id BIGINT NOT NULL,
    miembro_id BIGINT NOT NULL,
    cargo_anterior cargo_type,
    cargo_nuevo cargo_type NOT NULL,
    fecha_cambio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    motivo TEXT,
    usuario_modificacion VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) DEFAULT 'SYSTEM',

    FOREIGN KEY (comision_id, miembro_id)
        REFERENCES comision_miembros(comision_id, miembro_id)
        ON DELETE CASCADE,

    CONSTRAINT check_cargo_diferente
        CHECK (cargo_anterior IS NULL OR cargo_anterior != cargo_nuevo)
);

COMMENT ON TABLE comision_miembro_historial_cargos IS 'Historial completo de cambios de cargo de miembros en comisiones';
COMMENT ON COLUMN comision_miembro_historial_cargos.comision_id IS 'ID de la comisión (parte de clave compuesta)';
COMMENT ON COLUMN comision_miembro_historial_cargos.miembro_id IS 'ID del miembro (parte de clave compuesta)';
COMMENT ON COLUMN comision_miembro_historial_cargos.cargo_anterior IS 'Cargo antes del cambio, NULL en primer registro (tipo ENUM cargo_type)';
COMMENT ON COLUMN comision_miembro_historial_cargos.cargo_nuevo IS 'Cargo después del cambio (tipo ENUM cargo_type)';
COMMENT ON COLUMN comision_miembro_historial_cargos.fecha_cambio IS 'Timestamp exacto del cambio';
COMMENT ON COLUMN comision_miembro_historial_cargos.motivo IS 'Motivo opcional del cambio de cargo';
COMMENT ON COLUMN comision_miembro_historial_cargos.usuario_modificacion IS 'Usuario que realizó el cambio';

-- ========================================
-- TABLA: auditoria_acciones (V11 + columnas extendidas de V13)
-- ========================================

CREATE TABLE auditoria_acciones (
    id            BIGSERIAL    PRIMARY KEY,
    fecha_hora    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario       VARCHAR(100) NOT NULL,
    accion        VARCHAR(50)  NOT NULL,
    entidad       VARCHAR(50)  NOT NULL,
    entidad_id    VARCHAR(100),
    descripcion   TEXT,
    ip_origen     VARCHAR(45),
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_agent    VARCHAR(500),
    resultado     VARCHAR(20)
        CHECK (resultado IN ('EXITOSO', 'FALLIDO', 'DENEGADO', 'VALIDACION_ERROR')),
    duracion_ms   INTEGER,
    mensaje_error TEXT,
    sesion_id     VARCHAR(100)
);

COMMENT ON TABLE  auditoria_acciones              IS 'Registro centralizado de acciones de usuarios en la aplicación';
COMMENT ON COLUMN auditoria_acciones.usuario       IS 'Username AD del usuario que realizó la acción';
COMMENT ON COLUMN auditoria_acciones.accion        IS 'Tipo de acción: CREAR, MODIFICAR, ELIMINAR, BAJA, CONSULTAR';
COMMENT ON COLUMN auditoria_acciones.entidad       IS 'Entidad afectada: COMISION, MIEMBRO, ACTA, CARGO, SESION';
COMMENT ON COLUMN auditoria_acciones.entidad_id    IS 'Identificador del registro afectado (puede ser compuesto)';
COMMENT ON COLUMN auditoria_acciones.descripcion   IS 'Descripción legible del cambio realizado';
COMMENT ON COLUMN auditoria_acciones.ip_origen     IS 'Dirección IP del cliente que originó la petición';
COMMENT ON COLUMN auditoria_acciones.user_agent    IS 'Cabecera User-Agent del cliente HTTP';
COMMENT ON COLUMN auditoria_acciones.resultado     IS 'Resultado: EXITOSO, FALLIDO, DENEGADO, VALIDACION_ERROR';
COMMENT ON COLUMN auditoria_acciones.duracion_ms   IS 'Duración de la operación en milisegundos';
COMMENT ON COLUMN auditoria_acciones.mensaje_error IS 'Detalle del error si la operación falló';
COMMENT ON COLUMN auditoria_acciones.sesion_id     IS 'Identificador de sesión HTTP del usuario';

-- ========================================
-- ÍNDICES
-- ========================================

-- comisiones
CREATE INDEX idx_comisiones_nombre ON comisiones (nombre);
CREATE INDEX idx_comisiones_area ON comisiones (area);
CREATE INDEX idx_comisiones_tipo ON comisiones (tipo);
CREATE INDEX idx_comisiones_lower_nombre ON comisiones (LOWER(nombre)); -- V20

-- actas (nombres finales tras V7, que eliminó los duplicados idx_acta_*)
CREATE INDEX idx_actas_comision ON actas (comision_id);
CREATE INDEX idx_actas_fecha ON actas (fecha_reunion DESC);
CREATE INDEX idx_actas_titulo ON actas (titulo); -- V14

-- miembros (dni_nif ya tiene UNIQUE index automático; V7 eliminó el duplicado idx_miembros_dni)

-- comision_miembros
CREATE INDEX idx_comision_miembros_miembro_id_activo
    ON comision_miembros (miembro_id)
    WHERE fecha_baja IS NULL; -- V19

-- historial de cargos
CREATE INDEX idx_historial_cargos_comision_miembro
    ON comision_miembro_historial_cargos(comision_id, miembro_id);

CREATE INDEX idx_historial_cargos_fecha
    ON comision_miembro_historial_cargos(fecha_cambio DESC);

CREATE INDEX idx_historial_cargos_usuario
    ON comision_miembro_historial_cargos(usuario_modificacion);

-- auditoria_acciones
CREATE INDEX idx_auditoria_usuario ON auditoria_acciones(usuario);
CREATE INDEX idx_auditoria_fecha ON auditoria_acciones(fecha_hora DESC);
CREATE INDEX idx_auditoria_entidad ON auditoria_acciones(entidad, entidad_id);
CREATE INDEX idx_auditoria_accion ON auditoria_acciones(accion);
CREATE INDEX idx_auditoria_ip_fecha ON auditoria_acciones(ip_origen, fecha_hora DESC); -- V13
CREATE INDEX idx_auditoria_resultado ON auditoria_acciones(resultado, fecha_hora DESC); -- V13
CREATE INDEX idx_auditoria_usuario_resultado ON auditoria_acciones(usuario, resultado); -- V13
CREATE INDEX idx_auditoria_usuario_fecha ON auditoria_acciones(usuario, fecha_hora DESC); -- V17

-- ========================================
-- FUNCIONES
-- ========================================

-- Actualiza automáticamente fecha_modificacion en cada UPDATE
CREATE OR REPLACE FUNCTION actualizar_fecha_modificacion()
RETURNS TRIGGER AS $$
BEGIN
    NEW.fecha_modificacion = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION actualizar_fecha_modificacion() IS 'Actualiza automáticamente el campo fecha_modificacion';

-- Registra cambios de cargo en el historial (versión final tras V16:
-- inserta directamente los valores ENUM sin cast a VARCHAR, y lee el
-- usuario AD desde la variable de sesión app.usuario_modificacion,
-- establecida por la aplicación Java antes de cada UPDATE de cargo)
CREATE OR REPLACE FUNCTION registrar_cambio_cargo()
RETURNS TRIGGER AS $$
BEGIN
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
            COALESCE(
                NULLIF(current_setting('app.usuario_modificacion', true), ''),
                current_user,
                'SYSTEM'
            )
        );
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION registrar_cambio_cargo() IS
    'Registra automáticamente cambios de cargo en el historial, insertando '
    'directamente los valores ENUM cargo_type (sin cast a VARCHAR) y leyendo '
    'el usuario AD desde la variable de sesión app.usuario_modificacion '
    'establecida por la aplicación web antes de cada UPDATE.';

-- ========================================
-- TRIGGERS
-- ========================================

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

CREATE TRIGGER trigger_cambio_cargo
    AFTER UPDATE OF cargo ON comision_miembros
    FOR EACH ROW
    WHEN (OLD.cargo IS DISTINCT FROM NEW.cargo)
    EXECUTE FUNCTION registrar_cambio_cargo();

COMMENT ON TRIGGER trigger_cambio_cargo ON comision_miembros IS 'Trigger que registra cambios de cargo automáticamente';

-- ========================================
-- VERIFICACIÓN FINAL
-- ========================================

DO $$
DECLARE
    tablas_esperadas TEXT[] := ARRAY[
        'comisiones', 'miembros', 'comision_miembros', 'actas',
        'asistencias_actas', 'comision_miembro_historial_cargos',
        'auditoria_acciones'
    ];
    tabla TEXT;
BEGIN
    FOREACH tabla IN ARRAY tablas_esperadas LOOP
        IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = tabla) THEN
            RAISE EXCEPTION '❌ Error: la tabla % no se creó correctamente', tabla;
        END IF;
    END LOOP;

    RAISE NOTICE '✅ Esquema completo creado correctamente (equivalente a V1..V21 aplicadas)';
END $$;

COMMIT;
