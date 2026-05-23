CREATE TABLE os_history
(
	id               UUID PRIMARY KEY,
	ordem_servico_id UUID        NOT NULL REFERENCES ordens_servico (id) ON DELETE CASCADE,
	status           VARCHAR(50) NOT NULL,
	started_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
	ended_at         TIMESTAMPTZ,
	CONSTRAINT fk_history_os FOREIGN KEY (ordem_servico_id) REFERENCES ordens_servico (id)
);

CREATE INDEX idx_os_status_history_os_id ON os_history (ordem_servico_id);
CREATE INDEX idx_os_status_history_status ON os_history (status);
