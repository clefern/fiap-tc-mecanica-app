-- V11__alter_table_os_add_prioridade_data_aprovacao.sql

-- Adicionar colunas
ALTER TABLE ordens_servico ADD COLUMN prioridade INTEGER DEFAULT 1 NOT NULL;
ALTER TABLE ordens_servico ADD COLUMN data_aprovacao TIMESTAMP;

-- Criar índices para otimização das filas (conforme ADR-012)
CREATE INDEX idx_os_fila_orcamento ON ordens_servico (status, prioridade DESC, created_at ASC) WHERE status = 'RECEBIDA';
CREATE INDEX idx_os_fila_execucao ON ordens_servico (status, prioridade DESC, data_aprovacao ASC) WHERE status = 'APROVADA';
