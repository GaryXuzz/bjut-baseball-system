# Deploy Java Web Version to Render

This guide deploys the final Java + JDBC + MySQL version of the project.

Recommended setup:

- Java Web app: Render Web Service with Docker
- MySQL database: Aiven MySQL free tier

## 1. Create Cloud MySQL

Create a free MySQL service on Aiven.

After it is ready, note these values from the connection page:

- host
- port
- database name
- user
- password

Import the project schema and original data into the cloud MySQL database:

```bash
mysql -h YOUR_AIVEN_HOST -P YOUR_AIVEN_PORT -u YOUR_AIVEN_USER -p YOUR_AIVEN_DATABASE < ../sql/phase2_mysql_schema.sql
mysql -h YOUR_AIVEN_HOST -P YOUR_AIVEN_PORT -u YOUR_AIVEN_USER -p YOUR_AIVEN_DATABASE < ../sql/original_project_data.sql
```

If the MySQL provider requires SSL, add the provider's SSL option when importing or use its web SQL console.

## 2. Deploy Java Web on Render

Create a new Render Web Service:

- Build type: Docker
- Repository: this GitHub repository
- Root Directory: `java-web`
- Dockerfile Path: `Dockerfile`
- Instance type: Free is acceptable for course demonstration

## 3. Render Environment Variables

Set these variables in Render:

```text
PORT=8080
DB_URL=jdbc:mysql://YOUR_AIVEN_HOST:YOUR_AIVEN_PORT/YOUR_AIVEN_DATABASE?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&sslMode=REQUIRED
DB_USER=YOUR_AIVEN_USER
DB_PASSWORD=YOUR_AIVEN_PASSWORD
AUTH_DEMO_USERNAME=admin
AUTH_DEMO_PASSWORD=admin123
```

If the provider does not require SSL, remove `&sslMode=REQUIRED`.

The application also accepts `MYSQL_URL` or `DATABASE_URL` if the platform provides a MySQL URL in that form.

## 4. Verify

After deployment, open the Render URL and check:

- `/`
- `/players`
- `/stats`
- `/matchups`
- `/schema`
- `/export/players.csv`
- `/export/game-records.csv`

Administrator login:

```text
admin
admin123
```

## Notes

Render Free web services can sleep after a period of inactivity. Open the site once before the project demonstration so it has time to wake up.

Do not commit real cloud database passwords to GitHub. Use Render environment variables only.
