package br.com.logistics.tms.commons.infrastructure.jpa.transaction;

import br.com.logistics.tms.commons.application.usecases.UseCaseInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Supplier;

@Component
public class JpaTransactionalAdapter implements Transactional, UseCaseInterceptor {

    private final TransactionTemplate transactionTemplate;

    public JpaTransactionalAdapter(final TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public void runWithinTransaction(Runnable action) {
        transactionTemplate.executeWithoutResult(status -> action.run());
    }

    @Override
    public <T> T runWithinTransactionAndReturn(Supplier<T> action) {
        return transactionTemplate.execute(status -> action.get());
    }

    @Override
    public <T> T intercept(Supplier<T> next) {
        return runWithinTransactionAndReturn(next);
    }
}
