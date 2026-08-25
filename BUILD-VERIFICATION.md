# PMTrack Build Verification

## Corrected in this package

- Backend `Project.documentUrls` lazy-loading/JSON serialization failure.
- Notification `markAsRead` ownership/signature issue.
- Audit test method signature issue.
- Vercel-to-Render API base URL handling.
- Vercel SPA route rewrites.
- Authenticated CSV report download.
- Independent Project Manager and project loading.
- Project creation with initial team-member selection.
- Task creation with project, owner, and employee/assignee selection.
- Timesheet loading that survives an individual API failure.
- Timesheet project fallback from assigned tasks.
- Responsive utility CSS for the existing Tailwind-style JSX classes.
- Dynamic current-week start for weekly timesheets.
- Vercel + Render deployment documentation.

## Verification limitation

The supplied environment does not contain Maven, and frontend dependency installation could not complete within the execution window, so full Maven/Node production builds could not be executed here.

Run before pushing:

```bash
cd backend
mvn -B -DskipTests clean package

cd ../frontend
npm ci
npm run build
```

Render will run the backend Maven build through the Dockerfile.
