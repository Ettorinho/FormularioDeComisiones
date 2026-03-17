-- ========================================
-- SCRIPT DE MIGRACIÓN: USUARIO AD EN HISTORIAL DE CARGOS
-- Versión: 003
-- Fecha: 2026-03-17
-- Descripción: Actualiza el trigger de historial de cargos para registrar
--              el usuario AD de la aplicación web en lugar del usuario de BD.
--              La app Java establece la variable de sesión 'app.usuario_modificacion'
--              antes de cada UPDATE de cargo, y el trigger la lee aquí.
-- ========================================

-- IMPORTANTE: Hacer BACKUP de la base de datos antes de ejecutar
-- Comando sugerido: pg_dump -U postgres -d nombre_bd > backup_pre_migracion_003.sql

-- ========================================
-- ACTUALIZAR FUNCIÓN: registrar_cambio_cargo
-- ========================================
-- Cambia COALESCE(current_user, 'SYSTEM') por la lectura de la variable
-- de sesión que establece la aplicación Java antes de cada cambio de cargo.

CREATE OR REPLACE FUNCTION registrar_cambio_cargo()
RETURNS TRIGGER AS $$
BEGIN
    -- Solo registrar si el cargo realmente cambió
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
            OLD.cargo,
            NEW.cargo,
            CURRENT_TIMESTAMP,
            -- Leer la variable de sesión establecida por la app Java.
            -- current_setting('app.usuario_modificacion', true) devuelve NULL
            -- si la variable no está definida (true = no lanzar excepción).
            -- NULLIF descarta el string vacío ''.
            -- Fallback a current_user si no viene de la app (ej: cambios manuales por SQL).
            COALESCE(
                NULLIF(current_setting('app.usuario_modificacion', true), ''),
                current_user,
                'SYSTEM'
            )
        );
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION registrar_cambio_cargo() IS 
    'Registra automáticamente cambios de cargo en el historial. '
    'Lee el usuario AD desde la variable de sesión app.usuario_modificacion '
    'establecida por la aplicación web antes de cada UPDATE.';

-- ========================================
-- VERIFICACIÓN
-- ========================================
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM pg_proc 
        WHERE proname = 'registrar_cambio_cargo'
    ) THEN
        RAISE NOTICE '✅ Función registrar_cambio_cargo actualizada correctamente';
    ELSE
        RAISE EXCEPTION '❌ Error: La función registrar_cambio_cargo no existe';
    END IF;
    
    RAISE NOTICE '✅ Migración 003 completada exitosamente';
    RAISE NOTICE 'ℹ️  Recuerda ejecutar este script en la base de datos de producción';
    RAISE NOTICE 'ℹ️  Los cambios de cargo anteriores NO se modifican (solo afecta a futuros cambios)';
END $$;
