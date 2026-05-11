-- Rename existing table to avoid conflict and allow data migration
ALTER TABLE IF EXISTS servicos RENAME TO servicos_legacy;

-- 1. Base Table: Itens Comerciais
CREATE TABLE itens_comerciais (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tipo_item VARCHAR(31) NOT NULL, -- Discriminator column
    nome VARCHAR(255) NOT NULL,
    descricao TEXT,
    preco_base NUMERIC(19, 2) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_itens_comerciais_tipo ON itens_comerciais(tipo_item);
CREATE INDEX idx_itens_comerciais_nome ON itens_comerciais(nome);

-- 2. Child Table: Serviços
CREATE TABLE servicos (
    id UUID PRIMARY KEY REFERENCES itens_comerciais(id) ON DELETE CASCADE,
    tempo_estimado_minutos BIGINT NOT NULL,
    categoria VARCHAR(50) NOT NULL
);

-- 3. Child Table: Peças
CREATE TABLE pecas (
    id UUID PRIMARY KEY REFERENCES itens_comerciais(id) ON DELETE CASCADE,
    fabricante VARCHAR(255),
    codigo_fabricante VARCHAR(255),
    modelo VARCHAR(255)
);

CREATE INDEX idx_pecas_codigo_fabricante ON pecas(codigo_fabricante);

-- 4. Child Table: Insumos
CREATE TABLE insumos (
    id UUID PRIMARY KEY REFERENCES itens_comerciais(id) ON DELETE CASCADE,
    unidade_medida VARCHAR(50)
);

-- 5. Data Migration from servicos_legacy
DO $$
DECLARE
    r RECORD;
BEGIN
    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'servicos_legacy') THEN
        FOR r IN SELECT * FROM servicos_legacy LOOP
            -- Insert into base table (mapping valor_base to preco_base)
            INSERT INTO itens_comerciais (id, tipo_item, nome, descricao, preco_base, ativo, created_at, updated_at)
            VALUES (r.id, 'SERVICO', r.nome, r.descricao, r.valor_base, r.ativo, NOW(), NOW());
            
            -- Insert into child table
            INSERT INTO servicos (id, tempo_estimado_minutos, categoria)
            VALUES (r.id, r.tempo_estimado_minutos, r.categoria);
        END LOOP;
    END IF;
END $$;

-- 6. Cleanup
DROP TABLE IF EXISTS servicos_legacy;
