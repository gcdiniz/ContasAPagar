CREATE TABLE fornecedor (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL
);

CREATE TABLE conta (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    data_vencimento DATE NOT NULL,
    data_pagamento DATE,
    valor NUMERIC(15, 2) NOT NULL CHECK (valor > 0),
    descricao VARCHAR(500) NOT NULL,
    situacao INTEGER NOT NULL DEFAULT 1,
    fornecedor_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_conta_fornecedor FOREIGN KEY (fornecedor_id) REFERENCES fornecedor (id),
    CONSTRAINT chk_situacao CHECK (situacao IN (1, 2, 3))
);

CREATE INDEX idx_conta_data_vencimento ON conta (data_vencimento);
CREATE INDEX idx_conta_situacao ON conta (situacao);
CREATE INDEX idx_conta_fornecedor_id ON conta (fornecedor_id);
CREATE INDEX idx_conta_descricao ON conta USING gin (to_tsvector('portuguese', descricao));
