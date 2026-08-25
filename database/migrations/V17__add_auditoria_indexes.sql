-- ========================================
-- SCRIPT DE MIGRACIÓN: ÍNDICES ADICIONALES EN AUDITORÍA
-- Versión: 007
-- Fecha: 2026-08-25
-- Descripción: Añade índice compuesto (usuario, fecha_hora) para optimizar
--              las consultas filtradas por usuario con rango de fechas,
--              como la detección de intentos fallidos en los últimos N días.
-- ========================================

CREATE INDEX IF NOT EXISTS idx_auditoria_usuario_fecha
    ON auditoria_acciones(usuario, fecha_hora DESC);

-- Verificación
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM pg_indexes
        WHERE tablename = 'auditoria_acciones'
          AND indexname  = 'idx_auditoria_usuario_fecha'
    ) THEN
        RAISE NOTICE '✅ Índice idx_auditoria_usuario_fecha creado correctamente';
    ELSE
        RAISE EXCEPTION '❌ Error: el índice idx_auditoria_usuario_fecha no se creó';
    END IF;
END $$;
