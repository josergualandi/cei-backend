Postgres local (desenvolvimento)

Este projeto pode ser executado com um banco PostgreSQL local usando Docker Compose.

1) Iniciar o banco com Docker Compose:

```powershell
cd C:\Users\José Gualandi\cei-digital
docker compose up -d
```

Isto cria um banco `cei_db` na porta `5432` com usuário `ceiuser` e senha `ceipass`.

2) Rodar a aplicação (usa valores padrão do `application.yaml`):

```powershell
# rodar via maven wrapper
.\mvnw.cmd spring-boot:run
```

3) Variáveis de ambiente

Se preferir, sobrescreva via variáveis de ambiente:

```powershell
$env:SPRING_DATASOURCE_URL = "jdbc:postgresql://localhost:5432/cei_db"
$env:SPRING_DATASOURCE_USERNAME = "ceiuser"
$env:SPRING_DATASOURCE_PASSWORD = "ceipass"
.\mvnw.cmd spring-boot:run
```

4) Parar

```powershell
docker compose down
```

Notas
- `spring.jpa.hibernate.ddl-auto=update` é conveniente para desenvolvimento; em produção use `validate` ou migrações (Flyway/Liquibase).
- Se não tiver Docker instalado, instale-o ou execute um PostgreSQL localmente e atualize `application.yaml`/variáveis de ambiente.
