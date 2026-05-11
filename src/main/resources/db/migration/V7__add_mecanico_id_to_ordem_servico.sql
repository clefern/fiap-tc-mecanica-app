ALTER TABLE ordens_servico ADD COLUMN mecanico_id UUID;
ALTER TABLE ordens_servico ADD CONSTRAINT fk_os_mecanico FOREIGN KEY (mecanico_id) REFERENCES mecanicos(id);
CREATE INDEX idx_os_mecanico ON ordens_servico(mecanico_id);
