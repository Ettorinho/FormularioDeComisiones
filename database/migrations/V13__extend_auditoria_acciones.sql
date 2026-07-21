-- ========================================
-- SCRIPT DE MIGRACIÓN: EXTENDER TABLA DE AUDITORÍA
-- Versión: 006
-- Fecha: 2026-05-11
-- Descripción: Añade columnas de seguridad y operacionales a auditoria_acciones:
--              user_agent, resultado, duracion_ms, mensaje_error, sesion_id
-- ========================================

-- IMPORTANTE: Hacer BACKUP antes de ejecutar
-- pg_dump -U postgres -d nombre_bd > backup_pre_migracion_006.sql

ALTER TABLE auditoria_acciones ADD COLUMN IF NOT EXISTS user_agent   VARCHAR(500);
ALTER TABLE auditoria_acciones ADD COLUMN IF NOT EXISTS resultado     VARCHAR(20)
    CHECK (resultado IN ('EXITOSO', 'FALLIDO', 'DENEGADO', 'VALIDACION_ERROR'));
ALTER TABLE auditoria_acciones ADD COLUMN IF NOT EXISTS duracion_ms  INTEGER;
ALTER TABLE auditoria_acciones ADD COLUMN IF NOT EXISTS mensaje_error TEXT;
ALTER TABLE auditoria_acciones ADD COLUMN IF NOT EXISTS sesion_id    VARCHAR(100);

COMMENT ON COLUMN auditoria_acciones.user_agent    IS 'Cabecera User-Agent del cliente HTTP';
COMMENT ON COLUMN auditoria_acciones.resultado     IS 'Resultado: EXITOSO, FALLIDO, DENEGADO, VALIDACION_ERROR';
COMMENT ON COLUMN auditoria_acciones.duracion_ms   IS 'Duración de la operación en milisegundos';
COMMENT ON COLUMN auditoria_acciones.mensaje_error IS 'Detalle del error si la operación falló';
COMMENT ON COLUMN auditoria_acciones.sesion_id     IS 'Identificador de sesión HTTP del usuario';

-- Índices para consultas de seguridad y monitoreo
CREATE INDEX IF NOT EXISTS idx_auditoria_ip_fecha          ON auditoria_acciones(ip_origen, fecha_hora DESC);
CREATE INDEX IF NOT EXISTS idx_auditoria_resultado         ON auditoria_acciones(resultado, fecha_hora DESC);
CREATE INDEX IF NOT EXISTS idx_auditoria_usuario_resultado ON auditoria_acciones(usuario, resultado);

-- Verificación
DO $$
DECLARE
    col_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO col_count
    FROM information_schema.columns
    WHERE table_name = 'auditoria_acciones'
      AND column_name IN ('user_agent', 'resultado', 'duracion_ms', 'mensaje_error', 'sesion_id');

    IF col_count = 5 THEN
        RAISE NOTICE '✅ Migración 006 completada: 5 nuevas columnas añadidas a auditoria_acciones';
    ELSE
        RAISE EXCEPTION '❌ Error: Se esperaban 5 nuevas columnas, se encontraron %', col_count;
    END IF;
END $$;
