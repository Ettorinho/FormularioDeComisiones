-- ========================================
-- SCRIPT DE MIGRACIÓN: EXTENSIÓN DE AUDITORÍA
-- Versión: 006
-- Fecha: 2026-05-11
-- Descripción: Extiende la tabla auditoria_acciones con campos de seguridad:
--              user_agent, session_id, resultado, metodo_http, url_solicitada,
--              duracion_ms y mensaje_error. Añade índices de análisis.
-- ========================================

-- IMPORTANTE: Hacer BACKUP antes de ejecutar
-- pg_dump -U postgres -d nombre_bd > backup_pre_migracion_006.sql

-- ----------------------------------------
-- 1. Nuevas columnas
-- ----------------------------------------
ALTER TABLE auditoria_acciones
    ADD COLUMN IF NOT EXISTS user_agent     VARCHAR(500),
    ADD COLUMN IF NOT EXISTS session_id     VARCHAR(100),
    ADD COLUMN IF NOT EXISTS resultado      VARCHAR(20) DEFAULT 'EXITOSO',
    ADD COLUMN IF NOT EXISTS metodo_http    VARCHAR(10),
    ADD COLUMN IF NOT EXISTS url_solicitada VARCHAR(500),
    ADD COLUMN IF NOT EXISTS duracion_ms    INTEGER,
    ADD COLUMN IF NOT EXISTS mensaje_error  TEXT;

-- ----------------------------------------
-- 2. Comentarios descriptivos
-- ----------------------------------------
COMMENT ON COLUMN auditoria_acciones.user_agent     IS 'Cabecera User-Agent del cliente HTTP (navegador o bot)';
COMMENT ON COLUMN auditoria_acciones.session_id     IS 'Identificador de sesión HTTP para correlacionar acciones';
COMMENT ON COLUMN auditoria_acciones.resultado      IS 'EXITOSO: operación correcta, FALLIDO: error, DENEGADO: sin permiso';
COMMENT ON COLUMN auditoria_acciones.metodo_http    IS 'Verbo HTTP de la petición: GET, POST, PUT, DELETE, etc.';
COMMENT ON COLUMN auditoria_acciones.url_solicitada IS 'URI de la petición que originó la acción auditada';
COMMENT ON COLUMN auditoria_acciones.duracion_ms    IS 'Duración de la operación en milisegundos';
COMMENT ON COLUMN auditoria_acciones.mensaje_error  IS 'Mensaje de error o stack trace resumido en caso de FALLIDO';

-- ----------------------------------------
-- 3. Índices para análisis de seguridad
-- ----------------------------------------

-- Análisis de IPs (ya existe ip_origen, añadimos índice si no existe)
CREATE INDEX IF NOT EXISTS idx_auditoria_ip_origen
    ON auditoria_acciones(ip_origen);

-- Consultas de intentos fallidos/denegados ordenados por fecha
CREATE INDEX IF NOT EXISTS idx_auditoria_resultado_fecha
    ON auditoria_acciones(resultado, fecha_hora DESC);

-- Actividad de un usuario en el tiempo
CREATE INDEX IF NOT EXISTS idx_auditoria_usuario_fecha
    ON auditoria_acciones(usuario, fecha_hora DESC);

-- ----------------------------------------
-- 4. Actualizar filas existentes con valor por defecto
-- ----------------------------------------
UPDATE auditoria_acciones
SET resultado = 'EXITOSO'
WHERE resultado IS NULL;

-- ----------------------------------------
-- 5. Verificación
-- ----------------------------------------
DO $$
DECLARE
    col_count INTEGER;
BEGIN
    SELECT COUNT(*)
    INTO col_count
    FROM information_schema.columns
    WHERE table_name = 'auditoria_acciones'
      AND column_name IN ('user_agent','session_id','resultado',
                          'metodo_http','url_solicitada','duracion_ms','mensaje_error');

    IF col_count = 7 THEN
        RAISE NOTICE '✅ Las 7 columnas nuevas existen en auditoria_acciones';
    ELSE
        RAISE EXCEPTION '❌ Solo se encontraron % columnas nuevas (se esperaban 7)', col_count;
    END IF;

    RAISE NOTICE '✅ Migración 006 completada exitosamente';
END $$;
