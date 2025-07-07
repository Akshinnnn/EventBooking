.DEFAULT_GOAL := run

ifneq (,$(wildcard ./config/.env))
    include config/.env
    export
endif

args = -f docker-compose.yml --env-file config/.env --project-name ${PROJECT_NAME}

SERVICE:=

run:
	@echo "Running the application..."
	@docker compose $(args) up --build ${SERVICE}

run-d:
	@echo "Running the application..."
	@docker compose $(args) up -d --build ${SERVICE}

up:
	@echo "Running the application..."
	@docker compose $(args) up -d ${SERVICE}

build:
	@echo "Building the application..."
	@docker compose $(args) build ${SERVICE}

logs:
	@echo "Showing logs..."
	@docker compose $(args) logs -f ${SERVICE}

down:
	@echo "Stopping the application..."
	@docker compose $(args) down ${SERVICE}

down-v:
	@echo "Stopping the application and removing volumes..."
	@docker compose $(args) down -v ${SERVICE}

restart:
	@echo "Restarting the application..."
	@docker compose $(args) down ${SERVICE} && docker compose $(args) up -d --build ${SERVICE}