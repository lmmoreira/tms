COMPOSE_PROJECT_NAME := tms
export COMPOSE_PROJECT_NAME

CORE_SERVICES := \
	tms-database \
	tms-database-pool \
	tms-broker \
	tms-flyway

OAUTH_SERVICES := \
	$(CORE_SERVICES) \
	tms-nginx \
	tms-keycloak-database \
	tms-keycloak \
	tms-keycloak-oauth2-proxy

OBS_SERVICES := \
	$(OAUTH_SERVICES) \
	tms-database-exporter \
	tms-otel-collector \
	tms-loki \
	tms-prometheus \
	tms-grafana \
	tms-minio \
	tms-jaeger

.PHONY: \
	start-tms start-tms-oauth start-tms-with-observation \
	stop-tms stop-tms-oauth stop-tms-with-observation \
	stop-tms-all down-tms-all

start-tms:
	docker compose up -d $(CORE_SERVICES)

start-tms-oauth:
	docker compose up -d $(OAUTH_SERVICES)

start-tms-with-observation:
	docker compose up -d $(OBS_SERVICES)

stop-tms:
	docker compose stop $(CORE_SERVICES)

stop-tms-oauth:
	docker compose stop $(OAUTH_SERVICES)

stop-tms-with-observation:
	docker compose stop $(OBS_SERVICES)

stop-tms-all:
	docker compose stop

down-tms-all:
	docker compose down