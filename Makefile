.PHONY: start-tms start-tms-oauth start-tms-with-observation down-tms down-tms-oauth down-tms-all

start-tms:
	docker compose up -d tms-database tms-neo4j tms-broker tms-flyway

start-tms-oauth:
	docker compose up -d tms-database tms-neo4j tms-broker tms-flyway tms-nginx tms-keycloak-database tms-keycloak tms-keycloak-oauth2-proxy

start-tms-with-observation:
	docker compose up -d tms-database tms-neo4j tms-broker tms-flyway tms-nginx tms-keycloak-database tms-keycloak tms-keycloak-oauth2-proxy tms-otel-collector tms-loki tms-prometheus tms-grafana tms-minio tms-jaeger

stop-tms:
	docker compose stop tms-database tms-neo4j tms-broker tms-flyway

stop-tms-oauth:
	docker compose stop tms-database tms-neo4j tms-broker tms-flyway tms-nginx tms-keycloak-database tms-keycloak tms-keycloak-oauth2-proxy

stop-tms-with-observation:
	docker compose stop tms-database tms-neo4j tms-broker tms-flyway tms-nginx tms-keycloak-database tms-keycloak tms-keycloak-oauth2-proxy tms-otel-collector tms-loki tms-prometheus tms-grafana tms-minio tms-jaeger

down-tms-all:
	docker compose down