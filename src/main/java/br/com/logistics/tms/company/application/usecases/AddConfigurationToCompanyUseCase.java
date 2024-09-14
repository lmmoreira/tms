package br.com.logistics.tms.company.application.usecases;

import br.com.logistics.tms.commons.application.gateways.QueueGateway;

public class AddConfigurationToCompanyUseCase {

    private final QueueGateway queueGateway;

    public AddConfigurationToCompanyUseCase(QueueGateway queueGateway) {
        this.queueGateway = queueGateway;
    }

    public void execute(final Input input) {
        System.out.println("Adding configuration to company " + input.id() + " configuration " + input.configurationId() + " value " + input.value());
        queueGateway.publish("add_configuration_to_company", "add_configuration_to_company_routing_key", input);
    }

    public record Input(String id, String configurationId, String value) {

    }

}
