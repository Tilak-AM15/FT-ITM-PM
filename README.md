# PMTrack — Integrated Project Management & Timesheet Management Platform

PMTrack connects:

**Project → Task → Assignment → Execution → Timesheet → Approval → Reporting → Audit**

The implementation is based on the supplied Concept Note and Developer Task.

## Included modules

- Project Management
- Task / Sub-task Management
- Project team/resource assignment
- Employee workspace
- Daily and weekly timesheets
- PM approval/rejection/correction workflow
- Management and PM dashboards
- Project/resource/timesheet reports
- CSV export and browser Print/PDF
- Role-based access control
- In-app notifications and scheduled reminders
- Audit trail
- Swagger/OpenAPI
- AI Copilot foundation
- PostgreSQL + H2 support
- Docker deployment
- Render/Supabase deployment configuration
- Health endpoint

## Roles

- `SUPER_ADMIN`
- `ADMIN`
- `PROJECT_MANAGER`
- `TEAM_LEAD`
- `EMPLOYEE`
- `MANAGEMENT`
- `FINANCE_HR`

## Project structure

```text
pmtrack-platform/
├── backend/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
├── frontend/
│   ├── Dockerfile
│   ├── package.json
│   └── src/
├── Dockerfile.backend
├── Dockerfile.frontend
├── docker-compose.yml
├── .env.example
└── DEPLOYMENT.md
```

## Local development

Backend:

```bash
cd backend
mvn spring-boot:run
```

Frontend:

```bash
cd frontend
npm ci
npm run dev
```

## Docker

From the repository root:

```bash
docker compose up --build -d
```

Open:

```text
http://localhost
```

## API

Swagger:

```text
http://localhost:8080/swagger-ui.html
```

Health:

```text
http://localhost:8080/actuator/health
```

## Production

See `DEPLOYMENT.md` for the recommended Render + Supabase setup.

Production should use:

```env
APP_SEED_DEMO_DATA=false
VITE_DEMO_MODE=false
```

and provide a strong `JWT_SECRET` and bootstrap administrator credentials.

## Testing

Backend:

```bash
cd backend
mvn test
```

Frontend:

```bash
cd frontend
npm run build
```

The supplied automated backend tests cover application startup, authentication, and the Project → Task → Timesheet → Approval → Audit workflow.
"# FT-ITM-PM" 


## Vercel + Render deployment

Deploy `backend/` as a Render Docker Web Service and `frontend/` as a Vercel Vite project.

### Vercel environment variables

```env
VITE_API_BASE_URL=https://<your-backend>.onrender.com/api
VITE_DEMO_MODE=false
```

### Render backend environment variables

```env
SPRING_PROFILES_ACTIVE=postgres
DB_URL=jdbc:postgresql://<supabase-host>:5432/postgres
DB_USERNAME=<supabase-username>
DB_PASSWORD=<supabase-password>
JWT_SECRET=<long-random-secret>
JWT_EXPIRATION_MS=86400000
CORS_ALLOWED_ORIGINS=https://<your-vercel-domain>
APP_SEED_DEMO_DATA=false
APP_BOOTSTRAP_ADMIN_USERNAME=admin
APP_BOOTSTRAP_ADMIN_EMAIL=<admin-email>
APP_BOOTSTRAP_ADMIN_PASSWORD=<strong-password>
APP_BOOTSTRAP_ADMIN_FULL_NAME=System Administrator
JPA_DDL_AUTO=update
APP_SCHEDULER_ZONE=Asia/Kolkata
```

`VITE_*` values are browser-visible by design. Never store database passwords, JWT secrets, or private API keys in Vite environment variables.
