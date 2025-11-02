-- =====================================================================================
-- CEI - Modelo de Dados (PostgreSQL)
-- =====================================================================================

-- 1) Função e trigger para atualizar automaticamente o campo atualizado_em
CREATE OR REPLACE FUNCTION set_current_timestamp()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
  NEW.atualizado_em = NOW();
  RETURN NEW;
END;
$$;

-- =====================================================================================
-- Tabela: perfil
-- =====================================================================================
CREATE TABLE IF NOT EXISTS perfil (
  id_perfil      BIGSERIAL PRIMARY KEY,
  nome           VARCHAR(50)  NOT NULL,
  descricao      VARCHAR(150)
);

-- Garantir que o nome do perfil seja único
CREATE UNIQUE INDEX IF NOT EXISTS ux_perfil_nome ON perfil (nome);

-- =====================================================================================
-- Tabela: permissao
-- =====================================================================================
CREATE TABLE IF NOT EXISTS permissao (
  id_permissao   BIGSERIAL PRIMARY KEY,
  nome           VARCHAR(100) NOT NULL,
  descricao      VARCHAR(200),
  rota           VARCHAR(100)
);

-- Garantir que o nome da permissão seja único
CREATE UNIQUE INDEX IF NOT EXISTS ux_permissao_nome ON permissao (nome);

-- =====================================================================================
-- Tabela: usuario
-- =====================================================================================
CREATE TABLE IF NOT EXISTS usuario (
  id_usuario     BIGSERIAL PRIMARY KEY,
  nome           VARCHAR(100) NOT NULL,
  email          VARCHAR(254) NOT NULL,
  senha          VARCHAR(255) NOT NULL, -- senha criptografada (BCrypt)
  ativo          BOOLEAN      NOT NULL DEFAULT TRUE,
  criado_em      TIMESTAMP    NOT NULL DEFAULT NOW(),
  atualizado_em  TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Email deve ser único para login
CREATE UNIQUE INDEX IF NOT EXISTS ux_usuario_email ON usuario (email);

-- Trigger de atualização do updated_at
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_trigger WHERE tgname = 'trg_usuario_set_current_timestamp'
  ) THEN
    CREATE TRIGGER trg_usuario_set_current_timestamp
    BEFORE UPDATE ON usuario
    FOR EACH ROW
    EXECUTE FUNCTION set_current_timestamp();
  END IF;
END$$;

-- =====================================================================================
-- Tabela: usuario_perfil (N:N entre usuario e perfil)
-- =====================================================================================
CREATE TABLE IF NOT EXISTS usuario_perfil (
  id_usuario_perfil BIGSERIAL PRIMARY KEY,
  id_usuario        BIGINT NOT NULL,
  id_perfil         BIGINT NOT NULL,
  CONSTRAINT fk_usuario_perfil_usuario
    FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_usuario_perfil_perfil
    FOREIGN KEY (id_perfil) REFERENCES perfil (id_perfil)
    ON DELETE CASCADE ON UPDATE CASCADE
);

-- Evita o mesmo perfil duplicado para o mesmo usuário
CREATE UNIQUE INDEX IF NOT EXISTS ux_usuario_perfil_pair
  ON usuario_perfil (id_usuario, id_perfil);

-- Índices auxiliares
CREATE INDEX IF NOT EXISTS ix_usuario_perfil_usuario ON usuario_perfil (id_usuario);
CREATE INDEX IF NOT EXISTS ix_usuario_perfil_perfil ON usuario_perfil (id_perfil);

-- =====================================================================================
-- Tabela: perfil_permissao (N:N entre perfil e permissao)
-- =====================================================================================
CREATE TABLE IF NOT EXISTS perfil_permissao (
  id_perfil_permissao BIGSERIAL PRIMARY KEY,
  id_perfil           BIGINT NOT NULL,
  id_permissao        BIGINT NOT NULL,
  CONSTRAINT fk_perfil_permissao_perfil
    FOREIGN KEY (id_perfil) REFERENCES perfil (id_perfil)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_perfil_permissao_permissao
    FOREIGN KEY (id_permissao) REFERENCES permissao (id_permissao)
    ON DELETE CASCADE ON UPDATE CASCADE
);

