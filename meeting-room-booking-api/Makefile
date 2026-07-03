# Makefile for database migrations using Liquibase

.PHONY: migrate status rollback seed help

## help: Show this help message
help:
	@echo "Available commands:"
	@echo "  make migrate      - Run all pending database migrations"
	@echo "  make status       - Check the status of database migrations"
	@echo "  make rollback     - Rollback migrations (requires COUNT parameter, e.g., make rollback COUNT=1)"
	@echo "  make rollback-all - Drop all database tables and schema (Complete reset)"


## seed: Run database seeder
seed:
	./mvnw spring-boot:run -Dspring-boot.run.profiles=seed

## migrate: Run database migrations
migrate:
	./mvnw liquibase:update

## status: Check migration status
status:
	./mvnw liquibase:status

## rollback: Rollback migrations by count
rollback:
	@if [ -z "$(COUNT)" ]; then \
		echo "Error: Please specify the number of migrations to rollback using COUNT=<number>"; \
		exit 1; \
	fi
	./mvnw liquibase:rollback -Dliquibase.rollbackCount=$(COUNT)

## rollback-all: Drop all database objects (reset database)
rollback-all:
	./mvnw liquibase:dropAll

test:
	./mvnw test -Dtest=AuthControllerTest

