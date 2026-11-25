# {{raw-name}}

{{description}}

## Architecture Overview

This project is structured into two components:

- **[`server/`](server/README.md)**: Backend REST API server built with
  Clojure, HTTP-Kit, Reitit, Ring (`ring-defaults`, `ring-cors`), Buddy Auth,
  and Datahike (in-memory or SQLite). Includes container and Podman Quadlet
  files.
- **[`web/`](web/README.md)**: Frontend SPA built with ClojureScript, Helix
  (React 18), shadow-cljs, external CSS, and build-time cache busting.

## Quick Start

### Run All Tests

```bash
make test
```

### Run Services Locally

1. **Start the Backend Server**:
   ```bash
   cd server
   clojure -M:run-m 6000
   ```
   See [server/README.md](server/README.md) for persistence and API details.

2. **Start the Web Frontend**:
   ```bash
   cd web
   npm install
   npx shadow-cljs watch frontend
   ```
   Open `http://localhost:3000`. API requests to `/api` are proxied to
   `http://localhost:6000`. See [web/README.md](web/README.md) for details.

### Build Release Artifacts

Build the server uberjar and optimized frontend bundle:

```bash
make build
```

### Build and Run Server Container

```bash
cd server && make container
make run-server
# Stop with: make stop-server
```

## License

Copyright (c) {{now/year}} {{developer}}

Distributed under the Eclipse Public License version 1.0.
