CREATE TABLE itens_ordem_servico (
    id UUID PRIMARY KEY,
    ordem_servico_id UUID NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    descricao VARCHAR(255) NOT NULL,
    valor_unitario DECIMAL(19, 2) NOT NULL,
    quantidade INTEGER NOT NULL,
    referencia_id UUID NOT NULL,
    CONSTRAINT fk_itens_os_ordem_servico FOREIGN KEY (ordem_servico_id) REFERENCES ordens_servico(id)
);

CREATE INDEX idx_itens_os_ordem_servico_id ON itens_ordem_servico(ordem_servico_id);
