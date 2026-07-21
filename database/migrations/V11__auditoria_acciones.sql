-- ========================================
-- SCRIPT DE MIGRACIÓN: TABLA DE AUDITORÍA CENTRALIZADA
-- Versión: 004
-- Fecha: 2026-03-17
-- Descripción: Crea la tabla auditoria_acciones para registrar todas
--              las acciones relevantes realizadas por usuarios en la app.
-- ========================================

-- IMPORTANTE: Hacer BACKUP antes de ejecutar
-- pg_dump -U postgres -d nombre_bd > backup_pre_migracion_004.sql

CREATE TABLE auditoria_acciones (
    id            BIGSERIAL    PRIMARY KEY,
    fecha_hora    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario       VARCHAR(100) NOT NULL,
    accion        VARCHAR(50)  NOT NULL,
    entidad       VARCHAR(50)  NOT NULL,
    entidad_id    VARCHAR(100),
    descripcion   TEXT,
    ip_origen     VARCHAR(45),
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE  auditoria_acciones              IS 'Registro centralizado de acciones de usuarios en la aplicación';
COMMENT ON COLUMN auditoria_acciones.usuario      IS 'Username AD del usuario que realizó la acción';
COMMENT ON COLUMN auditoria_acciones.accion       IS 'Tipo de acción: CREAR, MODIFICAR, ELIMINAR, BAJA, CONSULTAR';
COMMENT ON COLUMN auditoria_acciones.entidad      IS 'Entidad afectada: COMISION, MIEMBRO, ACTA, CARGO, SESION';
COMMENT ON COLUMN auditoria_acciones.entidad_id   IS 'Identificador del registro afectado (puede ser compuesto)';
COMMENT ON COLUMN auditoria_acciones.descripcion  IS 'Descripción legible del cambio realizado';
COMMENT ON COLUMN auditoria_acciones.ip_origen    IS 'Dirección IP del cliente que originó la petición';

-- Índices para consultas de auditoría frecuentes
CREATE INDEX idx_auditoria_usuario    ON auditoria_acciones(usuario);
CREATE INDEX idx_auditoria_fecha      ON auditoria_acciones(fecha_hora DESC);
CREATE INDEX idx_auditoria_entidad    ON auditoria_acciones(entidad, entidad_id);
CREATE INDEX idx_auditoria_accion     ON auditoria_acciones(accion);

-- Verificación
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'auditoria_acciones') THEN
        RAISE NOTICE '✅ Tabla auditoria_acciones creada correctamente';
    ELSE
        RAISE EXCEPTION '❌ Error: La tabla auditoria_acciones no se creó';
    END IF;
    RAISE NOTICE '✅ Migración 004 completada exitosamente';
END $$;
