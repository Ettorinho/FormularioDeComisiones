-- ========================================
-- V22: Estado de asistencia detallado (ASISTIO / EXCUSA / NO_ASISTIO)
-- ========================================
-- Motivo:
-- La tabla asistencias_actas solo distinguía asistencia mediante un
-- booleano `asistio` (true/false). Con la nueva opción de UI "Excusa
-- asistencia" (justificación obligatoria y confirmada explícitamente por
-- el usuario, distinta de "No asistió" sin justificar), es necesario
-- persistir un tercer estado que hasta ahora se perdía: tanto "Excusa
-- asistencia" como "No asistió" se guardaban igual (asistio = false).
--
-- Se añade la columna `estado_asistencia` con los 3 valores posibles y
-- se mantiene la columna `asistio` (backfill + sincronizada desde la
-- aplicación) por compatibilidad con el código y consultas existentes
-- que ya dependen de ella (asistio = true únicamente cuando
-- estado_asistencia = 'ASISTIO').
-- ========================================

-- 1. Añadir la nueva columna con valor por defecto seguro
ALTER TABLE asistencias_actas
ADD COLUMN IF NOT EXISTS estado_asistencia VARCHAR(20) NOT NULL DEFAULT 'NO_ASISTIO';

-- 2. Backfill de filas existentes a partir del booleano `asistio` y de si
--    tienen o no justificación registrada
UPDATE asistencias_actas
SET estado_asistencia = CASE
    WHEN asistio = true THEN 'ASISTIO'
    WHEN asistio = false AND justificacion IS NOT NULL AND trim(justificacion) <> '' THEN 'EXCUSA'
    ELSE 'NO_ASISTIO'
END;

-- 3. Restringir los valores admitidos
ALTER TABLE asistencias_actas DROP CONSTRAINT IF EXISTS check_estado_asistencia;
ALTER TABLE asistencias_actas
ADD CONSTRAINT check_estado_asistencia
CHECK (estado_asistencia IN ('ASISTIO', 'EXCUSA', 'NO_ASISTIO'));

-- 4. Índice para consultas/estadísticas por estado
CREATE INDEX IF NOT EXISTS idx_asistencias_estado ON asistencias_actas (estado_asistencia);

COMMENT ON COLUMN asistencias_actas.estado_asistencia IS
    'Estado detallado de la asistencia del miembro: ASISTIO, EXCUSA (no asistió pero justificó su ausencia, confirmada explícitamente en el formulario) o NO_ASISTIO (no asistió sin justificación).';

-- ========================================
-- VERIFICACIÓN POST-MIGRACIÓN
-- ========================================
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'asistencias_actas' AND column_name = 'estado_asistencia'
    ) THEN
        RAISE NOTICE '✓ Columna estado_asistencia añadida correctamente en asistencias_actas';
    ELSE
        RAISE EXCEPTION '✗ ERROR: Columna estado_asistencia no encontrada en asistencias_actas';
    END IF;
END $$;
