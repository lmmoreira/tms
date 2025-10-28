package br.com.logistics.tms.commons.infrastructure.database.transaction;

import java.util.function.Supplier;

public interface Transactional {

    void runWithinTransaction(Runnable action);

    void runWithinReadOnlyTransaction(Runnable action);

    <T> T runWithinTransactionAndReturn(Supplier<T> action);

    <T> T runWithinReadOnlyTransactionAndReturn(Supplier<T> action);

}
