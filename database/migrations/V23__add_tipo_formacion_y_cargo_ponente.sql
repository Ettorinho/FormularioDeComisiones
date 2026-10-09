-- ========================================
-- V23: Nuevo tipo de comisión "Formación/Taller/Sesión" y nuevo cargo "Ponente"
-- ========================================
-- Motivo:
-- Se necesita poder crear comisiones/grupos de tipo "Formación/Taller/Sesión"
-- (además de los ya existentes COMISION, GRUPO_TRABAJO, GRUPO_MEJORA), y
-- registrar en ellas miembros con el nuevo cargo "PONENTE" (además de los
-- cargos ya existentes: REFERENTE, RESPONSABLE, PRESIDENTE, PARTICIPANTE,
-- SECRETARIO, INVESTIGADOR_PRINCIPAL, INVESTIGADOR_COLABORADOR, FIRMANTE).
-- ========================================

-- 1. Añadir el nuevo valor al ENUM tipo_type (idempotente)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_enum e
        JOIN pg_type t ON e.enumtypid = t.oid
        WHERE t.typname = 'tipo_type' AND e.enumlabel = 'FORMACION_TALLER_SESION'
    ) THEN
        ALTER TYPE tipo_type ADD VALUE 'FORMACION_TALLER_SESION';
    END IF;
END
$$;

-- 2. Añadir el nuevo valor al ENUM cargo_type (idempotente)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_enum e
        JOIN pg_type t ON e.enumtypid = t.oid
        WHERE t.typname = 'cargo_type' AND e.enumlabel = 'PONENTE'
    ) THEN
        ALTER TYPE cargo_type ADD VALUE 'PONENTE';
    END IF;
END
$$;

-- 3. Actualizar CHECK constraints en comision_miembro_historial_cargos
-- Nota: ALTER TYPE ... ADD VALUE no puede usarse en la misma transacción que lo consuma,
-- pero Flyway gestiona las transacciones automáticamente; no se debe insertar COMMIT manual.
ALTER TABLE comision_miembro_historial_cargos
    DROP CONSTRAINT IF EXISTS check_cargo_nuevo;

ALTER TABLE comision_miembro_historial_cargos
    ADD CONSTRAINT check_cargo_nuevo
    CHECK (cargo_nuevo IN (
        'REFERENTE', 'RESPONSABLE', 'PRESIDENTE', 'PARTICIPANTE',
        'SECRETARIO', 'INVESTIGADOR_PRINCIPAL', 'INVESTIGADOR_COLABORADOR', 'FIRMANTE', 'PONENTE'
    ));

ALTER TABLE comision_miembro_historial_cargos
    DROP CONSTRAINT IF EXISTS check_cargo_anterior;

ALTER TABLE comision_miembro_historial_cargos
    ADD CONSTRAINT check_cargo_anterior
    CHECK (cargo_anterior IS NULL OR cargo_anterior IN (
        'REFERENTE', 'RESPONSABLE', 'PRESIDENTE', 'PARTICIPANTE',
        'SECRETARIO', 'INVESTIGADOR_PRINCIPAL', 'INVESTIGADOR_COLABORADOR', 'FIRMANTE', 'PONENTE'
    ));

-- Actualizar comentarios de los tipos ENUM
COMMENT ON TYPE tipo_type IS 'Tipos de comisión: COMISION, GRUPO_TRABAJO, GRUPO_MEJORA, FORMACION_TALLER_SESION';
COMMENT ON TYPE cargo_type IS 'Tipos de cargo en comisión: REFERENTE, RESPONSABLE, PRESIDENTE, PARTICIPANTE, SECRETARIO, INVESTIGADOR_PRINCIPAL, INVESTIGADOR_COLABORADOR, FIRMANTE, PONENTE';

-- Verificación final
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM pg_enum e
        JOIN pg_type t ON e.enumtypid = t.oid
        WHERE t.typname = 'tipo_type' AND e.enumlabel = 'FORMACION_TALLER_SESION'
    ) THEN
        RAISE NOTICE '✅ Valor FORMACION_TALLER_SESION añadido correctamente al ENUM tipo_type';
    ELSE
        RAISE EXCEPTION '❌ Error: El valor FORMACION_TALLER_SESION NO se añadió al ENUM tipo_type';
    END IF;

    IF EXISTS (
        SELECT 1 FROM pg_enum e
        JOIN pg_type t ON e.enumtypid = t.oid
        WHERE t.typname = 'cargo_type' AND e.enumlabel = 'PONENTE'
    ) THEN
        RAISE NOTICE '✅ Valor PONENTE añadido correctamente al ENUM cargo_type';
    ELSE
        RAISE EXCEPTION '❌ Error: El valor PONENTE NO se añadió al ENUM cargo_type';
    END IF;
END $$;
