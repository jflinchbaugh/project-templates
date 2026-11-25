SRC_FILES := $(shell find src test deps.edn build.clj -type f)

all: container

.PHONY: clean run-server start-server stop-server test uber container

test: $(SRC_FILES)
	clojure -T:build test

uber: target/.uberjar-built
target/.uberjar-built: $(SRC_FILES)
	clojure -T:build uber
	touch target/.uberjar-built

container: test target/.uberjar-built Containerfile
	podman build --no-cache --pull -t {{artifact/id}}-server:latest .

clean:
	clojure -T:build clean
	rm -f target/.uberjar-built

run-server:
	podman kube play --wait {{artifact/id}}-server.yaml

start-server:
	podman kube play {{artifact/id}}-server.yaml

stop-server:
	podman kube down {{artifact/id}}-server.yaml
