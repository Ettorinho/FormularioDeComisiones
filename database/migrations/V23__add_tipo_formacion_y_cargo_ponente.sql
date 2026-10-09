-- ========================================
-- V23: Nuevo tipo de comisión "Formación/Taller/Sesión" y nuevo cargo "Ponente"
-- ========================================
-- Motivo:
-- Se necesita poder crear comisiones/grupos de tipo "Formación/Taller/Sesión"
-- (además de los ya existentes COMISION, GRUPO_TRABAJO, GRUPO_MEJORA), y
-- registrar en ellas miembros con el nuevo cargo "PONENTE" (además de los
-- cargos ya existentes: REFERENTE, RESPONSABLE, PRESIDENTE, PARTICIPANTE,
-- SECRETARIO, INVESTIGADOR_PRINCIPAL, INVESTIGADOR_COLABORADOR, FIRMANTE).
--
-- IMPORTANTE: PostgreSQL exige que un valor de ENUM añadido con
-- "ALTER TYPE ... ADD VALUE" esté COMMITEADO antes de poder usarse (p. ej.
-- en un CHECK constraint). Como Flyway ejecuta cada migración en una única
-- transacción, esta migración se limita EXCLUSIVAMENTE a añadir los nuevos
-- valores a los ENUMs. La actualización de los CHECK constraints que los
-- referencian se hace en la migración V24, en una transacción posterior.
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
