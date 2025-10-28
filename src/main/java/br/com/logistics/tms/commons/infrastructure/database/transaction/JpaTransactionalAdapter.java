package br.com.logistics.tms.commons.infrastructure.database.transaction;

import br.com.logistics.tms.commons.application.usecases.UseCaseInterceptor;
import br.com.logistics.tms.commons.infrastructure.database.routing.DataSourceContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Supplier;

@Component
public class JpaTransactionalAdapter implements Transactional, UseCaseInterceptor {

    private final TransactionTemplate transactionTemplate;

    @Autowired
    public JpaTransactionalAdapter(final TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    private TransactionTemplate getTemplateFromContext() {
        assert transactionTemplate.getTransactionManager() != null;
        final TransactionTemplate template = new TransactionTemplate(transactionTemplate.getTransactionManager());
        template.setPropagationBehavior(transactionTemplate.getPropagationBehavior());
        template.setIsolationLevel(transactionTemplate.getIsolationLevel());
        template.setTimeout(transactionTemplate.getTimeout());
        template.setReadOnly(TransactionContextHolder.isReadOnly());
        return template;
    }

    @Override
    public void runWithinTransaction(Runnable action) {
        getTemplateFromContext().executeWithoutResult(status -> action.run());
    }

    @Override
    public void runWithinReadOnlyTransaction(Runnable action) {
        DataSourceContextHolder.markAsReadOnly();
        TransactionContextHolder.markAsReadOnly();

        try {
            getTemplateFromContext().executeWithoutResult(status -> action.run());
        } finally {
            TransactionContextHolder.clearReadOnlyContext();
            DataSourceContextHolder.clearReadOnlyContext();
        }
    }


    @Override
    public <T> T runWithinTransactionAndReturn(Supplier<T> action) {
        return getTemplateFromContext().execute(status -> action.get());
    }

    @Override
    public <T> T runWithinReadOnlyTransactionAndReturn(Supplier<T> action) {
        DataSourceContextHolder.markAsReadOnly();
        TransactionContextHolder.markAsReadOnly();

        try {
            return getTemplateFromContext().execute(status -> action.get());
        } finally {
            TransactionContextHolder.clearReadOnlyContext();
            DataSourceContextHolder.clearReadOnlyContext();
        }
    }

    @Override
    public <T> T intercept(Supplier<T> next) {
        return runWithinTransactionAndReturn(next);
    }

}
