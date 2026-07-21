-- database/migrations/002_fix_duplicate_indexes.sql
-- Eliminar índices duplicados en tablas actas y miembros
-- Idempotente: usa IF EXISTS para que pueda ejecutarse varias veces sin error

-- ============================================================
-- Tabla: actas
-- ============================================================

-- comision_id: mantener idx_actas_comision, eliminar idx_acta_comision_id
DROP INDEX IF EXISTS idx_acta_comision_id;

-- fecha_reunion: mantener idx_actas_fecha (con DESC, más útil para ORDER BY reciente)
-- y eliminar idx_acta_fecha_reunion (sin dirección explícita)
DROP INDEX IF EXISTS idx_acta_fecha_reunion;

-- ============================================================
-- Tabla: miembros
-- ============================================================

-- idx_miembros_dni es redundante con el UNIQUE index miembros_dni_nif_key
DROP INDEX IF EXISTS idx_miembros_dni;
