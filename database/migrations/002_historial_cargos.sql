-- ========================================
-- SCRIPT DE MIGRACIÓN: HISTORIAL DE CAMBIOS DE CARGO
-- Versión: 002
-- Fecha: 2026-02-11
-- Descripción: Implementa sistema de trazabilidad de cambios de cargo
-- ========================================

-- IMPORTANTE: Hacer BACKUP de la base de datos antes de ejecutar
-- Comando sugerido: pg_dump -U postgres -d nombre_bd > backup_pre_migracion_002.sql

-- ========================================
-- TABLA: comision_miembro_historial_cargos
-- ========================================
-- Almacena todos los cambios de cargo realizados en comision_miembros
-- Usa la clave compuesta (comision_id, miembro_id) para referenciar

CREATE TABLE comision_miembro_historial_cargos (
    id BIGSERIAL PRIMARY KEY,
    comision_id BIGINT NOT NULL,
    miembro_id BIGINT NOT NULL,
    cargo_anterior VARCHAR(100),
    cargo_nuevo VARCHAR(100) NOT NULL,
    fecha_cambio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    motivo TEXT,
    usuario_modificacion VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) DEFAULT 'SYSTEM',
    
    -- Referencia a la tabla con clave compuesta
    FOREIGN KEY (comision_id, miembro_id) 
        REFERENCES comision_miembros(comision_id, miembro_id) 
        ON DELETE CASCADE,
    
    -- Validar que el cargo cambió realmente
    CONSTRAINT check_cargo_diferente 
        CHECK (cargo_anterior IS NULL OR cargo_anterior != cargo_nuevo)
);

-- ========================================
-- COMENTARIOS EN LA TABLA
-- ========================================

COMMENT ON TABLE comision_miembro_historial_cargos IS 'Historial completo de cambios de cargo de miembros en comisiones';
COMMENT ON COLUMN comision_miembro_historial_cargos.comision_id IS 'ID de la comisión (parte de clave compuesta)';
COMMENT ON COLUMN comision_miembro_historial_cargos.miembro_id IS 'ID del miembro (parte de clave compuesta)';
COMMENT ON COLUMN comision_miembro_historial_cargos.cargo_anterior IS 'Cargo antes del cambio (NULL en primer registro)';
COMMENT ON COLUMN comision_miembro_historial_cargos.cargo_nuevo IS 'Cargo después del cambio';
COMMENT ON COLUMN comision_miembro_historial_cargos.fecha_cambio IS 'Timestamp exacto del cambio';
COMMENT ON COLUMN comision_miembro_historial_cargos.motivo IS 'Motivo opcional del cambio de cargo';
COMMENT ON COLUMN comision_miembro_historial_cargos.usuario_modificacion IS 'Usuario que realizó el cambio';

-- ========================================
-- ÍNDICES PARA RENDIMIENTO
-- ========================================

-- Índice para búsquedas por miembro+comisión (consulta más común)
CREATE INDEX idx_historial_cargos_comision_miembro 
    ON comision_miembro_historial_cargos(comision_id, miembro_id);

-- Índice para búsquedas cronológicas
CREATE INDEX idx_historial_cargos_fecha 
    ON comision_miembro_historial_cargos(fecha_cambio DESC);

-- Índice para auditorías por usuario
CREATE INDEX idx_historial_cargos_usuario 
    ON comision_miembro_historial_cargos(usuario_modificacion);

-- ========================================
-- FUNCIÓN: registrar_cambio_cargo
-- ========================================
-- Se ejecuta automáticamente cuando cambia el cargo en comision_miembros

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
            COALESCE(current_user, 'SYSTEM')
        );
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION registrar_cambio_cargo() IS 'Registra automáticamente cambios de cargo en el historial';

-- ========================================
-- TRIGGER: trigger_cambio_cargo
-- ========================================
-- Se activa después de cada UPDATE del campo cargo en comision_miembros

CREATE TRIGGER trigger_cambio_cargo
    AFTER UPDATE OF cargo ON comision_miembros
    FOR EACH ROW
    WHEN (OLD.cargo IS DISTINCT FROM NEW.cargo)
    EXECUTE FUNCTION registrar_cambio_cargo();

COMMENT ON TRIGGER trigger_cambio_cargo ON comision_miembros IS 'Trigger que registra cambios de cargo automáticamente';

-- ========================================
-- VERIFICACIÓN
-- ========================================

-- Verificar que la tabla se creó correctamente
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'comision_miembro_historial_cargos') THEN
        RAISE NOTICE '✅ Tabla comision_miembro_historial_cargos creada correctamente';
    ELSE
        RAISE EXCEPTION '❌ Error: La tabla comision_miembro_historial_cargos no se creó';
    END IF;
    
    IF EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trigger_cambio_cargo') THEN
        RAISE NOTICE '✅ Trigger trigger_cambio_cargo creado correctamente';
    ELSE
        RAISE EXCEPTION '❌ Error: El trigger trigger_cambio_cargo no se creó';
    END IF;
    
    RAISE NOTICE '✅ Migración 002 completada exitosamente';
END $$;
