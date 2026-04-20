CREATE SCHEMA IF NOT EXISTS financeiro;

CREATE TABLE IF NOT EXISTS financeiro.movimentacoes (
    id BIGSERIAL PRIMARY KEY,
    public_id UUID NOT NULL UNIQUE,
    escola_id VARCHAR(100) NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    descricao VARCHAR(255) NOT NULL,
    valor NUMERIC(19,2) NOT NULL,
    aluno_id BIGINT,
    data_movimentacao TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_movimentacoes_public_escola ON financeiro.movimentacoes (public_id, escola_id);
CREATE INDEX IF NOT EXISTS idx_movimentacoes_escola_aluno ON financeiro.movimentacoes (escola_id, aluno_id);

CREATE TABLE IF NOT EXISTS financeiro.debitos_aluno (
    id BIGSERIAL PRIMARY KEY,
    public_id UUID NOT NULL UNIQUE,
    escola_id VARCHAR(100) NOT NULL,
    aluno_id BIGINT NOT NULL,
    descricao VARCHAR(255) NOT NULL,
    valor NUMERIC(19,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    data_criacao TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_debitos_public_escola ON financeiro.debitos_aluno (public_id, escola_id);
CREATE INDEX IF NOT EXISTS idx_debitos_escola_aluno ON financeiro.debitos_aluno (escola_id, aluno_id);

CREATE TABLE IF NOT EXISTS financeiro.pagamentos (
    id BIGSERIAL PRIMARY KEY,
    public_id UUID NOT NULL UNIQUE,
    escola_id VARCHAR(100) NOT NULL,
    aluno_id BIGINT NOT NULL,
    valor NUMERIC(19,2) NOT NULL,
    metodo_pagamento VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL,
    provider VARCHAR(30) NOT NULL,
    transacao_externa VARCHAR(150),
    data_pagamento TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_pagamentos_public_escola ON financeiro.pagamentos (public_id, escola_id);
CREATE INDEX IF NOT EXISTS idx_pagamentos_escola_aluno ON financeiro.pagamentos (escola_id, aluno_id);
CREATE INDEX IF NOT EXISTS idx_pagamentos_transacao_provider_escola ON financeiro.pagamentos (transacao_externa, provider, escola_id);
