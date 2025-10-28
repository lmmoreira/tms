package br.com.logistics.tms.commons.infrastructure.database.routing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
public class RoutingDataSource extends AbstractRoutingDataSource {
    @Override
    protected Object determineCurrentLookupKey() {
        return DataSourceContextHolder.getDataSourceType();
    }

    @Override
    public Connection getConnection() throws SQLException {
        final Object lookupKey = determineCurrentLookupKey();
        final DataSource target = determineTargetDataSource();
        log.info("Getting connection -> lookupKey={}, txActive={}, txReadOnly={}, target={}",
                lookupKey,
                TransactionSynchronizationManager.isActualTransactionActive(),
                TransactionSynchronizationManager.isCurrentTransactionReadOnly(),
                target.getClass().getSimpleName());
        return super.getConnection();
    }
}