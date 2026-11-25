# {{raw-name}} - Web Frontend

Single Page Application built with ClojureScript, Helix (React 18),
shadow-cljs, external CSS, and build-time cache busting.

## Getting Started

### 1. Install Dependencies

```bash
npm install
# or
make install
```

### 2. Run Development Server

Start shadow-cljs with live reload:

```bash
npx shadow-cljs watch frontend
# or
make dev
```

Open `http://localhost:3000`. Requests starting with `/api` are proxied
to `http://localhost:6000`.

### 3. Run Tests

Run the test suite in Node (JSDOM + React Testing Library):

```bash
npx shadow-cljs compile test
# or
make test
```

Watch and re-run tests on file changes:

```bash
npx shadow-cljs watch frontend test
# or
make dev-test
```

### 4. Production Release Build

```bash
npx shadow-cljs release frontend
# or
make release
```

Build hooks inject `?ts=<timestamp>` cache-busting parameters into
`public/index.html` and `public/sw.js` and generate `version.cljs`.
