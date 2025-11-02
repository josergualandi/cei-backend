# CEI Digital — Cadastro Empresarial Integrado (PT-BR)

Este repositório agora está organizado em dois projetos separados:

- `backend/` — API Spring Boot (Java 21) com PostgreSQL, autenticação JWT e DTOs
- `frontend/` — Aplicação Angular (SPA) que consome a API

Também incluímos uma interface web do Postgres via pgAdmin (Docker) e uma coleção do Insomnia para testes.

## Tecnologias
- Java 21, Spring Boot 3
- Spring Web, Spring Data JPA, Bean Validation (Jakarta)
- PostgreSQL 15 (Docker) + pgAdmin 4 (Docker)
- Maven Wrapper (mvnw)

## Requisitos
- Windows com WSL2 habilitado
- Docker Desktop instalado e em execução
- Java 21 instalado (ou use somente o Docker para banco)

## Configuração rápida
1) Copie o arquivo de exemplo de variáveis e ajuste se necessário:
```powershell
cd "C:\\Users\\José Gualandi\\cei-digital"
Copy-Item .env.example .env
# Edite .env e personalize e-mail/senha do pgAdmin (opcional)
```

2) Suba o Postgres e o pgAdmin (interface web do Postgres):
```powershell
docker compose up -d
# ou apenas os serviços de banco e interface
docker compose up -d db pgadmin
```

3) Acesse o pgAdmin no navegador:
- URL: http://localhost:5050
- E-mail: o que estiver no .env (padrão: admin@cei.local)
- Senha: o que estiver no .env (padrão: Adm1n!2025-CEI)

4) Registre o servidor do Postgres no pgAdmin:
- Add New Server → General → Name: Local DB
- Connection:
  - Host: db
  - Port: 5432
  - Maintenance database: cei_db
  - Username: ceiuser
  - Password: ceipass

Dica: como pgAdmin e Postgres estão no mesmo docker-compose, use o host `db` (nome do serviço). Para clientes externos (DBeaver, etc.), use `localhost`.

## Como rodar a aplicação

### Backend (Spring Boot)
- Via Maven (recomendado durante o desenvolvimento):
```powershell
cd "C:\\Users\\José Gualandi\\cei-digital\\backend"
.\mvnw.cmd spring-boot:run
```

- Ou compilando e executando o JAR:
```powershell
cd "C:\\Users\\José Gualandi\\cei-digital\\backend"
.\mvnw.cmd -DskipTests clean package
java -jar target\\cei-digital-0.0.1-SNAPSHOT.jar
```

O backend sobe em: http://localhost:8081/

### Frontend (Angular)
```powershell
cd "C:\\Users\\José Gualandi\\cei-digital\\frontend"
npm install
npm start
```

O frontend sobe em: http://localhost:4200/

## Testar a API
- Endpoints principais (JSON):
  - GET `/api/empresas` — lista empresas
  - GET `/api/empresas/{id}` — busca por id
  - GET `/api/empresas/search?cnpj=...` — busca por CNPJ (com ou sem máscara)
  - POST `/api/empresas` — cria empresa
    - Exemplo de corpo:
```json
{
  "nome": "Minha Empresa",
  "cnpj": "12.345.678/0001-99"
}
```

- Erros padronizados:
  - 400 quando há erro de validação (campos obrigatórios, CNPJ deve ter 14 dígitos)
  - 409 quando o CNPJ já existir (conflito de integridade)

- Insomnia: use o arquivo `insomnia-empresas.json` (coleção pronta; o login preenche o token automaticamente para as demais requisições).

## Configurações de ambiente
- `backend/src/main/resources/application.yaml` já vem configurado para usar Postgres local via Docker:
  - URL: `jdbc:postgresql://localhost:5432/cei_db`
  - Usuário: `ceiuser`
  - Senha: `ceipass`
  - JPA `ddl-auto: update` (para ambiente de desenvolvimento)

Você pode sobrescrever via variáveis de ambiente padrão do Spring:
- `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`

## Solução de problemas (FAQ)
- Docker não inicia / Engine 500:
  - Atualize o WSL2: `wsl --update`
  - Reinicie o Docker Desktop
  - Verifique: `docker info`
- Porta 5432 em uso:
  - Pare processos que usam Postgres local ou altere a porta no `docker-compose.yml`.
- Rebuild falha por arquivo JAR em uso:
  - Pare a aplicação que está rodando antes de empacotar: encerre o processo Java e rode o `mvnw` novamente.

## URLs úteis
- API (backend): http://localhost:8081/
- pgAdmin: http://localhost:5050
- JDBC (aplicação): `jdbc:postgresql://localhost:5432/cei_db`
- Conexão cliente: `postgresql://ceiuser:ceipass@localhost:5432/cei_db`

---
Qualquer dúvida, abra uma issue ou peça ajuda por aqui. 🙂
