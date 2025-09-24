CREATE EXTENSION IF NOT EXISTS pg_stat_statements;
SELECT * FROM pg_extension;
SELECT query, calls, total_exec_time, mean_exec_time FROM pg_stat_statements ORDER BY total_exec_time DESC LIMIT 5;