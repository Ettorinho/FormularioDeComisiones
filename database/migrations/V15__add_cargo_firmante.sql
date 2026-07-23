-- ========================================
-- MIGRACIÓN V15: Añadir cargo FIRMANTE
-- Fecha: 2026-07-23
-- Descripción: Añade el valor 'FIRMANTE' al ENUM cargo_type y actualiza
--              los CHECK constraints del historial de cargos para
--              permitir este nuevo valor.
-- ========================================

-- Paso 1: Añadir el nuevo valor al ENUM cargo_type (idempotente)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_enum e
        JOIN pg_type t ON e.enumtypid = t.oid
        WHERE t.typname = 'cargo_type' AND e.enumlabel = 'FIRMANTE'
    ) THEN
        ALTER TYPE cargo_type ADD VALUE 'FIRMANTE';
    END IF;
END
$$;

COMMIT;

-- Paso 2: Actualizar CHECK constraints en comision_miembro_historial_cargos
-- (deben ejecutarse en una transacción separada porque ALTER TYPE ... ADD VALUE
-- no puede usarse en la misma transacción que lo consuma)
ALTER TABLE comision_miembro_historial_cargos
    DROP CONSTRAINT IF EXISTS check_cargo_nuevo;

ALTER TABLE comision_miembro_historial_cargos
    ADD CONSTRAINT check_cargo_nuevo
    CHECK (cargo_nuevo IN (
        'REFERENTE', 'RESPONSABLE', 'PRESIDENTE', 'PARTICIPANTE',
        'SECRETARIO', 'INVESTIGADOR_PRINCIPAL', 'INVESTIGADOR_COLABORADOR', 'FIRMANTE'
    ));

ALTER TABLE comision_miembro_historial_cargos
    DROP CONSTRAINT IF EXISTS check_cargo_anterior;

ALTER TABLE comision_miembro_historial_cargos
    ADD CONSTRAINT check_cargo_anterior
    CHECK (cargo_anterior IS NULL OR cargo_anterior IN (
        'REFERENTE', 'RESPONSABLE', 'PRESIDENTE', 'PARTICIPANTE',
        'SECRETARIO', 'INVESTIGADOR_PRINCIPAL', 'INVESTIGADOR_COLABORADOR', 'FIRMANTE'
    ));

-- Actualizar comentario del tipo ENUM
COMMENT ON TYPE cargo_type IS 'Tipos de cargo en comisión: REFERENTE, RESPONSABLE, PRESIDENTE, PARTICIPANTE, SECRETARIO, INVESTIGADOR_PRINCIPAL, INVESTIGADOR_COLABORADOR, FIRMANTE';

-- Verificación final
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM pg_enum e
        JOIN pg_type t ON e.enumtypid = t.oid
        WHERE t.typname = 'cargo_type' AND e.enumlabel = 'FIRMANTE'
    ) THEN
        RAISE NOTICE '✅ Valor FIRMANTE añadido correctamente al ENUM cargo_type';
    ELSE
        RAISE EXCEPTION '❌ Error: El valor FIRMANTE NO se añadió al ENUM cargo_type';
    END IF;
END $$;
