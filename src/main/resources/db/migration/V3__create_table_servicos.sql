CREATE TABLE servicos (
    id UUID PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    descricao TEXT NOT NULL,
    valor_base NUMERIC(19, 2) NOT NULL,
    tempo_estimado_minutos BIGINT NOT NULL,
    categoria VARCHAR(50) NOT NULL, -- Added in V4, consolidated here
    ativo BOOLEAN NOT NULL
);

CREATE INDEX idx_servico_nome ON servicos(nome);
