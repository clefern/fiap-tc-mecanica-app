-- V16: Add indexes for common query patterns
-- Improves fila operacional query (status filter + ordering) and budget lookups

CREATE INDEX IF NOT EXISTS idx_ordens_servico_status
    ON ordens_servico (status);

CREATE INDEX IF NOT EXISTS idx_ordens_servico_status_data_entrada
    ON ordens_servico (status, data_entrada ASC);

CREATE INDEX IF NOT EXISTS idx_orcamentos_ordem_servico_status
    ON orcamentos (ordem_servico_id, status);
