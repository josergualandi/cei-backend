# Deploy barato (Brasil) – Backend no Fly.io (GRU) + Frontend no Cloudflare Pages (grátis)

Este guia coloca o backend (Spring Boot) em container no Fly.io (região São Paulo/GRU) e o frontend (Angular) no Cloudflare Pages (gratuito com CDN global e PoP no Brasil).

## Pré‑requisitos
- Conta no Fly.io e CLI instalada (Windows PowerShell): https://fly.io/docs/hands-on/install-flyctl/
- Conta no Cloudflare com Pages habilitado
- Repositório no GitHub (para usar os workflows manuais)
- Segredos: TWILIO_*, credenciais do Postgres, JWT secret etc. (não faça commit no repo)

## Backend (Fly.io – GRU)
1) No PowerShell, dentro da pasta `backend/`:
   - Login: `flyctl auth signup` (ou `flyctl auth login` se já tiver)
   - Crie o app (pode aceitar gerar um nome, região GRU):
     - `flyctl launch --no-deploy --copy-config --region gru`
       - Se perguntar Dockerfile: já existe
       - Se perguntar Postgres: pode responder “não” agora (ver seção banco)
       - Isso criará/ajustará `backend/fly.toml` (altere `app = "..."` se quiser nome fixo)

2) Configure segredos (exemplos):
   - `flyctl secrets set TWILIO_ACCOUNT_SID=... TWILIO_AUTH_TOKEN=... WHATSAPP_ENABLED=true`
   - Banco (escolha UMA estratégia abaixo e defina as variáveis do Spring Boot):
     - Gerenciado no Fly Postgres (Brasil):
       - `flyctl postgres create --region gru` (escolha tamanho mais barato)
       - `flyctl postgres attach <nome-do-cluster>` (vai injetar DATABASE_URL)
       - Opcional: setar manualmente `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
     - Neon/Supabase (free – fora do Brasil):
       - Pegar a conexão e setar: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`

3) Deploy do backend:
   - `flyctl deploy --remote-only --config fly.toml --strategy rolling`
   - Ver logs: `flyctl logs`
   - Abrir app: `flyctl open`

4) Health check: `GET /actuator/health` deve retornar UP (o Fly já faz isso via check).

## Frontend (Cloudflare Pages – grátis)
1) Pelo painel do Cloudflare Pages:
   - Create a project > Conecte seu repositório GitHub
   - Build settings:
     - Framework: Angular
     - Root directory: `frontend`
     - Build command: `npm run build`
     - Build output directory: `dist/frontend`

2) Variáveis de ambiente (se precisar): adicione no Pages (por exemplo URL da API).

3) Alternativa via GitHub Actions manual (já incluso neste repo):
   - Configurar Secrets no GitHub repo:
     - `CLOUDFLARE_API_TOKEN`
     - `CLOUDFLARE_ACCOUNT_ID`
   - Rodar workflow “Deploy Frontend to Cloudflare Pages” manualmente (Actions > Workflows > Run).

## Domain/DNS/SSL
- Cloudflare Pages oferece domínio `<project>.pages.dev` com HTTPS; você pode apontar seu domínio customizado no painel do Cloudflare.
- Backend no Fly.io ganha um `https://<app>.fly.dev`; você pode configurar um domínio próprio no Fly e apontar DNS.

## Custos
- Cloudflare Pages: gratuito (com limites generosos)
- Fly.io (backend): instância pequena em GRU costuma ficar a poucos dólares/mês em baixa carga
- Postgres:
  - Fly Postgres (Brasil): baixo custo, mas não gratuito
  - Neon/Supabase (free): fora do Brasil (latência maior)

## Dicas
- Mantenha logs em nível INFO/ERROR em produção para reduzir custo de armazenamento
- Sempre use segredos (Fly secrets / Cloudflare envs); nunca faça commit de tokens
- Se precisar de CI/CD automático, podemos ativar workflows para push na `main`
