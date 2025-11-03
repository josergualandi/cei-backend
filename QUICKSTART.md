# Quickstart

Abra cada projeto em sua própria janela do VS Code ou use os atalhos abaixo.

## Workspaces (duplo clique)
- `backend.code-workspace` → abre o projeto Java (Spring Boot)
- `frontend.code-workspace` → abre o projeto Angular

## Atalhos para rodar (Windows)
- `run-backend.cmd` → sobe o backend (porta 8081 por padrão)
- `run-frontend.cmd` → sobe o frontend (porta 4200)

> Dica: o diretório raiz não contém um `pom.xml`. Evite rodar `mvnw` a partir da raiz;
> use os scripts acima ou abra cada projeto no seu workspace.

## VS Code – Tarefas e Debug
### Backend
- Tarefas: Ctrl+Shift+P → "Run Task" →
  - `backend: run (Spring Boot)`
  - `backend: run (debug 5005)` e depois Debug → "Attach to Spring Boot (JDWP 5005)"
  - `backend: build` / `backend: test`
- Debug: F5 → "Debug Spring Boot"

### Frontend
- Tarefas: Ctrl+Shift+P → "Run Task" →
  - `frontend: install`
  - `frontend: start`
  - `frontend: build`
- Debug: F5 → "Debug Angular (Chrome)" (abre http://localhost:4200)

## URLs
- API: http://localhost:8081
- App: http://localhost:4200

## Variáveis de ambiente (opcional)
Você pode definir variáveis no arquivo `.env` na raiz para customizar a execução do backend:

- `SERVER_PORT` — porta do Spring Boot (default 8081)
- `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
- `JWT_SECRET`, `JWT_EXPIRATION`
- `APP_ADMIN_EMAIL`, `APP_ADMIN_PASSWORD`

Os scripts de execução irão carregar automaticamente essas variáveis se estiverem presentes.
