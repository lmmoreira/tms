package br.com.logistics.tms.shipmentorder.infrastructure.listener;

import br.com.logistics.tms.commons.application.annotation.Cqrs;
import br.com.logistics.tms.commons.application.annotation.DatabaseRole;
import br.com.logistics.tms.commons.infrastructure.usecases.VoidUseCaseExecutor;
import br.com.logistics.tms.shipmentorder.application.usecases.SynchronizeCompanyUseCase;
import br.com.logistics.tms.shipmentorder.infrastructure.dto.CompanyUpdatedDTO;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Cqrs(DatabaseRole.WRITE)
@Lazy(false)
public class CompanyUpdatedListener {

    private final VoidUseCaseExecutor voidUseCaseExecutor;
    private final SynchronizeCompanyUseCase synchronizeCompanyUseCase;

    public CompanyUpdatedListener(final VoidUseCaseExecutor voidUseCaseExecutor,
                                  final SynchronizeCompanyUseCase synchronizeCompanyUseCase) {
        this.voidUseCaseExecutor = voidUseCaseExecutor;
        this.synchronizeCompanyUseCase = synchronizeCompanyUseCase;
    }

    @RabbitListener(queues = "integration.shipmentorder.company.updated")
    public void handle(final CompanyUpdatedDTO event, final Message message, final Channel channel) {
        final Map<String, Object> data = new HashMap<>();
        data.put(event.property(), event.newValue());

        voidUseCaseExecutor
                .from(synchronizeCompanyUseCase)
                .withInput(new SynchronizeCompanyUseCase.Input(event.companyId(), data))
                .execute();
    }
}
