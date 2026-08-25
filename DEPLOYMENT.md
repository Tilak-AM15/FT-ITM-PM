# PMTrack Deployment Guide

PMTrack is a React/Vite frontend + Spring Boot 3/Java 21 backend + PostgreSQL application.

## 1. Recommended production architecture

For a simple cloud deployment:

- Frontend: Vercel (Vite/React)
- Backend: Render Web Service using `backend/Dockerfile`
- Database: Supabase PostgreSQL
- API base URL: the public Render backend URL

Supabase recommends a connection string for application backends. For an IPv4-only persistent backend, its shared pooler session mode uses port 5432; get the exact connection string from the Supabase Dashboard > Connect. Do not copy a password into Git. 

## 2. Before deploying

1. Push this project to GitHub.
2. Create a Supabase PostgreSQL database.
3. In Supabase, click **Connect** and copy the PostgreSQL connection string. Use the session pooler connection if your hosting environment needs IPv4 connectivity.
4. Generate a strong random JWT secret (at least 32 characters).
5. Decide whether to seed demo data:
   - `APP_SEED_DEMO_DATA=true` for a demo/staging deployment.
   - `APP_SEED_DEMO_DATA=false` for production. In production, provide the bootstrap admin variables.

## 3. Deploy backend to Render

Render supports Docker services and monorepos. For this repository:

- **New → Web Service**
- Connect the GitHub repository
- Branch: `main`
- Runtime/Language: **Docker**
- Root Directory: `backend`
- Dockerfile: `Dockerfile`
- Health Check Path: `/actuator/health`

The backend listens on `${PORT}` and defaults to 8080. Render supplies a `PORT` value for web services, so the application is configured to use it.

### Backend environment variables

Set these in Render → Environment:

```env
SPRING_PROFILES_ACTIVE=postgres

DB_URL=jdbc:postgresql://<supabase-host>:5432/postgres
DB_USERNAME=<supabase-username>
DB_PASSWORD=<supabase-password>

JWT_SECRET=<long-random-secret>
JWT_EXPIRATION_MS=86400000

CORS_ALLOWED_ORIGINS=https://<your-frontend>.onrender.com

APP_SEED_DEMO_DATA=false
APP_BOOTSTRAP_ADMIN_USERNAME=admin
APP_BOOTSTRAP_ADMIN_EMAIL=<your-admin-email>
APP_BOOTSTRAP_ADMIN_PASSWORD=<strong-admin-password>
APP_BOOTSTRAP_ADMIN_FULL_NAME=System Administrator

JPA_DDL_AUTO=update
```

If your Supabase connection string already contains SSL/query parameters, preserve them exactly in `DB_URL`.

After deployment, verify:

```text
https://<your-backend>.onrender.com/actuator/health
```

It should return an HTTP 2xx response.

## 4. Deploy frontend to Vercel

In Vercel, import the same GitHub repository and set **Root Directory** to `frontend`.

Use:

- Build Command: `npm run build`
- Output Directory: `dist`

Add these Production Environment Variables:

```env
VITE_API_BASE_URL=https://<your-backend>.onrender.com/api
VITE_DEMO_MODE=false
```

`VITE_*` variables are intentionally public because the browser needs the API base URL. Never put database passwords, JWT secrets, or private API keys in `VITE_*` variables.

The repository includes `frontend/vercel.json` so React Router routes rewrite to `index.html`. Changing a `VITE_*` variable requires a new Vercel deployment.


## 5. CORS

After the frontend URL is known, set the backend:

```env
CORS_ALLOWED_ORIGINS=https://your-frontend.vercel.app
```

For multiple frontends, separate origins with commas:

```env
CORS_ALLOWED_ORIGINS=https://app.example.com,https://staging.example.com
```

Do not use `*` in production.

## 6. First production login

When `APP_SEED_DEMO_DATA=false` and the database is empty, PMTrack creates one Super Admin from:

```env
APP_BOOTSTRAP_ADMIN_USERNAME
APP_BOOTSTRAP_ADMIN_EMAIL
APP_BOOTSTRAP_ADMIN_PASSWORD
APP_BOOTSTRAP_ADMIN_FULL_NAME
```

Sign in with those credentials, then create:

1. Project Managers
2. Employees / Team Leads
3. Projects
4. Project members
5. Tasks and assignments

After verifying the deployment, rotate the bootstrap admin password if required by your organization's policy.

## 7. Demo/staging deployment

For a demo environment you can use:

```env
APP_SEED_DEMO_DATA=true
VITE_DEMO_MODE=true
```

The seed contains preconfigured personas and sample projects/tasks/timesheets.

Do not use the demo passwords in production.

## 8. Local development

### Backend with H2

```bash
cd backend
mvn spring-boot:run
```

Backend:

```text
http://localhost:8080
```

Swagger:

```text
http://localhost:8080/swagger-ui.html
```

Health:

```text
http://localhost:8080/actuator/health
```

### Frontend

```bash
cd frontend
npm ci
npm run dev
```

Frontend:

```text
http://localhost:5173
```

The Vite development proxy forwards `/api` to `http://localhost:8080`.

## 9. Local PostgreSQL/Docker deployment

From the repository root:

```bash
docker compose up --build -d
```

Then open:

```text
http://localhost
```

Check:

```bash
docker compose ps
docker compose logs -f backend
```

Stop:

```bash
docker compose down
```

Stop and delete the PostgreSQL volume:

```bash
docker compose down -v
```

The last command deletes the local database data.

## 10. Production smoke test

After deployment, test this exact workflow:

1. Login as Super Admin.
2. Create a Project Manager.
3. Create an Employee.
4. Create a project and assign the PM.
5. Add the employee to the project.
6. Create a task and assign the employee.
7. Login as the employee.
8. Update the task.
9. Create a daily timesheet.
10. Save it as Draft.
11. Submit it.
12. Login as the PM.
13. Open Approvals.
14. Approve or reject the timesheet.
15. If rejected, login as the employee and use **Edit & Resubmit**.
16. Verify the dashboard and reports change.
17. Verify notifications.
18. Verify the audit trail.
19. Verify unauthorized users cannot access another project's data.
20. Verify `/actuator/health` remains healthy.

## 11. Important production notes

- Never commit `.env`.
- Never commit a populated production secret file.
- Keep `JWT_SECRET` different between environments.
- Keep demo mode disabled in production.
- Use Supabase backups/retention appropriate to your project.
- `JPA_DDL_AUTO=update` is convenient for this v1 deployment. For a mature production system, move to versioned database migrations and use `validate`.
- The application records document/attachment **URLs** rather than storing uploaded binary files. If true file upload is required later, add object storage such as S3/Supabase Storage and store only the object metadata/URL in PostgreSQL.
