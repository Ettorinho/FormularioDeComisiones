-- ========================================
-- MIGRACIÓN V20: Índice funcional en LOWER(nombre) para comisiones
-- Fecha: 2026-08-25
-- Descripción: Añade un índice funcional sobre LOWER(nombre) en la tabla comisiones
--              para acelerar las búsquedas case-insensitive realizadas por
--              ComisionDAO.findByNombreLike que usa LOWER(nombre) LIKE LOWER(?).
-- ========================================

CREATE INDEX IF NOT EXISTS idx_comisiones_lower_nombre
    ON comisiones (LOWER(nombre));
