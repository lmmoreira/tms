package br.com.logistics.tms.commons.infrastructure.jpa.transaction;

import java.util.function.Supplier;

public interface Transactional {

    void runWithinTransaction(Runnable action);

    <T> T runWithinTransactionAndReturn(Supplier<T> action);

}
