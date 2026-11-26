# com.hjsoft/project-templates

My collection of project templates for use with:
- https://github.com/seancorfield/deps-new
- https://github.com/practicalli/clojure-cli-config

## Usage

This is a template project for use with [deps-new](https://github.com/seancorfield/deps-new).
As originally generated, it will produce a new app project when run:

$ clojure -Sdeps '{:deps {com.hjsoft/project-templates {:local/root "."}}}' -Tnew create :template jflinchbaugh/app :name myusername/mycoolapp

Or with the repo already added to the alias:
$ clojure -T:project/create :template jflinchbaugh/app :name myusername/mycoolapp

For a web server application (`jflinchbaugh/app-web-server`):

$ clojure -Sdeps '{:deps {com.hjsoft/project-templates {:local/root "."}}}' -Tnew create :template jflinchbaugh/app-web-server :name myusername/mywebserver
$ clojure -T:project/create :template jflinchbaugh/app-web-server :name myusername/mywebserver

For a full-stack web + server application (`jflinchbaugh/app-full-stack`):

$ clojure -Sdeps '{:deps {com.hjsoft/project-templates {:local/root "."}}}' -Tnew create :template jflinchbaugh/app-full-stack :name myusername/myfullstackapp
$ clojure -T:project/create :template jflinchbaugh/app-full-stack :name myusername/myfullstackapp

For a Clay + Noj + REPL data analysis project (`jflinchbaugh/clay-repl`):

$ clojure -Sdeps '{:deps {com.hjsoft/project-templates {:local/root "."}}}' -Tnew create :template jflinchbaugh/clay-repl :name myusername/myanalysis
$ clojure -T:project/create :template jflinchbaugh/clay-repl :name myusername/myanalysis

Or with a single-word `:name` (which creates `./mycoolapp/` and `mycoolapp.*` namespaces without duplicating `mycoolapp.mycoolapp`):

$ clojure -T:project/create :template jflinchbaugh/app :name mycoolapp

## Naming, Directories, and Namespaces

`deps-new` treats `:name` as a project coordinate (`qualifier/project-name` or a single-word `project-name`), not a full Clojure namespace. All templates in this repo derive a consistent root namespace prefix and place namespaces in directories underneath it.

### Where the project resides

- By default, the project is created in `./<project-leaf>/` inside the current working directory — the part after `/` (or the final segment if `:name` has multiple `/` or `.` segments, so `com.hjsoft/my-app`, `hjsoft/com/full/my-app`, and `com.hjsoft.my-app` all default to `./my-app/`).
- Pass `:target-dir '"/path/to/dir"'` to place the project in a different directory.
- Hyphens in the project name stay hyphens in the project directory name and Clojure namespaces (e.g., `my-app/core.clj` -> `my-app.core`), and become underscores in source/test directory paths (`my_app/core.clj`).

### How `:name` maps to the root namespace

- **Qualified name** (`com.hjsoft/my-app`, `acme/my-app`): uses `<qualifier>.<project-name>` (e.g., `com.hjsoft.my-app`, `acme.my-app`).
  - Note: `deps-new` strips common hosting prefixes like `io.github.` from the qualifier, so `io.github.jflinchbaugh/my-app` becomes `jflinchbaugh.my-app`.
- **Single-word name** (`my-app`): deduplicates `deps-new`'s default `<name>/<name>` so the root namespace is just `my-app` (never `my-app.my-app`).
- **Multi-segment slash or dotted name** (`hjsoft/com/full/stack`, `com.hjsoft.full/stack`, or `com.hjsoft.full.stack`): uses all segments as the root namespace (`hjsoft.com.full.stack` / `com.hjsoft.full.stack`) and uses the final segment (`stack`) as the default directory (`./stack/`) and artifact ID.

### Namespace and directory layout by template

| Template | `:name com.hjsoft/my-app` | `:name my-app` | Project dir |
|---|---|---|---|
| `jflinchbaugh/app` | `src/com/hjsoft/my_app/core.clj` (`com.hjsoft.my-app.core`)<br>`test/com/hjsoft/my_app/core_test.clj` (`com.hjsoft.my-app.core-test`) | `src/my_app/core.clj` (`my-app.core`)<br>`test/my_app/core_test.clj` (`my-app.core-test`) | `./my-app/` |
| `jflinchbaugh/app-web-server` | `src/com/hjsoft/my_app/core.clj` (`com.hjsoft.my-app.core`)<br>`test/com/hjsoft/my_app/core_test.clj` (`com.hjsoft.my-app.core-test`) | `src/my_app/core.clj` (`my-app.core`)<br>`test/my_app/core_test.clj` (`my-app.core-test`) | `./my-app/` |
| `jflinchbaugh/app-full-stack` | `server/src/com/hjsoft/my_app/{server,db,logging}.clj`<br>`web/src/main/com/hjsoft/my_app/{app,core,ui,build_hooks}.*`<br>(`com.hjsoft.my-app.*`) | `server/src/my_app/{server,db,logging}.clj`<br>`web/src/main/my_app/{app,core,ui,build_hooks}.*`<br>(`my-app.*`) | `./my-app/` |
| `jflinchbaugh/clay-repl` | `src/com/hjsoft/my_app/core.clj` (`com.hjsoft.my-app.core`)<br>`test/com/hjsoft/my_app/core_test.clj` (`com.hjsoft.my-app.core-test`) | `src/my_app/core.clj` (`my-app.core`)<br>`test/my_app/core_test.clj` (`my-app.core-test`) | `./my-app/` |

## Testing

### Template spec and generation tests

Run the repository's unit tests (validates each `template.edn` against `deps-new` specs and generates each template into a temp directory):

$ clojure -T:build test

### End-to-end test script

Run the test script to execute the repo test suite, generate projects from the local templates in a temporary directory, and verify their entry points, tests, and HTTP endpoints:

$ ./scripts/test-templates.sh
$ ./scripts/test-templates.sh app
$ ./scripts/test-templates.sh app-web-server
$ ./scripts/test-templates.sh app-full-stack
$ ./scripts/test-templates.sh clay-repl

Useful flags:
- `--ci`: also run release/uberjar builds (`clojure -T:build ci` on `app-web-server`, `make build` on `app-full-stack`)
- `--outdated`: also run `clojure -T:search/outdated` in each generated project
- `--keep`: keep the temporary generated directory for inspection

Examples:

$ ./scripts/test-templates.sh --ci app-web-server
$ ./scripts/test-templates.sh --ci app-full-stack
$ ./scripts/test-templates.sh --ci clay-repl
$ ./scripts/test-templates.sh --keep app-full-stack
$ ./scripts/test-templates.sh --outdated app-full-stack

### Manual testing

Generate a project into `/tmp` and exercise it directly:

$ clojure -T:project/create :template jflinchbaugh/app-web-server :name acme/demo-web :target-dir '"/tmp/demo-web"'
$ cd /tmp/demo-web
$ clojure -T:build test
$ clojure -X:run-x            # or: clojure -M:run-m 8080

In another terminal:

$ curl -i http://localhost:8080/
$ curl -i http://localhost:8080/health

Build an uberjar or check for outdated dependencies:

$ clojure -T:build ci
$ clojure -T:search/outdated

> Note: `deps-new` maps hyphens in `:template` names to underscores when resolving resources under `resources/` (for example, `jflinchbaugh/app-web-server` maps to `resources/jflinchbaugh/app_web_server/`).
