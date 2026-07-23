-- ========================================
-- MIGRACIÓN V16: Corregir función registrar_cambio_cargo()
-- Fecha: 2026-07-23
-- Descripción: La migración V12 convirtió las columnas cargo_anterior y
--              cargo_nuevo de comision_miembro_historial_cargos de VARCHAR
--              a cargo_type ENUM, pero la función registrar_cambio_cargo()
--              (creada en V4) seguía haciendo cast explícito a VARCHAR al
--              insertar (OLD.cargo::VARCHAR / NEW.cargo::VARCHAR), lo que
--              provoca el error:
--                "la columna «cargo_anterior» es de tipo cargo_type pero
--                 la expresión es de tipo text"
--              Esta migración recrea la función insertando directamente
--              los valores ENUM sin cast a VARCHAR.
-- ========================================

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
            OLD.cargo,   -- ya es cargo_type, sin cast a VARCHAR
            NEW.cargo,   -- ya es cargo_type, sin cast a VARCHAR
            CURRENT_TIMESTAMP,
            COALESCE(current_setting('app.usuario_modificacion', true), current_user, 'SYSTEM')
        );
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION registrar_cambio_cargo() IS 'Trigger function que registra en el historial los cambios de cargo, insertando directamente los valores ENUM cargo_type (sin cast a VARCHAR, corregido en V16)';

-- Verificación
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM pg_proc WHERE proname = 'registrar_cambio_cargo'
    ) THEN
        RAISE NOTICE '✅ Función registrar_cambio_cargo() recreada correctamente sin cast a VARCHAR';
    ELSE
        RAISE EXCEPTION '❌ Error: La función registrar_cambio_cargo() no existe';
    END IF;
END $$;
