# DevSync AI — Frontend Workspace

A visually stunning, premium SaaS-style Agile Project Management and Collaboration frontend dashboard built to consume the DevSync AI Spring Boot backend.

---

## Technical Stack
*   **Core**: React 18+, TypeScript, Vite
*   **Styling**: Tailwind CSS v4, shadcn/ui custom theme patterns
*   **Routing**: React Router v6
*   **Network / Async Operations**: Axios + TanStack React Query (v5)
*   **Forms & Validation**: React Hook Form + Zod

---

## Local Setup & Installation

### 1. Install Dependencies
```bash
npm install
```

### 2. Run the Development Server
Spin up the hot-reloading development server on port `5173`:
```bash
npm run dev
```
Open [http://localhost:5173](http://localhost:5173) in your browser.

### 3. Lint
```bash
npm run lint
```

### 4. Production Build
Type-checks the project and compiles production bundles into `dist/`:
```bash
npm run build
```

If you don't have Node.js/npm installed locally, run any of the above through Docker instead, e.g.:
```bash
docker run --rm -v $(pwd):/app -w /app node:20-alpine npm install
docker run -it --rm -p 5173:5173 -v $(pwd):/app -w /app node:20-alpine npm run dev -- --host
docker run --rm -v $(pwd):/app -w /app node:20-alpine npm run build
```

---

## API & JWT Authentication Configuration
All requests flow through the Axios interceptor (`src/lib/api.ts`). By default, it hits `http://localhost:8080` (Spring Boot monolith local backend).
To override, configure the environment variable:
*   `VITE_API_URL`: Custom backend API endpoint.

---

## Recent Changes (Build Health Pass)

- Verified `npm run build` (TypeScript project references + Vite) completes cleanly with no compile errors.
- Cleaned up `npm run lint` (oxlint) output: removed 16 unused-`err`-in-`catch` warnings across `Teams.tsx`, `Settings.tsx`, `ProjectDetails.tsx`, `AIAssistant.tsx`, `Organizations.tsx`, `SprintBoard.tsx`, and `ForgotPassword.tsx` by dropping the unused catch binding where the error itself was never inspected.
- Remaining lint warnings are intentional patterns, not bugs: the `react-hooks/exhaustive-deps` warnings flag data-loading functions that are deliberately called once on mount (adding them to the dependency array would require wrapping each in `useCallback` first — left as a follow-up), and the `react/only-export-components` warnings flag context files (`AuthContext`, `ThemeContext`, `ToastContext`) that intentionally export both a provider component and a hook.
- Simplified the setup instructions to run directly with local Node/npm (Docker remains available as a fallback), and documented `npm run lint`.
