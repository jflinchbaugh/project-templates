# {{raw-name}}

{{description}}

## REPL & Clay Workflow

Start an nREPL server with CIDER middleware and `dev/user.clj` loaded:

```bash
clojure -M:nrepl
```

In the REPL (`user` namespace):

```clojure
(start!)              ;; start Clay live preview server
(show! "sample.clj")  ;; render a specific notebook
(make!)               ;; render configured notebooks from clay.edn
(browse!)             ;; open Clay view in browser
(stop!)               ;; stop Clay preview server
```

## CLI Rendering & Live Watch

Render notebooks directly from the command line:

```bash
clojure -X:clay
clojure -X:clay :show false :browse false
clojure -T:build render
```

Watch and re-render on file changes:

```bash
clojure -M:clay/watch
```

Clean generated `docs/` and `target/` artifacts:

```bash
clojure -T:build clean
```

## Running Tests

```bash
clojure -T:build test
```

## Project Layout

- Put data files in `resources/` (for example `resources/sample.txt`).
- Put shared helpers in `src/{{main/file}}/core.clj`.
- Add analysis notebooks in `notebooks/` (for example `notebooks/sample.clj`) and update `clay.edn` or call `(show! "sample.clj")`.
- Add unit tests in `test/{{main/file}}/core_test.clj`.

## License

Copyright (c) {{now/year}} {{developer}}

Distributed under the Eclipse Public License version 1.0.