-- Evita a mesma permissão duplicada para o mesmo perfil
CREATE UNIQUE INDEX IF NOT EXISTS ux_perfil_permissao_pair
  ON perfil_permissao (id_perfil, id_permissao);

-- Índices auxiliares
CREATE INDEX IF NOT EXISTS ix_perfil_permissao_perfil ON perfil_permissao (id_perfil);
CREATE INDEX IF NOT EXISTS ix_perfil_permissao_permissao ON perfil_permissao (id_permissao);

-- =====================================================================================
-- Tabela: empresa
-- =====================================================================================
CREATE TABLE IF NOT EXISTS empresa (
  id_empresa          BIGSERIAL PRIMARY KEY,
  tipo_pessoa         VARCHAR(10)  NOT NULL,   -- 'CNPJ', 'CPF', 'MEI'
  numero_documento    VARCHAR(20)  NOT NULL,   -- CNPJ/CPF sem máscara (só dígitos)
  nome_razao_social   VARCHAR(150) NOT NULL,
  nome_fantasia       VARCHAR(150),
  tipo_atividade      VARCHAR(100),
  cnae                VARCHAR(20),
  data_abertura       DATE,
  situacao            VARCHAR(20),             -- 'Ativa', 'Inativa'
  endereco            VARCHAR(255),
  cidade              VARCHAR(100),
  estado              VARCHAR(2),
  telefone            VARCHAR(20),
  email               VARCHAR(100),
  criado_em           TIMESTAMP     NOT NULL DEFAULT NOW(),
  atualizado_em       TIMESTAMP     NOT NULL DEFAULT NOW(),
  CONSTRAINT ck_empresa_tipo_pessoa CHECK (tipo_pessoa IN ('CNPJ','CPF','MEI'))
);

-- Documento único por tipo
CREATE UNIQUE INDEX IF NOT EXISTS ux_empresa_tipo_doc
  ON empresa (tipo_pessoa, numero_documento);

-- Índices de busca
CREATE INDEX IF NOT EXISTS ix_empresa_nome_razao ON empresa (nome_razao_social);
CREATE INDEX IF NOT EXISTS ix_empresa_nome_fantasia ON empresa (nome_fantasia);
CREATE INDEX IF NOT EXISTS ix_empresa_cidade ON empresa (cidade);
CREATE INDEX IF NOT EXISTS ix_empresa_estado ON empresa (estado);

-- Trigger de atualização do updated_at em empresa
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_trigger WHERE tgname = 'trg_empresa_set_current_timestamp'
  ) THEN
    CREATE TRIGGER trg_empresa_set_current_timestamp
    BEFORE UPDATE ON empresa
    FOR EACH ROW
    EXECUTE FUNCTION set_current_timestamp();
  END IF;
END$$;

-- =====================================================================================
-- Inserts iniciais (opcional)
-- =====================================================================================

-- Perfis padrão
INSERT INTO perfil (nome, descricao)
VALUES ('ADMIN', 'Administrador do sistema'),
       ('COMUM', 'Usuário comum')
ON CONFLICT (nome) DO NOTHING;

-- Permissões exemplo
INSERT INTO permissao (nome, descricao, rota)
VALUES ('CADASTRAR_EMPRESA', 'Pode cadastrar empresa', '/empresas/novo'),
       ('EDITAR_EMPRESA', 'Pode editar empresa', '/empresas/editar'),
       ('EXCLUIR_EMPRESA', 'Pode excluir/inativar empresa', '/empresas'),
       ('CONSULTAR_EMPRESA', 'Pode consultar empresas', '/empresas'),
       ('GERENCIAR_USUARIOS', 'Pode gerenciar usuários', '/usuarios'),
       ('GERENCIAR_PERFIS', 'Pode gerenciar perfis e acessos', '/perfis')
ON CONFLICT (nome) DO NOTHING;

-- Concede todas as permissões ao perfil ADMIN
INSERT INTO perfil_permissao (id_perfil, id_permissao)
SELECT p.id_perfil, pm.id_permissao
FROM perfil p
CROSS JOIN permissao pm
WHERE p.nome = 'ADMIN'
  AND NOT EXISTS (
    SELECT 1 FROM perfil_permissao pp
    WHERE pp.id_perfil = p.id_perfil
      AND pp.id_permissao = pm.id_permissao
  );
