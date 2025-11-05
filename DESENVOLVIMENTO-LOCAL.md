# Desenvolvimento local (Windows)

Guia rápido para subir a infraestrutura (Postgres, Mailpit, pgAdmin) e rodar backend (Spring Boot) e frontend (Angular) localmente.

## Pré‑requisitos
- Windows com Docker Desktop em execução
- Java 21 (para rodar o backend localmente)
- Node.js 18+ (para rodar o frontend)

## 1) Subir a infraestrutura (Docker)
No diretório raiz do repositório:

```powershell
cd "C:\\Users\\José Gualandi\\cei-digital"
docker compose up -d
```

Serviços e portas:
- Postgres: localhost:5432
- Mailpit (SMTP/UI de e‑mail de teste):
  - SMTP: localhost:1025
  - UI: http://localhost:8025
- pgAdmin: http://localhost:5050

Credenciais e banco (padrão — ver docker-compose.yml):
- Banco: cei_db
- Usuário: ceiuser
- Senha: ceipass

Strings de conexão úteis:
- JDBC (aplicação): `jdbc:postgresql://localhost:5432/cei_db`
- Cliente (DBeaver/psql): `postgresql://ceiuser:ceipass@localhost:5432/cei_db`

Sobre o pgAdmin:
- E-mail e senha vêm do arquivo `.env` (se existir). Caso não exista, os padrões do compose são usados: `admin@cei.local` / `admin`.
- Dica: como pgAdmin e Postgres estão no mesmo docker-compose, ao registrar um servidor no pgAdmin use Host `db` (nome do serviço). Em clientes externos use `localhost`.

Para verificar os contêineres:
```powershell
docker compose ps
```

Para encerrar:
```powershell
docker compose down
```

## 2) Rodar o backend (Spring Boot)
Use o atalho na raiz:
```powershell
.\run-backend.cmd
```
- Porta padrão: http://localhost:8081
- Configuração padrão de DB/SMTP já aponta para a infraestrutura acima.
- É possível sobrescrever variáveis via `.env` na raiz (veja `.env.example`).

## 3) Rodar o frontend (Angular)
Use o atalho na raiz:
```powershell
.\run-frontend.cmd
```
- Porta padrão: http://localhost:4200
- O frontend usa `http://localhost:8081` como API (veja `frontend/src/environments/environment.ts`).

## 4) Verificações rápidas
- API: abra http://localhost:8081 (deve responder)
- App: abra http://localhost:4200 (deve carregar a SPA)
- E‑mail de teste: abra http://localhost:8025 (UI do Mailpit)
- Postgres via pgAdmin: http://localhost:5050

## 5) Dicas e solução de problemas
- Porta 5432 já em uso: pare outro Postgres local ou altere a porta no `docker-compose.yml`.
- Docker com erro no WSL2: `wsl --update` e reinicie o Docker Desktop.
- Backend não conecta no DB: confirme que o contêiner `db` está “Up” e que as variáveis do Spring não foram sobrescritas incorretamente.
- Se precisar credenciais do banco para um cliente externo, use: usuário `ceiuser`, senha `ceipass`.

---
Qualquer dúvida, consulte também `README.md`, `README-POSTGRES.md` e `QUICKSTART.md`.
