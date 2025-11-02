# Backend — CEI Digital API (Spring Boot)

API REST em Spring Boot (Java 21) com autenticação JWT, PostgreSQL (via Docker) e camadas de serviço/DTO.

## Requisitos
- Java 21 (JDK)
- Maven Wrapper (já incluso: `mvnw`/`mvnw.cmd`)
- PostgreSQL em execução (via `docker-compose` na raiz do repositório)

## Executar em desenvolvimento
```powershell
# No Windows PowerShell
cd "C:\Users\José Gualandi\cei-digital\backend"
./mvnw.cmd spring-boot:run
```

A API ficará disponível em: http://localhost:8081/

- Login (JWT): `POST /auth/login` com JSON `{ "email": "admin@contoso.dev", "senha": "Admin!234" }`
- Empresas: `GET /api/empresas` (requer Bearer Token)

## Empacotar e executar JAR
```powershell
cd "C:\Users\José Gualandi\cei-digital\backend"
./mvnw.cmd -DskipTests clean package
java -jar target\cei-digital-0.0.1-SNAPSHOT.jar
```

## Configurações
Arquivo: `src/main/resources/application.yaml`

Principais propriedades:
- `server.port: 8081`
- `spring.datasource.*` (Postgres local)
- `security.jwt.secret`, `security.jwt.expiration`

Você pode sobrescrever por variáveis de ambiente padrão do Spring, por exemplo:
- `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`

## Dicas
- Suba banco/pgAdmin pela raiz do repo:
  ```powershell
  cd "C:\Users\José Gualandi\cei-digital"
  docker compose up -d db pgadmin
  ```
- Use a coleção `insomnia-empresas.json` na raiz para testar login e endpoints protegidos.
