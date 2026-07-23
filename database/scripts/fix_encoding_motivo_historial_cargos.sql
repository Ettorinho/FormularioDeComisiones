-- ========================================
-- SCRIPT DE REPARACIÓN PUNTUAL (NO es una migración Flyway)
-- Fecha: 2026-07-23
-- Descripción: Corrige registros existentes en
--              comision_miembro_historial_cargos.motivo que quedaron
--              con "doble codificación" (mojibake) por el bug de
--              encoding JDBC corregido en DBUtil.java (commit 568a816).
--              Ejemplo del problema: "VotaciÃ³n" en vez de "Votación".
--
-- CÓMO FUNCIONA:
--   convert_from(convert_to(texto, 'LATIN1'), 'UTF8')
--   Interpreta los bytes actuales (que Postgres ve como UTF-8 válido,
--   p.ej. "Ã³" = 2 bytes) como si fueran Latin-1, y los reinterpreta
--   como UTF-8, revirtiendo la doble codificación.
--
-- ⚠️ IMPORTANTE:
--   1. Haz backup antes de ejecutar:
--      pg_dump -U postgres -d nombre_bd > backup_pre_fix_encoding.sql
--   2. Ejecuta primero el SELECT de verificación (paso 1) y revisa
--      manualmente que el resultado "motivo_corregido" es el esperado
--      ANTES de ejecutar el UPDATE (paso 2).
--   3. Este script solo toca la columna "motivo". Si detectas el mismo
--      problema en otras columnas de texto libre (p.ej. nombres,
--      observaciones), avisa para preparar un script equivalente.
-- ========================================

-- ----------------------------------------
-- PASO 1: Verificación previa (solo lectura)
-- Revisa el resultado antes de aplicar el UPDATE.
-- ----------------------------------------
SELECT
    id,
    motivo AS motivo_actual,
    convert_from(convert_to(motivo, 'LATIN1'), 'UTF8') AS motivo_corregido
FROM comision_miembro_historial_cargos
WHERE motivo ~ 'Ã|Â'  -- patrón típico de mojibake UTF-8/Latin-1
ORDER BY id;

-- ----------------------------------------
-- PASO 2: Corrección (ejecutar SOLO tras validar el PASO 1)
-- ----------------------------------------
-- UPDATE comision_miembro_historial_cargos
-- SET motivo = convert_from(convert_to(motivo, 'LATIN1'), 'UTF8')
-- WHERE motivo ~ 'Ã|Â';

-- ----------------------------------------
-- PASO 3: Verificación posterior
-- ----------------------------------------
-- SELECT id, motivo
-- FROM comision_miembro_historial_cargos
-- WHERE id IN (/* IDs corregidos en el paso 2 */)
-- ORDER BY id;
