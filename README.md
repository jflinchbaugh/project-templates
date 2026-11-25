# com.hjsoft/project-templates

My collection of project templates for use with:
- https://github.com/seancorfield/deps-new
- https://github.com/practicalli/clojure-cli-config

## Usage

This is a template project for use with [deps-new](https://github.com/seancorfield/deps-new).
As originally generated, it will produce a new library project when run:

$ clojure -Sdeps '{:deps {com.hjsoft/project-templates {:local/root "."}}}' -Tnew create :template jflinchbaugh/app :name myusername/mycoolapp

Or with the repo alredy added to th ealias:
$ clojure -T:project/create :template jflinchbaugh/app :name myusername/mycoolapp

For a web server application (`jflinchbaugh/app-web-server`):

$ clojure -Sdeps '{:deps {com.hjsoft/project-templates {:local/root "."}}}' -Tnew create :template jflinchbaugh/app-web-server :name myusername/mywebserver
$ clojure -T:project/create :template jflinchbaugh/app-web-server :name myusername/mywebserver

## Testing

### Template spec and generation tests

Run the repository's unit tests (validates each `template.edn` against `deps-new` specs and generates each template into a temp directory):

$ clojure -T:build test

### End-to-end test script

Run the test script to execute the repo test suite, generate projects from the local templates in a temporary directory, and verify their entry points, tests, and HTTP endpoints:

$ ./scripts/test-templates.sh
$ ./scripts/test-templates.sh app-web-server
$ ./scripts/test-templates.sh app

Useful flags:
- `--ci`: also run `clojure -T:build ci` (builds the uberjar) on `app-web-server`
- `--outdated`: also run `clojure -T:search/outdated` in each generated project
- `--keep`: keep the temporary generated directory for inspection

Examples:

$ ./scripts/test-templates.sh --ci app-web-server
$ ./scripts/test-templates.sh --keep app-web-server
$ ./scripts/test-templates.sh --outdated app-web-server

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
