-- ========================================
-- SCRIPT DE MIGRACIÓN: EXTENSIÓN DE AUDITORÍA
-- Versión: 006
-- Fecha: 2026-05-11
-- Descripción: Amplía auditoria_acciones para monitoreo de seguridad y rendimiento
-- ========================================

ALTER TABLE auditoria_acciones
    ADD COLUMN IF NOT EXISTS user_agent VARCHAR(500),
    ADD COLUMN IF NOT EXISTS resultado VARCHAR(20) NOT NULL DEFAULT 'EXITOSO',
    ADD COLUMN IF NOT EXISTS duracion_ms INTEGER,
    ADD COLUMN IF NOT EXISTS mensaje_error TEXT,
    ADD COLUMN IF NOT EXISTS sesion_id VARCHAR(100);

CREATE INDEX IF NOT EXISTS idx_auditoria_resultado ON auditoria_acciones(resultado);
CREATE INDEX IF NOT EXISTS idx_auditoria_ip ON auditoria_acciones(ip_origen);
CREATE INDEX IF NOT EXISTS idx_auditoria_fecha_desc ON auditoria_acciones(fecha_hora DESC);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE table_name = 'auditoria_acciones'
          AND constraint_name = 'chk_auditoria_resultado'
    ) THEN
        ALTER TABLE auditoria_acciones
            ADD CONSTRAINT chk_auditoria_resultado
            CHECK (resultado IN ('EXITOSO', 'FALLIDO', 'DENEGADO'));
    END IF;
END $$;

CREATE OR REPLACE VIEW vw_auditoria_fuerza_bruta AS
SELECT usuario,
       ip_origen,
       COUNT(*) AS intentos_fallidos,
       MIN(fecha_hora) AS primer_intento,
       MAX(fecha_hora) AS ultimo_intento
FROM auditoria_acciones
WHERE accion = 'LOGIN' AND resultado = 'FALLIDO'
  AND fecha_hora >= NOW() - INTERVAL '10 minutes'
GROUP BY usuario, ip_origen
HAVING COUNT(*) >= 5;

CREATE OR REPLACE VIEW vw_auditoria_actividad_sospechosa AS
SELECT usuario,
       ip_origen,
       COUNT(*) AS total_eventos,
       COUNT(*) FILTER (WHERE resultado = 'DENEGADO') AS accesos_denegados,
       COUNT(DISTINCT sesion_id) AS sesiones_distintas,
       MAX(fecha_hora) AS ultimo_evento
FROM auditoria_acciones
WHERE fecha_hora >= NOW() - INTERVAL '1 hour'
GROUP BY usuario, ip_origen
HAVING COUNT(*) FILTER (WHERE resultado IN ('FALLIDO', 'DENEGADO')) >= 3
    OR COUNT(DISTINCT sesion_id) >= 5;

CREATE OR REPLACE VIEW vw_auditoria_operaciones_lentas AS
SELECT id,
       fecha_hora,
       usuario,
       accion,
       entidad,
       entidad_id,
       duracion_ms,
       ip_origen
FROM auditoria_acciones
WHERE duracion_ms IS NOT NULL
  AND duracion_ms > 1000
ORDER BY duracion_ms DESC;
