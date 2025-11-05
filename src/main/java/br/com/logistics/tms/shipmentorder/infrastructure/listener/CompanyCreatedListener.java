package br.com.logistics.tms.shipmentorder.infrastructure.listener;

import br.com.logistics.tms.commons.application.annotation.Cqrs;
import br.com.logistics.tms.commons.application.annotation.DatabaseRole;
import br.com.logistics.tms.commons.application.mapper.Mapper;
import br.com.logistics.tms.commons.infrastructure.usecases.VoidUseCaseExecutor;
import br.com.logistics.tms.shipmentorder.application.usecases.SynchronizeCompanyUseCase;
import br.com.logistics.tms.shipmentorder.infrastructure.dto.CompanyCreatedDTO;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Cqrs(DatabaseRole.WRITE)
@Lazy(false)
public class CompanyCreatedListener {

    private final VoidUseCaseExecutor voidUseCaseExecutor;
    private final SynchronizeCompanyUseCase synchronizeCompanyUseCase;
    private final Mapper mapper;

    public CompanyCreatedListener(final VoidUseCaseExecutor voidUseCaseExecutor,
                                  final SynchronizeCompanyUseCase synchronizeCompanyUseCase,
                                  final Mapper mapper) {
        this.voidUseCaseExecutor = voidUseCaseExecutor;
        this.synchronizeCompanyUseCase = synchronizeCompanyUseCase;
        this.mapper = mapper;
    }

    @SuppressWarnings("unchecked")
    @RabbitListener(queues = "integration.shipmentorder.company.created")
    public void handle(final CompanyCreatedDTO event, final Message message, final Channel channel) {
        voidUseCaseExecutor
                .from(synchronizeCompanyUseCase)
                .withInput(new SynchronizeCompanyUseCase.Input(event.companyId(), mapper.map(event, Map.class)))
                .execute();
    }
}
