-- ========================================
-- MIGRACIÓN V19: Índice en comision_miembros(miembro_id)
-- Fecha: 2026-08-25
-- Descripción: Añade un índice parcial sobre miembro_id en la tabla comision_miembros
--              para acelerar las consultas frecuentes de ComisionMiembroDAO que filtran
--              por miembro_id. El índice parcial excluye miembros dados de baja, ya que
--              la mayoría de consultas operativas solo necesitan miembros activos.
-- ========================================

CREATE INDEX IF NOT EXISTS idx_comision_miembros_miembro_id_activo
    ON comision_miembros (miembro_id)
    WHERE fecha_baja IS NULL;
