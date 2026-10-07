# Online Examination Portal

Full-stack examination portal with a Spring Boot backend and React frontend.

## Structure

- `backend/` — Spring Boot REST API, JWT security, H2 database
- `frontend/` — React + Vite single-page application

## Quick start

1. Install/select a JDK 17 runtime, then start the API: `cd backend && mvn spring-boot:run`
2. Start the web app: `cd frontend && npm install && npm run dev`
3. Open `http://localhost:5173`

The application creates an administrator at startup:

- Email: `admin@gmail.com`
- Password: `admin`

> Change the default administrator password before deploying.

## Java version

The backend is built for **Java 17**. Do not run it with Java 24; select a JDK 17 installation in your IDE's Project SDK and Maven runner settings first.

## Reset local admin access

If an existing local database has a different administrator password, run the backend once with `RESET_ADMIN_PASSWORD=true`. This resets only `admin@gmail.com` to `admin`. Remove the variable and restart normally afterwards.

## API configuration

Set `VITE_API_URL` in `frontend/.env` when the API is hosted somewhere other than `http://localhost:8080/api`. For deployment, set a long random `JWT_SECRET` and `CORS_ORIGIN` environment variable for the backend.
