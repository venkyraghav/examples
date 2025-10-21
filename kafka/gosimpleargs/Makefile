default: help

VERSION=0.0.1
PROJECTNAME=$(shell basename "$(PWD)")
CLI_MAIN_FOLDER=.
BIN_FOLDER=bin
BIN_FOLDER_MACOS=${BIN_FOLDER}/amd64/darwin
BIN_FOLDER_WINDOWS=${BIN_FOLDER}/amd64/windows
BIN_FOLDER_LINUX=${BIN_FOLDER}/amd64/linux
BIN_FOLDER_SCRATCH=${BIN_FOLDER}/amd64/scratch
BIN_NAME=${PROJECTNAME}

PROTO_DIR=api/proto
PROTO_OUT=.
AVRO_DIR=api/avro
AVRO_OUT=.

# Make is verbose in Linux. Make it silent.
MAKEFLAGS += --silent
# LDFLAGS=--ldflags="-linkmode external -extldflags -static" 
LDFLAGS=-X main.buildDate=$(shell date -u '+%Y-%m-%dT%H:%M:%SZ') -X main.version=${VERSION} -X main.programName=${PROJECTNAME}


## setup: install all build dependencies for ci
setup: mod-download

## compile: compiles project in current system
compile: clean generate fmt vet test gen-proto gen-avro build

## watch: format, test and build project at go files modification
watch:
	@echo "  >  Watching go files..."
	@if !type "entr" > /dev/null 2>&1; then \
		echo "Please install entr: http://eradman.com/entrproject/"; \
	else \
		find -type f -name *.go \
			| entr make clean generate fmt vet test-colorized build; \
	fi

# ---------------------------------------------------------------------------

clean:
	@echo "  >  Cleaning build cache"
	@-rm -rf ${BIN_FOLDER}/amd64 ${BIN_FOLDER}/${BIN_NAME} \
		&& go clean ./...

gen-proto:
	@echo "  >  generating proto"
	@mkdir -p $(PROTO_OUT)
	@protoc --go_out=$(PROTO_OUT) --go-grpc_out=$(PROTO_OUT) $(PROTO_DIR)/*.proto

gen-avro:
	@echo "  >  generating avro"
	@mkdir -p $(AVRO_OUT)
	@gogen-avro -package main $(AVRO_OUT) $(AVRO_DIR)/*.avsc

build: gen-proto gen-avro
	@echo "  >  Building binary"
	@go build \
		-ldflags="${LDFLAGS}" \
		-o ${BIN_FOLDER}/${BIN_NAME} \
		.

build-all: gen-proto build-macos build-windows build-linux build-alpine-scratch

build-macos:
	@echo "  >  Building binary for MacOS"
	@GOOS=darwin GOARCH=amd64 \
		go build \
		-ldflags="${LDFLAGS}" \
		-o ${BIN_FOLDER_MACOS}/${BIN_NAME} \
		"${CLI_MAIN_FOLDER}"

build-windows:
	@echo "  >  Building binary for Windows"
	@GOOS=windows GOARCH=amd64 \
		go build \
		-ldflags="${LDFLAGS}" \
		-o ${BIN_FOLDER_WINDOWS}/${BIN_NAME}.exe \
		"${CLI_MAIN_FOLDER}"

build-linux:
	@echo "  >  Building binary for Linux"
	@GOOS=linux GOARCH=amd64 \
		go build \
		-ldflags="${LDFLAGS}" \
		-o ${BIN_FOLDER_LINUX}/${BIN_NAME} \
		"${CLI_MAIN_FOLDER}"

# Alpine & scratch base images use musl instead of gnu libc, thus we need to add additional parameters on the build
build-alpine-scratch:
	@echo "  >  Building binary for Alpine/Scratch"
	@CGO_ENABLED=0 GOOS=linux GOARCH=amd64 \
		go build \
		-ldflags="${LDFLAGS}" \
		-a -installsuffix cgo \
		-o ${BIN_FOLDER_SCRATCH}/${BIN_NAME} \
		"${CLI_MAIN_FOLDER}"

fmt:
	@echo "  >  Formatting code"
	@go fmt ./...

generate:
	@echo "  >  Go generate"
	@if !type "stringer" > /dev/null 2>&1; then \
		go install golang.org/x/tools/cmd/stringer@latest; \
	fi
	@go generate ./...

mod-download:
	@echo "  >  Download dependencies..."
	@go mod download && go mod tidy

test:
	@echo "  >  Executing unit tests"
	@go test -v -timeout 60s -race ./...

test-colorized:
	@echo "  >  Executing unit tests"
	@if ! type "richgo" > /dev/null 2>&1; then \
		go install github.com/kyoh86/richgo@latest; \
	fi
	@richgo test -v -timeout 60s -race ./...

vet:
	@echo "  >  Checking code with vet"
	@go vet ./...

.PHONY: help
all: help
help: Makefile
	@echo
	@echo " Choose a command run in "$(PROJECTNAME)":"
	@echo
	@sed -n 's/^##//p' $< | column -t -s ':' |  sed -e 's/^/ /'
	@echo