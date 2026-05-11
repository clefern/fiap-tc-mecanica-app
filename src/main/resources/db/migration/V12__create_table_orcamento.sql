CREATE TABLE orcamentos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo VARCHAR(20) NOT NULL UNIQUE,
    ordem_servico_id UUID NOT NULL,
    data_emissao TIMESTAMP NOT NULL,
    data_validade TIMESTAMP NOT NULL,
    valor_total_materiais DECIMAL(19, 2) NOT NULL,
    valor_total_mao_de_obra DECIMAL(19, 2) NOT NULL,
    valor_impostos DECIMAL(19, 2) NOT NULL,
    valor_total DECIMAL(19, 2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    url_pdf VARCHAR(255),
    
    CONSTRAINT fk_orcamento_os FOREIGN KEY (ordem_servico_id) REFERENCES ordens_servico(id)
);

CREATE INDEX idx_orcamentos_os ON orcamentos(ordem_servico_id);
CREATE INDEX idx_orcamentos_status ON orcamentos(status);
