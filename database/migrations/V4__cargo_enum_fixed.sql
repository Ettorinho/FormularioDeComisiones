
-- ========================================
-- MIGRACIÓN V4 (CORREGIDA): Convertir cargo a ENUM cargo_type
-- Solución al error: "no se puede alterar el tipo de una columna usada en trigger"
-- ========================================

-- Paso 1: Crear el tipo ENUM (solo si no existe)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'cargo_type') THEN
        CREATE TYPE cargo_type AS ENUM (
            'REFERENTE',
            'RESPONSABLE',
            'PRESIDENTE',
            'PARTICIPANTE',
            'SECRETARIO',
            'INVESTIGADOR_PRINCIPAL',
            'INVESTIGADOR_COLABORADOR'
        );
    END IF;
END
$$;

-- Paso 2: Normalizar datos legacy — 'MIEMBRO' → 'PARTICIPANTE'
UPDATE comision_miembros SET cargo = 'PARTICIPANTE' WHERE cargo = 'MIEMBRO';

-- Paso 3: Eliminar el CHECK constraint antiguo
ALTER TABLE comision_miembros DROP CONSTRAINT IF EXISTS check_cargo;

-- Paso 4: Eliminar el TRIGGER que bloquea el ALTER COLUMN
DROP TRIGGER IF EXISTS trigger_cambio_cargo ON comision_miembros;

-- Paso 5: Cambiar el tipo de la columna de VARCHAR a cargo_type ENUM
ALTER TABLE comision_miembros
    ALTER COLUMN cargo TYPE cargo_type USING cargo::cargo_type;

-- Paso 6: Volver a crear la FUNCIÓN (por si acaso, idempotente con OR REPLACE)
CREATE OR REPLACE FUNCTION registrar_cambio_cargo()
RETURNS TRIGGER AS $$
BEGIN
    IF OLD.cargo IS DISTINCT FROM NEW.cargo THEN
        INSERT INTO comision_miembro_historial_cargos (
            comision_id,
            miembro_id,
            cargo_anterior,
            cargo_nuevo,
            fecha_cambio,
            usuario_modificacion
        ) VALUES (
            NEW.comision_id,
            NEW.miembro_id,
            OLD.cargo::VARCHAR,   -- cast explícito de ENUM → VARCHAR para el historial
            NEW.cargo::VARCHAR,
            CURRENT_TIMESTAMP,
            COALESCE(current_user, 'SYSTEM')
        );
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Paso 7: Volver a crear el TRIGGER
CREATE TRIGGER trigger_cambio_cargo
    AFTER UPDATE OF cargo ON comision_miembros
    FOR EACH ROW
    WHEN (OLD.cargo IS DISTINCT FROM NEW.cargo)
    EXECUTE FUNCTION registrar_cambio_cargo();

-- Paso 8: Documentación
COMMENT ON COLUMN comision_miembros.cargo IS 'Cargo del miembro en la comisión (tipo ENUM cargo_type)';

-- Verificación final
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM pg_attribute a
        JOIN pg_class c ON a.attrelid = c.oid
        JOIN pg_type t ON a.atttypid = t.oid
        WHERE c.relname = 'comision_miembros'
          AND a.attname = 'cargo'
          AND t.typname = 'cargo_type'
    ) THEN
        RAISE NOTICE '✅ Columna cargo migrada correctamente a cargo_type ENUM';
    ELSE
        RAISE EXCEPTION '❌ Error: La columna cargo NO se migró al tipo cargo_type';
    END IF;

    IF EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trigger_cambio_cargo') THEN
        RAISE NOTICE '✅ Trigger trigger_cambio_cargo recreado correctamente';
    ELSE
        RAISE EXCEPTION '❌ Error: El trigger trigger_cambio_cargo no se recreó';
    END IF;
END $$;