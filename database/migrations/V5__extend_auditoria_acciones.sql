-- ========================================
-- SCRIPT DE MIGRACIÓN: EXTENSIÓN DE AUDITORÍA
-- Versión: V5
-- Fecha: 2026-05-11
-- Descripción: Añade metadatos de seguridad y operacionales a auditoria_acciones:
--              IP real (X-Forwarded-For), user-agent, resultado, duración y sesión.
-- ========================================

-- IMPORTANTE: Hacer BACKUP antes de ejecutar
-- pg_dump -U postgres -d nombre_bd > backup_pre_migracion_V5.sql

-- Añadir nuevas columnas a la tabla existente
ALTER TABLE auditoria_acciones
    ADD COLUMN IF NOT EXISTS user_agent   VARCHAR(500),
    ADD COLUMN IF NOT EXISTS resultado    VARCHAR(20)  NOT NULL DEFAULT 'EXITOSO',
    ADD COLUMN IF NOT EXISTS duracion_ms  INTEGER,
    ADD COLUMN IF NOT EXISTS mensaje_error TEXT,
    ADD COLUMN IF NOT EXISTS sesion_id   VARCHAR(100);

COMMENT ON COLUMN auditoria_acciones.user_agent    IS 'Identificación del navegador/cliente HTTP';
COMMENT ON COLUMN auditoria_acciones.resultado     IS 'Resultado de la operación: EXITOSO, FALLIDO, DENEGADO';
COMMENT ON COLUMN auditoria_acciones.duracion_ms   IS 'Tiempo de ejecución de la operación en milisegundos';
COMMENT ON COLUMN auditoria_acciones.mensaje_error IS 'Mensaje de error detallado (cuando resultado = FALLIDO)';
COMMENT ON COLUMN auditoria_acciones.sesion_id     IS 'ID de sesión HTTP para correlación de actividad';

-- Índices para consultas de seguridad y rendimiento frecuentes
CREATE INDEX IF NOT EXISTS idx_auditoria_resultado       ON auditoria_acciones(resultado);
CREATE INDEX IF NOT EXISTS idx_auditoria_fecha_resultado ON auditoria_acciones(fecha_hora, resultado);
CREATE INDEX IF NOT EXISTS idx_auditoria_sesion          ON auditoria_acciones(sesion_id);

-- Vista para operaciones fallidas/denegadas (útil en dashboards de seguridad)
CREATE OR REPLACE VIEW auditoria_fallos AS
SELECT
    id,
    fecha_hora,
    usuario,
    accion,
    entidad,
    entidad_id,
    ip_origen,
    resultado,
    mensaje_error
FROM auditoria_acciones
WHERE resultado IN ('FALLIDO', 'DENEGADO')
ORDER BY fecha_hora DESC;

-- Vista para monitoreo de seguridad: logins fallidos y accesos denegados
CREATE OR REPLACE VIEW auditoria_seguridad AS
SELECT
    usuario,
    ip_origen,
    user_agent,
    accion,
    resultado,
    fecha_hora,
    COUNT(*) OVER (
        PARTITION BY ip_origen, usuario
        ORDER BY fecha_hora
        RANGE BETWEEN INTERVAL '10 minutes' PRECEDING AND CURRENT ROW
    ) AS intentos_recientes
FROM auditoria_acciones
WHERE accion IN ('LOGIN_FALLIDO', 'ACCESS_DENIED', 'UNAUTHORIZED')
  AND resultado = 'FALLIDO'
ORDER BY fecha_hora DESC;

-- Verificación
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'auditoria_acciones' AND column_name = 'resultado'
    ) THEN
        RAISE NOTICE '✅ Columnas de extensión de auditoría añadidas correctamente';
    ELSE
        RAISE EXCEPTION '❌ Error: Las columnas de extensión no se crearon';
    END IF;
    RAISE NOTICE '✅ Migración V5 completada exitosamente';
END $$;
