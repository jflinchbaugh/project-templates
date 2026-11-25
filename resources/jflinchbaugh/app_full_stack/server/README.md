# {{raw-name}} - Backend Server

Backend REST API and persistence server built with Clojure, HTTP-Kit,
Reitit, Ring (`ring-defaults`, `ring-cors`), Buddy Auth, and Datahike.

## Getting Started

### Run Server in Development

Run on port 6000 with in-memory storage (`Ctrl+C` to stop):

```bash
clojure -M:run-m 6000
```

Run with Datahike SQLite persistence:

```bash
clojure -M:run-m 6000 /path/to/{{artifact/id}}.sqlite
```

### Run Tests

```bash
clojure -T:build test
# or
make test
```

### Build Standalone Uberjar

```bash
clojure -T:build uber
# or
make uber
```

Run the compiled JAR:

```bash
java -jar target/{{artifact/id}}-server-standalone.jar 6000
```

## API Endpoints

- `GET /api/ping` -> `200 pong`
- `GET /api/health` -> `200 {"status":"ok"}`
- `POST /api/register` -> JSON `{"username":"alice","password":"secret"}`
- `GET /api/items` (Basic Auth) -> `200 {"items":[...]}`
- `POST /api/items` (Basic Auth) -> JSON `{"title":"First item"}`
- `DELETE /api/items/:id` (Basic Auth) -> `200 {"deleted":"..."}`

## Container & Backup

```bash
make container
make run-server
# Stop with: make stop-server
```

Dump or load Datahike data as EDN:

```bash
java -jar target/{{artifact/id}}-server-standalone.jar \
  dump /var/lib/datahike/{{artifact/id}}.sqlite > backup.edn
java -jar target/{{artifact/id}}-server-standalone.jar \
  load /var/lib/datahike/{{artifact/id}}.sqlite < backup.edn
```
