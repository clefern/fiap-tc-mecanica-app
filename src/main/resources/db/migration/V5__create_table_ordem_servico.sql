CREATE TABLE ordens_servico (
    id UUID PRIMARY KEY,
    cliente_id UUID NOT NULL,
    veiculo_id UUID NOT NULL,
    codigo VARCHAR(20) NOT NULL UNIQUE,
    status VARCHAR(50) NOT NULL,
    valor_total DECIMAL(19, 2) NOT NULL DEFAULT 0,
    data_entrada TIMESTAMP NOT NULL,
    data_previsao TIMESTAMP,
    data_fechamento TIMESTAMP,
    observacoes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    
    CONSTRAINT fk_os_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id),
    CONSTRAINT fk_os_veiculo FOREIGN KEY (veiculo_id) REFERENCES veiculos(id)
);

CREATE INDEX idx_os_cliente ON ordens_servico(cliente_id);
CREATE INDEX idx_os_veiculo ON ordens_servico(veiculo_id);
CREATE INDEX idx_os_status ON ordens_servico(status);
