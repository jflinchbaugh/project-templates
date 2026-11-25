#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

RUN_CI=false
RUN_OUTDATED=false
KEEP_TMP=false
TEMPLATES=()

usage() {
  cat <<'EOF'
Usage: scripts/test-templates.sh [OPTIONS] [TEMPLATE...]

Test project templates in this repository end-to-end.

Templates:
  app              Test jflinchbaugh/app
  app-web-server   Test jflinchbaugh/app-web-server
  (defaults to running all templates when omitted)

Options:
  --ci             Run clojure -T:build ci (uberjar) on app-web-server
  --outdated       Run clojure -T:search/outdated in each generated project
  --keep           Keep the temporary directory after finishing
  -h, --help       Show this help message and exit
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --ci)
      RUN_CI=true
      shift
      ;;
    --outdated)
      RUN_OUTDATED=true
      shift
      ;;
    --keep)
      KEEP_TMP=true
      shift
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    -*)
      echo "Unknown option: $1" >&2
      usage >&2
      exit 1
      ;;
    app|app-web-server)
      TEMPLATES+=("$1")
      shift
      ;;
    *)
      echo "Unknown template: $1" >&2
      usage >&2
      exit 1
      ;;
  esac
done

if [[ ${#TEMPLATES[@]} -eq 0 ]]; then
  TEMPLATES=("app" "app-web-server")
fi

WORK_DIR="$(mktemp -d)"
cleanup() {
  if [[ "$KEEP_TMP" == true ]]; then
    echo "Kept temporary directory: $WORK_DIR"
  else
    rm -rf "$WORK_DIR"
  fi
}
trap cleanup EXIT

create_project() {
  local template="$1"
  local name="$2"
  local target_dir="$3"
  local sdeps
  sdeps="{:deps {io.github.seancorfield/deps-new {:git/tag \"v0.12.2\" :git/sha \"465b303\"} com.hjsoft/project-templates {:local/root \"$REPO_ROOT\"}}}"
  clojure -Sdeps "$sdeps" \
    -X org.corfield.new/create \
    :template "$template" \
    :name "$name" \
    :target-dir "\"$target_dir\""
}

run_outdated_if_enabled() {
  if [[ "$RUN_OUTDATED" == true ]]; then
    echo "--- Running clojure -T:search/outdated ---"
    clojure -T:search/outdated
  fi
}

test_app() {
  local dir="$WORK_DIR/demo-app"
  echo "=== Testing jflinchbaugh/app ==="
  create_project "jflinchbaugh/app" "acme/demo-app" "$dir"

  (
    cd "$dir"
    local out
    out="$(clojure -X:run-x)"
    echo "$out"
    grep -q "Hello, Clojure!" <<<"$out"

    out="$(clojure -M:run-m)"
    echo "$out"
    grep -q "Hello, World!" <<<"$out"

    out="$(clojure -M:run-m Tester)"
    echo "$out"
    grep -q "Hello, Tester!" <<<"$out"

    set +e
    out="$(clojure -T:build test 2>&1)"
    local rc=$?
    set -e
    echo "$out"
    if [[ "$rc" -eq 0 ]]; then
      echo "ERROR: expected placeholder test in app to fail" >&2
      exit 1
    fi
    grep -q "Ran 1 tests" <<<"$out"
    grep -q "1 failures, 0 errors\." <<<"$out"

    if [[ "$RUN_CI" == true ]]; then
      echo "Note: skipping clojure -T:build ci for app (intentional failing placeholder test)"
    fi

    run_outdated_if_enabled
  )
}

test_app_web_server() {
  local dir="$WORK_DIR/demo-web"
  local port="${TEST_PORT:-18089}"
  echo "=== Testing jflinchbaugh/app-web-server ==="
  create_project "jflinchbaugh/app-web-server" "acme/demo-web" "$dir"

  (
    cd "$dir"
    echo "--- Running clojure -T:build test ---"
    clojure -T:build test

    echo "--- Running live HTTP smoke test on port $port ---"
    clojure -M -e "
      (require '[acme.demo-web :as app]
               '[clojure.string :as str]
               '[org.httpkit.client :as http])
      (app/start {:port $port})
      (try
        (let [root @(http/get \"http://localhost:$port/\")
              named @(http/get \"http://localhost:$port/?name=Clojure\")
              health @(http/get \"http://localhost:$port/health\")
              missing @(http/get \"http://localhost:$port/missing\")]
          (assert (= 200 (:status root)))
          (assert (str/includes? (:body root) \"Hello, World!\"))
          (assert (= 200 (:status named)))
          (assert (str/includes? (:body named) \"Hello, Clojure!\"))
          (assert (= 200 (:status health)))
          (assert (= \"{\\\"status\\\":\\\"ok\\\"}\" (:body health)))
          (assert (= 404 (:status missing)))
          (println \"HTTP smoke test OK:\" (:status health) (:body health)))
        (finally
          (app/stop)))
      (System/exit 0)"

    if [[ "$RUN_CI" == true ]]; then
      echo "--- Running clojure -T:build ci ---"
      clojure -T:build ci
      local jar_path
      jar_path="$(find target -name '*.jar' -print -quit)"
      if [[ -z "$jar_path" ]]; then
        echo "ERROR: expected uberjar under target/" >&2
        exit 1
      fi
      echo "Built uberjar: $jar_path"
    fi

    run_outdated_if_enabled
  )
}

echo "=== Running project-templates test suite ==="
(
  cd "$REPO_ROOT"
  clojure -T:build test
)

for tmpl in "${TEMPLATES[@]}"; do
  case "$tmpl" in
    app)
      test_app
      ;;
    app-web-server)
      test_app_web_server
      ;;
  esac
done

echo "All template tests passed."
