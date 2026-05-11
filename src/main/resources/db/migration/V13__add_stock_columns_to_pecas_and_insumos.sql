-- Add columns to pecas
ALTER TABLE pecas ADD quantidade_estoque INTEGER NOT NULL DEFAULT 0;
ALTER TABLE pecas ADD estoque_minimo INTEGER NOT NULL DEFAULT 0;
ALTER TABLE pecas ADD estoque_maximo INTEGER NOT NULL DEFAULT 0;

-- Add columns to insumos
ALTER TABLE insumos ADD quantidade_estoque INTEGER NOT NULL DEFAULT 0;
ALTER TABLE insumos ADD estoque_minimo INTEGER NOT NULL DEFAULT 0;
ALTER TABLE insumos ADD estoque_maximo INTEGER NOT NULL DEFAULT 0;
